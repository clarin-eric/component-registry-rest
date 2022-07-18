package clarin.cmdi.componentregistry.servlet;

import clarin.cmdi.componentregistry.Configuration;
import com.google.common.io.ByteStreams;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterface;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Servlet responsible for providing a bridge to the CLAVAS REST interface. Can
 * be called by the front end to circumvent cross-scripting and browser
 * limitation in setting the headers of a request.
 *
 * <p>
 * Sample requests:
 * <ul>
 * <li>/conceptscheme</li>
 * <li>/conceptscheme/vocabpage?id=xyz</li>
 * <li>/items?scheme=xyz&fields=uri,prefLabel</li>
 * </ul>
 * </p>
 *
 */
public class VocabularyServiceServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final static Logger logger = LoggerFactory.getLogger(VocabularyServiceServlet.class);

    private static final String CONTENT_TYPE_JSON = "application/json";

    /**
     * Vocabulary service path on external service
     */
    private static final String UPSTREAM_REST_BASE = "/rest/v1";
    private static final String VOCABULARIES_RESOURCE_PATH = UPSTREAM_REST_BASE + "/vocabularies";
    private static final String VOCABULARY_PAGE_PATH_FORMAT = "/%s/en/";
    private static final String VOCAB_ITEMS_INDEX_PATH = UPSTREAM_REST_BASE + "/%s/index/";

    private static final String QUERY_PARAMETER_LANGUAGE = "lang";
    private static final String QUERY_PARAMETER_FORMAT = "format";

    /**
     * Vocabulary service path on this service
     */
    private final static String VOCABS_PATH = "/vocabularies";
    /**
     * Vocabulary page redirect path on this service
     */
    private static final String VOCAB_PAGE_PATH = "/page";

    private static final String VOCAB_ITEMS_PATH = "/items";

    private static final String PARAM_ID = "id";
    private static final String PARAM_INDEX_POS = "index";

    private transient WebResource service;
    private URI serviceUri;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        this.serviceUri = UriBuilder.fromUri(Configuration.getInstance().getClavasRestUrl()).build();
        this.service = Client.create().resource(serviceUri);

        logger.info("Instantiated vocabulary servlet on URI {}", serviceUri);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String path = req.getPathInfo();
        if (path != null) {
            if (path.equals(VOCABS_PATH)) {
                //proxy the response (list of concept schemes (vocabularies)) from the original service
                serveVocabularies(req, resp);
                return;
            } else if (path.equals(VOCAB_ITEMS_PATH)) {
                //proxy the list of concepts (items) within a concept scheme (vocabulary) - JSON only
                serveConceptItems(req, resp);
                return;
            } else if (path.equals(VOCAB_PAGE_PATH)) {
                //redirect the client to the vocabulary page on the original service
                serveVocabularyPage(req, resp);
                return;
            }
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
    }

    private void serveVocabularies(HttpServletRequest servletRequest, HttpServletResponse resp) throws IOException {
        WebResource serviceReq = service.path(VOCABULARIES_RESOURCE_PATH);

        //forward query params to service request
        final Map<String, String[]> params = servletRequest.getParameterMap();
        if (params != null) {
            for (Map.Entry<String, String[]> param : params.entrySet()) {
                for (String value : param.getValue()) {
                    serviceReq = serviceReq.queryParam(param.getKey(), value);
                }
            }
        }
        if (params == null || !params.containsKey(QUERY_PARAMETER_LANGUAGE)) {
            // set mandatory language param
            serviceReq = serviceReq.queryParam(QUERY_PARAMETER_LANGUAGE, "en");
        }

        logger.debug("Forwarding vocabulary service request to {}", serviceReq.toString());
        final UniformInterface downstreamRequest = copyAcceptHeader(servletRequest, serviceReq, Optional.of(CONTENT_TYPE_JSON));
        forwardResponse(downstreamRequest, resp);
    }

    /**
     * Request concepts for a specific concept scheme ('scheme' parameter).
     * Optionally fields to be returned can be specified with the 'field'
     * parameter. Performs two requests: one to get the number of items, another
     * to perform the actual request. Response is JSON only!
     *
     * @param req
     * @param resp
     * @throws IOException
     */
    private void serveConceptItems(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String id = getSingleParamValue(req, PARAM_ID);
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "vocabulary id must be provided via 'id' query parameter");
            return;
        }

        final List<String> indexLetters = getIndexLetters(id);
        if (indexLetters == null) {
            resp.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return;
        }

        logger.debug("Letters in index: {}", indexLetters);
        final int index
                = Integer.parseInt(Optional.ofNullable(
                        getSingleParamValue(req, PARAM_INDEX_POS))
                        .orElse("0"));

        if (index >= indexLetters.size()) {
            // return empty object
            try (OutputStreamWriter osWriter = new OutputStreamWriter(resp.getOutputStream())) {
                osWriter.write(JSONWriter.valueToString(new JSONObject()));
            }
            resp.setStatus(Response.Status.OK.getStatusCode());
            resp.setContentType(CONTENT_TYPE_JSON);
            return;
        }

        final String letter = indexLetters.get(index);
        final WebResource serviceReq = service.path(String.format(VOCAB_ITEMS_INDEX_PATH + "%s", id, letter));

        logger.debug("Forwarding vocabulary service request to {}", serviceReq.toString());
        final UniformInterface downstreamRequest = copyAcceptHeader(req, serviceReq, Optional.of(CONTENT_TYPE_JSON));
        forwardResponse(downstreamRequest, resp);
    }

    private List<String> getIndexLetters(final String id) throws ClientHandlerException, UniformInterfaceException, RuntimeException {
        // retrieve index
        final ClientResponse indexResponse = service.path(String.format(VOCAB_ITEMS_INDEX_PATH, id))
                //.header("Accept", "application/json")
                .get(ClientResponse.class);
        final int indexResponseStatus = indexResponse.getStatus();
        final List<String> indexLetters = new ArrayList<>();
        if (indexResponseStatus >= 200 && indexResponseStatus < 300) {
            try {
                final JSONObject response = new JSONObject(indexResponse.getEntity(String.class));
                final JSONArray lettersArray = response.getJSONArray("indexLetters");
                if (lettersArray == null) {
                    logger.warn("Structure of index response not as expected - did not find /response");
                    return null;
                }
                lettersArray.forEach(o -> indexLetters.add(o.toString()));
            } catch (JSONException ex) {
                throw new RuntimeException("Could not retrieve items", ex);
            }
        }
        return indexLetters;
    }

    private UniformInterface copyAcceptHeader(HttpServletRequest req, WebResource serviceReq, Optional<String> defaultValue) {
        final String acceptHeader = req.getHeader("Accept");
        if (acceptHeader != null) {
            return serviceReq.header("Accept", acceptHeader);
        } else {
            if (defaultValue.isPresent()) {
                return serviceReq.header("Accept", defaultValue.get());
            }
        }

        return serviceReq;
    }

    private WebResource setFormatFromAcceptHeader(HttpServletRequest req, WebResource serviceReq) {
        final String acceptHeader = req.getHeader("Accept");
        if (acceptHeader != null) {
            if (acceptHeader.contains(MediaType.APPLICATION_JSON)) {
                serviceReq = serviceReq.queryParam(QUERY_PARAMETER_FORMAT, "json");
            } else if (acceptHeader.contains(MediaType.TEXT_HTML)) {
                serviceReq = serviceReq.queryParam(QUERY_PARAMETER_FORMAT, "html");
            }
        }
        return serviceReq;
    }

    private void serveVocabularyPage(HttpServletRequest req, HttpServletResponse resp) throws IllegalArgumentException, UriBuilderException, IOException {
        final String id = getSingleParamValue(req, PARAM_ID);
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "vocabulary id must be provided via 'id' query parameter");
            return;
        }
        // construct redirect URI to send client to the right page at the service
        final StringBuilder targetUriBuilder = new StringBuilder(
                UriBuilder.fromUri(serviceUri)
                        .path(String.format(VOCABULARY_PAGE_PATH_FORMAT, id))
                        .build().toString());

        // TODO: forward vocab info in JSON format in case of JSON accept header??
        resp.setStatus(HttpServletResponse.SC_SEE_OTHER);
        resp.sendRedirect(resp.encodeRedirectURL(targetUriBuilder.toString()));
    }

    private void forwardResponse(UniformInterface request, HttpServletResponse resp) throws IOException {
        //make GET request and copy directly to response stream
        final ClientResponse response = request.get(ClientResponse.class);
        //also forward content type header
        resp.setContentType(response.getHeaders().getFirst("content-type"));
        final InputStream get = response.getEntityInputStream();
        try (final InputStream serviceResultStream = get) {
            try (final ServletOutputStream responseOutStream = resp.getOutputStream()) {
                ByteStreams.copy(serviceResultStream, responseOutStream);
                responseOutStream.close();
            }
        }
    }

    private String getSingleParamValue(HttpServletRequest req, String param) throws IOException {
        // get id from query parameter
        final String[] idParam = req.getParameterValues(param);
        if (idParam == null || idParam.length == 0) {
            return null;
        } else {
            return idParam[0];
        }
    }
}
