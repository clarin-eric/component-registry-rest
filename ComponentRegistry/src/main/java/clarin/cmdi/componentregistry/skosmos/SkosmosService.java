/*
 * Copyright (C) 2022 CLARIN ERIC
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
package clarin.cmdi.componentregistry.skosmos;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class SkosmosService {

    private final static Logger logger = LoggerFactory.getLogger(SkosmosService.class);

    private static final String SKOSMOS_QUERY_PARAMETER_LANGUAGE = "lang";
    private static final String SKOSMOS_QUERY_PARAMETER_LANGUAGE_DEFAULT_VALUE = "en";

    private transient WebResource service;

    private final Object conceptSchemeUriMapCacheKey = new Object();

    private final AsyncLoadingCache<Object, Multimap<String, String>> conceptSchemeUriMapCache
            = Caffeine.newBuilder()
                    .maximumSize(10_000)
                    .refreshAfterWrite(60, TimeUnit.MINUTES)
                    .buildAsync(key -> createConceptSchemeUriMapCache());

    private final AsyncLoadingCache<String, Map> conceptSchemeInfoCache
            = Caffeine.newBuilder()
                    .maximumSize(10_000)
                    .refreshAfterWrite(60, TimeUnit.MINUTES)
                    .buildAsync(this::retrieveConceptSchemeInfo);

    public SkosmosService(URI serviceUri) {
        this.service = Client.create().resource(serviceUri);

        logger.info("Instantiated vocabulary servlet on URI {}", serviceUri);
    }

    public Multimap<String, String> getConceptSchemeUriMap() {
        try {
            return conceptSchemeUriMapCache.get(conceptSchemeUriMapCacheKey).get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException("Exception while getting concept scheme URIs from skosmos service", ex);
        }
    }

    public Map getConceptSchemeInfo(String uri) {
        try {
            return conceptSchemeInfoCache.get(uri).get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException("Exception while getting concept scheme info from skosmos service", ex);
        }
    }

    private Multimap<String, String> createConceptSchemeUriMapCache() throws IOException {
        final ImmutableSetMultimap.Builder<String, String> mapBuilder = ImmutableSetMultimap.<String, String>builder();

        // 1) get vocabularies
        logger.debug("Getting vocabulary identifiers");
        final Stream<String> vocabularyIds = getVocabularyIds();

        // 2) for each vocabulary get concept schemes and put in map
        logger.debug("Getting concept schemes for all vocabularies");
        vocabularyIds
                .peek(id -> logger.debug("Id: {}", id))
                .forEach(
                        vocabId -> getConceptUrisFromVocab(vocabId).forEach(
                                schemeUri -> mapBuilder.put(schemeUri, vocabId)));

        // 3) build the map
        return mapBuilder.build();
    }

    private Stream<String> getVocabularyIds() {
        final WebResource request = service
                .path("/vocabularies")
                .queryParam(SKOSMOS_QUERY_PARAMETER_LANGUAGE, SKOSMOS_QUERY_PARAMETER_LANGUAGE_DEFAULT_VALUE);
        logger.debug("Request: {}", request);

        final ClientResponse response = request.get(ClientResponse.class);
        if (response.getStatusInfo().getFamily() == SUCCESSFUL) {
            try (InputStream responseEntityStream = response.getEntityInputStream()) {
                final Object jsonObject = JsonUtils.fromInputStream(responseEntityStream);
                final Map context = new HashMap();
                final JsonLdOptions options = new JsonLdOptions();
                final Object compact = JsonLdProcessor.compact(jsonObject, context, options);
                if (compact instanceof Map) {
                    logger.trace("Vocabs response object: {}", compact);
                    Object vocabs = ((Map) compact).get("http://schema.onki.fi/onki#hasVocabulary");
                    logger.trace("Vocabularies in response: {}", vocabs);
                    if (vocabs instanceof List) {
                        return ((List<Map>) vocabs)
                                .stream()
                                .map(m -> m.get("http://schema.onki.fi/onki#vocabularyIdentifier").toString());
                    }
                }
            } catch (IOException ex) {
                logger.error("IOException while trying to process response for request: " + request.toString());
                throw new RuntimeException(ex);
            }
        }
        logger.warn("No vocabularies found in response (request: {}, response status: {})", request, response.getStatusInfo());
        return Stream.empty();
    }

    private Stream<String> getConceptUrisFromVocab(String id) {
        final WebResource request = service.path(id + "/");
        logger.debug("Request: {}", request);

        final ClientResponse response = request.get(ClientResponse.class);
        if (response.getStatusInfo().getFamily() == SUCCESSFUL) {
            try (InputStream responseEntityStream = response.getEntityInputStream()) {
                final Object jsonObject = JsonUtils.fromInputStream(responseEntityStream);
                final Map context = new HashMap();
                final JsonLdOptions options = new JsonLdOptions();
                final Object compact = JsonLdProcessor.compact(jsonObject, context, options);
                if (compact instanceof Map) {
                    logger.trace("Vocab response object: {}", compact);
                    Object schemes = ((Map) compact).get("http://schema.onki.fi/onki#hasConceptScheme");
                    logger.trace("Concept schemes in response: {}", schemes);
                    if (schemes instanceof List) {
                        return ((List<Map>) schemes)
                                .stream()
                                .map(m -> m.get("@id").toString());
                    }
                }
            } catch (IOException ex) {
                logger.error("IOException while trying to process response for request: " + request.toString());
                throw new RuntimeException(ex);
            }
        }
        logger.warn("No concept schemes found in response (request: {}, response status: {})", request, response.getStatusInfo());
        return Stream.empty();
    }

    private Map retrieveConceptSchemeInfo(String uri) {
        final WebResource request = service
                .path("/data")
                .queryParam("uri", uri);
        logger.debug("Request: {}", request);

        final ClientResponse response = request
                .accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        if (response.getStatusInfo().getFamily() == SUCCESSFUL) {
            try (InputStream responseEntityStream = response.getEntityInputStream()) {
                final Object jsonObject = JsonUtils.fromInputStream(responseEntityStream);
                final Map context = new HashMap();
                final JsonLdOptions options = new JsonLdOptions();
                final Object compact = JsonLdProcessor.compact(jsonObject, context, options);
                if (compact instanceof Map) {
                    logger.trace("Concept scheme data response object: {}", compact);
                    Object graph = ((Map) compact).get("@graph");
                    logger.trace("Graph in response: {}", graph);
                    if (graph instanceof List) {
                        return ((List<Map>) graph).stream()
                                .filter(map -> uri.equals(map.get("@id")))
                                .findFirst()
                                .orElseGet(Collections::emptyMap);
                    }
                }
            } catch (IOException ex) {
                logger.error("IOException while trying to process response for request: " + request.toString());
                throw new RuntimeException(ex);
            }
        }

        logger.warn("No data for concept scheme '{}', (request: {}, response status: {})", uri, request, response.getStatusInfo());
        return Collections.emptyMap();
    }
}
