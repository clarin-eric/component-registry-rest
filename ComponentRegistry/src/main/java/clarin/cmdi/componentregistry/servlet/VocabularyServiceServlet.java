package clarin.cmdi.componentregistry.servlet;

import clarin.cmdi.componentregistry.Configuration;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import org.glassfish.jersey.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

    /**
     * Vocabulary service path on external service
     */
    private static final String VOCABULARY_SERVICE_PATH = "conceptscheme";
    private static final String VOCABULARY_PAGE_SERVICE_PATH_FORMAT = VOCABULARY_SERVICE_PATH + "/%s"; //placeholder for id

    private static final String QUERY_PARAMETER_FORMAT = "format";

    /**
     * Service to retrieve vocabulary items as concepts
     */
    private static final String VOCABULARY_ITEMS_PATH = "find-concepts";
    private static final String VOCABULARY_ITEMS_PARAM_QUERY = "q";
    private static final String VOCABULARY_ITEMS_QUERY_ALL_VALUE = "uri:*";

    private static final String VOCABULARY_ITEMS_PARAM_ROWS = "rows";
    private static final String ITEM_RETRIEVAL_BATCH_SIZE = "500";

    private static final String VOCABULARY_ITEMS_PARAM_OFFSET = "start";

    private static final String VOCABULARY_ITEMS_PARAM_CONCEPT_SCHEME = "conceptScheme";
    private static final String VOCABULARY_ITEMS_PARAM_FIELDS = "fl";

    /**
     * Vocabulary service path on this service
     */
    private final static String CONCEPT_SCHEMES_PATH = "/conceptscheme";
    /**
     * Vocabulary page redirect path on this service
     */
    private static final String VOCAB_PAGE_PATH = "/conceptscheme/vocabpage";

    private static final String VOCAB_ITEMS_PATH = "/items";

    private static final String PARAM_ID = "id";
    private static final String PARAM_SCHEME_URI = "scheme";
    private static final String PARAM_FIELDS = "fields";
    private static final String PARAM_MAX_RESULTS = "maxResults";

    private transient WebTarget service;
    private URI serviceUri;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        this.serviceUri = UriBuilder.fromUri(Configuration.getInstance().getClavasRestUrl()).build();
        service = ClientBuilder.newClient().target(serviceUri);

        logger.info("Instantiated vocabulary servlet on URI {}", serviceUri);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String path = req.getPathInfo();
        if (path != null) {
            if (path.equals(CONCEPT_SCHEMES_PATH)) {
                //proxy the response (list of concept schemes (vocabularies)) from the original service
                serveConceptSchemes(req, resp);
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
            //TODO: else if path is '/items'...
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
    }

    private void serveConceptSchemes(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        WebTarget serviceReq = service.path(VOCABULARY_SERVICE_PATH);

        //forward query params to service request
        final Map<String, String[]> params = req.getParameterMap();
        if (params != null) {
            for (Map.Entry<String, String[]> param : params.entrySet()) {
                for (String value : param.getValue()) {
                    serviceReq = serviceReq.queryParam(param.getKey(), value);
                }
            }
        }

        //set request format according to Accept header (if no explicit 'format' in request)
        if (params == null || !params.containsKey(QUERY_PARAMETER_FORMAT)) {
            serviceReq = setFormatFromAcceptHeader(req, serviceReq);
        }

        logger.debug("Forwarding vocabulary service request to {}", serviceReq.toString());
        forwardResponse(serviceReq, resp);
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
        final String schemeId = getSingleParamValue(req, PARAM_SCHEME_URI);
        if (schemeId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "concept scheme URI for vocabulary must be provided via '" + PARAM_SCHEME_URI + "' query parameter");
            return;
        }

        WebTarget baseRequest = service.path(VOCABULARY_ITEMS_PATH)
                .queryParam(VOCABULARY_ITEMS_PARAM_QUERY, VOCABULARY_ITEMS_QUERY_ALL_VALUE)
                .queryParam(VOCABULARY_ITEMS_PARAM_CONCEPT_SCHEME, schemeId)
                .queryParam(QUERY_PARAMETER_FORMAT, "json");

        final String fields = getSingleParamValue(req, PARAM_FIELDS);
        if (fields != null) {
            baseRequest = baseRequest.queryParam(VOCABULARY_ITEMS_PARAM_FIELDS, fields);
        }

        final Integer maxResults;
        final String maxResultsParam = getSingleParamValue(req, PARAM_MAX_RESULTS);
        if (maxResultsParam != null) {
            maxResults = Integer.valueOf(maxResultsParam);
        } else {
            maxResults = null;
        }

        //keep some stats on the retrieved records (service will not necessarily return all at once)
        int target = 0;
        long lastFetchSize = 0;
        List results = null;
        //start fetch loop
        do {
            //continue where we left off
            final String offset = Integer.toString(results == null ? 0 : results.size());
            logger.trace("Getting items starting at {}", offset);

            final String rows = (maxResults == null) ? ITEM_RETRIEVAL_BATCH_SIZE : maxResults.toString();
            logger.trace("Getting {} items at a time", rows);

            final WebTarget baseRequestTarget = baseRequest
                    .queryParam(VOCABULARY_ITEMS_PARAM_OFFSET, offset)
                    .queryParam(VOCABULARY_ITEMS_PARAM_ROWS, rows);
            logger.debug("Retrieving items from {}", baseRequestTarget);
            final ClientResponse itemsResponse = baseRequestTarget.request(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            final int responseStatus = itemsResponse.getStatus();
            if (responseStatus >= 200 && responseStatus < 300) {
                try {
                    final JSONObject responseObject = new JSONObject(itemsResponse.readEntity(String.class));
                    final JSONObject response = responseObject.getJSONObject("response");
                    if (response == null) {
                        logger.warn("Structure of find-concepts service not as expected - did not find /response");
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        return;
                    }

                    //get total results count
                    target = response.getInt("numFound");
                    if (results == null) {
                        results = new ArrayList(target);
                    }

                    //get documents
                    final JSONArray docs = response.getJSONArray("docs");
                    if (docs == null) {
                        logger.warn("Structure of find-concepts service not as expected - did not find array at /response/docs");
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        return;
                    }

                    lastFetchSize = docs.length();
                    //add documents to result collection
                    for (int i = 0; i < lastFetchSize; i++) {
                        results.add(docs.get(i));
                    }
                } catch (JSONException ex) {
                    throw new RuntimeException("Could not retrieve items", ex);
                }
            } else {
                logger.warn("Response code {} when requesting vocabulary items", responseStatus);
                resp.setStatus(responseStatus);
                return;
            }
        } while (results.size() < target && (maxResults == null || results.size() < maxResults) && lastFetchSize > 0); //continue unless we have a complete result or retreived nothing last time

        logger.debug("Retrieved {} items", results.size());

        //turn back into a single JSON array
        final JSONArray docs = new JSONArray(results);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json;charset=UTF-8");
        try (Writer writer = new OutputStreamWriter(resp.getOutputStream(), "UTF-8")) {
            writer.write(docs.toString());
        }
    }

    private WebTarget setFormatFromAcceptHeader(HttpServletRequest req, WebTarget serviceReq) {
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
                        .path(String.format(VOCABULARY_PAGE_SERVICE_PATH_FORMAT, id))
                        .build().toString());
        // append request format specifier depending on accept header
        final String acceptHeader = req.getHeader("Accept");
        if (acceptHeader != null) {
            if (acceptHeader.contains(MediaType.APPLICATION_JSON)) {
                targetUriBuilder.append(".json");
                //forward JSON content
                final Client client = ClientBuilder.newClient();
                WebTarget target = client.target(targetUriBuilder.toString());
                forwardResponse(target, resp);
                return;
            } else if (acceptHeader.contains(MediaType.TEXT_HTML)) {
                targetUriBuilder.append(".html");
            }
        }
        // redirect to page at service (all cases except for JSON)
        resp.setStatus(HttpServletResponse.SC_SEE_OTHER);
        resp.sendRedirect(resp.encodeRedirectURL(targetUriBuilder.toString()));
    }

    private void forwardResponse(WebTarget target, HttpServletResponse resp) throws IOException {
        //make GET request and copy directly to response stream
        final Invocation.Builder request = target.request(MediaType.APPLICATION_JSON_TYPE);
        final Response response = request.get();
        //also forward content type header
        resp.setContentType(response.getHeaders().getFirst("content-type").toString());
        final InputStream get = response.readEntity(InputStream.class);
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
