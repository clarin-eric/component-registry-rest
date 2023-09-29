/*
 * Copyright (C) 2023 CLARIN ERIC
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
package clarin.cmdi.componentregistry.servlet;

import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.skosmos.SkosmosService;
import java.net.URI;
import java.time.Duration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

/**
 *
 * @author twagoo
 */
public abstract class SkosmosServiceServlet extends HttpServlet {

    //Skosmos endpoint knowledge
    private static final String SKOSMOS_VOCABULARY_PAGE_PATH_FORMAT = "/%s";

    protected static final String CONTENT_TYPE_HEADER_VALUE_JSON = "application/json; charset=UTF-8";

    private String serviceBaseUrl;
    private SkosmosService skosmosService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        serviceBaseUrl = getConfiguration().getClavasRestUrl();
        final long skosmosCacheRefreshRateSeconds = getConfiguration().getSkosmosCacheRefreshRateSeconds();
        this.skosmosService = new SkosmosService(UriBuilder.fromUri(serviceBaseUrl).path("rest/v1").build(), Duration.ofSeconds(skosmosCacheRefreshRateSeconds));
    }

    protected final Configuration getConfiguration() {
        return Configuration.getInstance();
    }

    protected SkosmosService getSkosmosService() {
        return skosmosService;
    }

    protected URI getVocabPageUri(final String vocabUri) throws IllegalArgumentException, UriBuilderException {
        return UriBuilder.fromUri(serviceBaseUrl)
                .path(String.format(SKOSMOS_VOCABULARY_PAGE_PATH_FORMAT, vocabUri))
                .build();
    }

}
