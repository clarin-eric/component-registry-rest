package clarin.cmdi.componentregistry.servlet;

import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.skosmos.SkosmosService;
import com.google.common.io.ByteStreams;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterface;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
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
    private static final String DEFAULT_VOCAB_LANG = "en";

    /**
     * Vocabulary service path on this service
     */
    private final static String VOCABS_PATH = "/vocabularies";

    /**
     * Vocabulary page redirect path on this service
     */
    private static final String VOCAB_PAGE_PATH = "/page";

    /**
     * Items path on this service
     */
    private static final String VOCAB_ITEMS_PATH = "/items";
    private SkosmosService skosmosService;
    

    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.skosmosService = new SkosmosService(UriBuilder.fromUri(Configuration.getInstance().getClavasRestUrl()).build());
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
        
    }
    
    private void serveVocabularyPage(HttpServletRequest req, HttpServletResponse resp) throws IllegalArgumentException, UriBuilderException, IOException {
        
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
    
    private WebResource copyRequestParams(final Map<String, String[]> paramsMap, WebResource serviceReq) {
        if (paramsMap != null) {
            for (Map.Entry<String, String[]> param : paramsMap.entrySet()) {
                for (String value : param.getValue()) {
                    serviceReq = serviceReq.queryParam(param.getKey(), value);
                }
            }
        }
        return serviceReq;
    }
}
