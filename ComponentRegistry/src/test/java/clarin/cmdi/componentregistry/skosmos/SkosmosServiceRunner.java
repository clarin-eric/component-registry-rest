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

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.ws.rs.core.UriBuilder;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class SkosmosServiceRunner {

    private final static Logger logger = LoggerFactory.getLogger(SkosmosServiceRunner.class);

    //private final static String SERVICE_URI = "https://api.finto.fi/rest/v1";
    //private final static String SERVICE_URI = "https://cc-sprint-skosmos.clarin-dev.eu/rest/v1";
    private final static String SERVICE_URI = "https://clarin-skosmos.sd.di.huc.knaw.nl/rest/v1";
    private final static String CONCEPT_SCHEME_METADATA = "http://hdl.handle.net/11459/CCR_P-Metadata_6f3f84d1-6f06-6291-4e20-4cd361cca128";

    public final static void main(String[] args) throws InterruptedException, ExecutionException {
        final String query = "*test*";
        final String scheme = CONCEPT_SCHEME_METADATA;

        logger.info("Searching for \"{}\" in scheme \"{}\"", query, scheme);
        final SkosmosService service = new SkosmosService(UriBuilder.fromUri(SERVICE_URI).build());
        final List<Object> results = service.searchConcepts(query, scheme);
        logger.info("Received {} results", results.size());
        results.forEach(o -> logger.info("Result: {}", o));
    }

    public final static void oldMain(String[] args) throws InterruptedException, ExecutionException {
        final SkosmosService service = new SkosmosService(UriBuilder.fromUri(SERVICE_URI).build());

        Multimap<String, String> map = dummySchemeVocabMap();
//        for (int i = 1; i <= 5; i++) {
//            logger.info("Getting map (iteration {})", i);
        final Instant start = Instant.now();
        map = service.getConceptSchemeUriMap();
        logger.info("Map retrieved ({} items in {}ms)", map.size(), durationSince(start).getMillis());
//        }

//        for (int i = 1; i <= 5; i++) {
//            logger.info("Getting scheme info (iteration {})", i);
//            final Instant start = Instant.now();
//            for (String schemeUri : map.keys()) {
//                logger.info("Getting scheme info for {}", schemeUri);
//                final Map schemeInfo = service.getConceptSchemeInfo(schemeUri);
//                logger.debug("Concept scheme '{}': {} keys", schemeUri, schemeInfo.size());
//            }
//            logger.info("All info retrieved ({} items in {}ms)", map.size(), durationSince(start).getMillis());
//        }
        for (String schemeUri : map.keys()) {
            final List concepts = service.getConceptsInScheme(schemeUri);
        }

    }

    private static Multimap<String, String> dummySchemeVocabMap() {
        return ImmutableMultimap.<String, String>builder()
                .put("http://www.yso.fi/onto/koko/", "koko")
                .put("http://www.yso.fi/onto/yso/", "yso")
                .build();

    }

    private static Duration durationSince(Instant instant) {
        return new Interval(instant, Instant.now()).toDuration();
    }
}
