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
package clarin.cmdi.componentregistry.util;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource cache that uses a remote URL to retrieve content and a local
 * resource that serves as a fallback in case the remote content cannot be
 * retrieved (or not fast enough)
 *
 * @author twagoo
 */
public class RemoteResourceCache {

    private final static Logger logger = LoggerFactory.getLogger(RemoteResourceCache.class);

    private final static String CACHE_KEY = "CONCEPTS_REST_SERVICE_CACHE_KEY";

    private final AsyncLoadingCache<Object, String> cache;
    private final String fallbackResource;
    private final String resourceUrl;
    private String contentFromResource = null;

    private Duration cacheExpiryTime = Duration.ofMinutes(10);
    private Duration retrievalTimeout = Duration.ofSeconds(2);

    public RemoteResourceCache(String resourceUrl, String fallbackResource) {
        this.resourceUrl = resourceUrl;
        this.fallbackResource = fallbackResource;

        //define cache
        cache = Caffeine.newBuilder()
                .initialCapacity(1)
                .expireAfterWrite(cacheExpiryTime)
                .<Object, String>buildAsync(this::getContentFromUrlOrResource);
    }

    public void init() {
        if (contentFromResource == null) {
            if (fallbackResource != null) {
                try {
                    contentFromResource = getContentFromLocalResource();
                } catch (IOException ex) {
                    logger.error("Failed to read resource at", fallbackResource);
                }
            }
        }
    }

    public String getContent() {
        return getContent(retrievalTimeout);
    }

    public String getContent(Duration timeout) {
        init();

        try {
            // Request the content from the cache (give it some time to retrieve if necessary)
            return cache
                    .get(CACHE_KEY)
                    .get(timeout.toSeconds(), TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException ex) {
            logger.debug("Exception while getting content:", ex);

            //Timeout, retrieval of content from remote location took a longish, 
            //so returning the bundled content instead to not leave the client waiting
            return contentFromResource;
        }
    }

    public void setCacheExpiryTime(Duration cacheExpiryTime) {
        this.cacheExpiryTime = cacheExpiryTime;
    }

    public void setRetrievalTimeout(Duration retrievalTimeout) {
        this.retrievalTimeout = retrievalTimeout;
    }

    private String getContentFromUrlOrResource(Object key) {
        try {
            return getContentFromURL();
        } catch (URISyntaxException | IOException ex1) {
            logger.debug("Failure while getting resource from URL", ex1);
            try {
                return getContentFromLocalResource();
            } catch (IOException ex2) {
                logger.warn("Failure while getting content from local resource", ex2);
                return null;
            }
        }
    }

    private String getContentFromLocalResource() throws IOException {
        try (InputStream is = getClass().getResourceAsStream(fallbackResource)) {
            return readFromInputStream(is);
        }
    }

    private String getContentFromURL() throws IOException, URISyntaxException {
        logger.debug("Retrieving resource content from URL: {}", resourceUrl);
        try (InputStream is = new URI(resourceUrl).toURL().openStream()) {
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

}
