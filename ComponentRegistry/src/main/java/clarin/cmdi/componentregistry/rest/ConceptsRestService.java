/*
 * Copyright (C) 2025 CLARIN ERIC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.concepts.wikidata.WikiDataConceptsService;
import clarin.cmdi.componentregistry.model.Concept;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.spi.resource.Singleton;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.PostConstruct;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author twagoo
 */
@Path("/concepts")
@Service
@Singleton
@Api(value = "/concepts", produces = MediaType.APPLICATION_JSON)
public class ConceptsRestService {

    private final static Logger logger = LoggerFactory.getLogger(ConceptsRestService.class);
    private final static String RULES_CACHE_KEY = "CONCEPTS_REST_SERVICE_CACHE_KEY";
    private static final Duration RULES_CACHE_EXPIRY_DURATION = Duration.ofMinutes(10);

    public static final String ALL_TYPES = "all";

    @InjectParam
    private WikiDataConceptsService wdService;

    // Rules cache
    private final AsyncLoadingCache<Object, String> rulesCache;
    private String conceptUriFallbackResource;
    private String conceptUriRulesUrl;
    private String rulesFromResource = null;

    public ConceptsRestService() {
        this(null, null);
    }

    public ConceptsRestService(String conceptUriFallbackResource, String conceptUriRulesUrl) {
        this.conceptUriFallbackResource = conceptUriFallbackResource;
        this.conceptUriRulesUrl = conceptUriRulesUrl;

        //init cache
        rulesCache = Caffeine.newBuilder()
                .initialCapacity(1)
                .expireAfterWrite(RULES_CACHE_EXPIRY_DURATION)
                .<Object, String>buildAsync(this::getFromUrlOrResource);
    }

    @PostConstruct
    public void init() {
        if (conceptUriFallbackResource == null) {
            conceptUriFallbackResource = Configuration.getInstance().getConceptUriFallbackResource();
            logger.debug("Setting conceptUriFallbackResource from config:", conceptUriFallbackResource);
        }
        if (conceptUriRulesUrl == null) {
            conceptUriRulesUrl = Configuration.getInstance().getConceptUriRulesUrl();
            logger.debug("Setting conceptUriRulesUrl from config:", conceptUriRulesUrl);
        }

        //init rules definition
        if (conceptUriFallbackResource != null) {
            try {
                rulesFromResource = getRulesFromResource();
            } catch (IOException ex) {
                logger.error("Failed to read concept URI rules from resource at", conceptUriFallbackResource);
            }
        }

    }

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns a listing of groups to which an item belongs")
    public List<Concept> getGroupsTheItemIsAMemberOf(@QueryParam("q") String query, @QueryParam("type") @DefaultValue(ALL_TYPES) List<String> type) {
        return wdService.search(query, toWikidataTypes(type)).toList();
    }

    public String[] toWikidataTypes(List<String> types) {
        final ImmutableList.Builder<String> builder = ImmutableList.<String>builder();

        if (types != null) {
            //sanitise
            for (String type : types) {
                switch (type) {
                    case WikiDataConceptsService.ITEM_TYPE, WikiDataConceptsService.PROPERTY_TYPE ->
                        builder.add(type);
                    case ALL_TYPES -> {
                        builder.add(WikiDataConceptsService.ITEM_TYPE);
                        builder.add(WikiDataConceptsService.PROPERTY_TYPE);
                    }

                }
            }
        }
        return builder.build().toArray(String[]::new);
    }

    @GET
    @Path("/rules")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns structured rules for the evaluation of concept links")
    public String getRules() {

        try {
            // Request the rules from the cache (give it some time to retrieve if necessary)
            return rulesCache
                    .get(RULES_CACHE_KEY)
                    .get(2, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException ex) {
            logger.debug("Exception while getting rules:", ex);

            //Timeout, retrieval of rules from remote location took a longish, 
            // so returning the bundled rules instead to not leave the client waiting
            return rulesFromResource;
        }
    }

    private String getRulesFromResource() throws IOException {
        try (InputStream is = getClass().getResourceAsStream(conceptUriFallbackResource)) {
            return readFromInputStream(is);
        }
    }

    private String getRulesFromURL() throws IOException, URISyntaxException {
        logger.info("Retrieving concept URI rules from URL: {}", conceptUriRulesUrl);
        try (InputStream is = new URI(conceptUriRulesUrl).toURL().openStream()) {
            return readFromInputStream(is);
        }
    }

    private String readFromInputStream(final InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            reader.lines().forEach(line -> sb.append(line).append("\n"));
            return sb.toString();
        }
    }

    private String getFromUrlOrResource(Object key) {
        try {
            return getRulesFromURL();
        } catch (URISyntaxException | IOException ex1) {
            logger.debug("Failure while getting concept URI rules from URL", ex1);
            try {
                return getRulesFromResource();
            } catch (IOException ex2) {
                logger.warn("Failure while getting concept URI rules", ex2);
                return null;
            }
        }
    }

}
