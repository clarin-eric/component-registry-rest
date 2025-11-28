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
package clarin.cmdi.componentregistry.concepts.wikidata;

import clarin.cmdi.componentregistry.model.Concept;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.wikibaseapi.BasicApiConnection;
import org.wikidata.wdtk.wikibaseapi.WbGetEntitiesSearchData;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;

/**
 *
 * @author twagoo
 */
@Component
public class WikiDataConceptsService {

    private final Logger log = LoggerFactory.getLogger(WikiDataConceptsService.class);
    public static final Long RESULTS_LIMIT = 50L;

    public static final String ITEM_TYPE = "item";
    public static final String PROPERTY_TYPE = "property";

    private WikibaseDataFetcher wbdf;

    @PostConstruct
    public void init() {
        wbdf = new WikibaseDataFetcher(
                BasicApiConnection.getWikidataApiConnection(),
                Datamodel.SITE_WIKIDATA);
    }

    public Stream<Concept> search(String query) {
        return search(query, ITEM_TYPE);
    }

    public Stream<Concept> search(String query, String... types) {
        return Stream.of(types)
                .flatMap(type -> this.search(query, type));
    }

    public Stream<Concept> search(String query, String type) {
        try {
            WbGetEntitiesSearchData properties = new WbGetEntitiesSearchData();
            properties.search = query;
            properties.type = type;
            properties.language = "en";

            final List<WbSearchEntitiesResult> results = wbdf.searchEntities(properties);
            log.debug("Result for query '{}': {} entities", query, results.size());

            final Stream<WbSearchEntitiesResult> resultStream;
            if (log.isDebugEnabled()) {
                //log the items
                resultStream = results.stream().peek(entity -> log.debug("{}: {}", entity.getConceptUri(), entity.getLabel()));
            } else {
                resultStream = results.stream();
            }

            return resultStream.map(WikiDataConceptsService::wikiDataEntityToConcept);
        } catch (IOException | MediaWikiApiErrorException ex) {
            throw new RuntimeException("Failed to query Wikidata", ex);
        }
    }

    public static Concept wikiDataEntityToConcept(WbSearchEntitiesResult entity) {
        final Concept concept = new Concept();
        concept.setId(entity.getEntityId());
        concept.setUri(entity.getConceptUri());
        concept.setLabel(entity.getLabel());
        concept.setDescription(entity.getDescription());
        return concept;
    }
}
