package clarin.cmdi.componentregistry.servlet;

import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.skosmos.SkosmosService;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
public class VocabularyServiceServlet extends SkosmosServiceServlet {
    
    private static final long serialVersionUID = 1L;
    private final static Logger logger = LoggerFactory.getLogger(VocabularyServiceServlet.class);

    // Paths
    private final static String VOCABS_PATH = "/vocabularies";
    private static final String VOCAB_PAGE_PATH = "/page";
    private static final String VOCAB_ITEMS_PATH = "/items";

    //Parameters
    private static final String PARAM_URI = "uri";
    
    @Override
    public void init(ServletConfig servletConf) throws ServletException {
        super.init(servletConf);        
        
        
        //apply include/exclude configuration for skosmos schemes and vocabs
        final SkosmosService service = getSkosmosService();
        final Configuration config = getConfiguration();
        service.setIncludedSchemes(config.getIncludedSchemesForVocabularies());
        service.setExcludedSchemes(config.getExcludedSchemesForVocabularies());
        service.setIncludedVocabs(config.getIncludedVocabsForVocabularies());
        service.setExcludedVocabs(config.getExcludedVocabsForVocabularies());
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
    
    private void serveVocabularies(HttpServletRequest servletRequest, HttpServletResponse resp) {
        logger.debug("Retrieving information from service");
        final List<Object> infos
                = getSkosmosService().getConceptSchemeUriMap()
                        //= dummySchemeVocabMap()
                        .keySet()
                        .stream()
                        .map(uri -> getSkosmosService().getConceptSchemeInfo(uri))
                        //.map(info -> JsonLdProcessor.fromRDF(info, options))
                        .collect(Collectors.toList());
        
        logger.debug("Constructing response");
        // make aggregated JSON-LD output
        //final Map context = new HashMap();
        final JsonLdOptions options = new JsonLdOptions();
        //final Object result = JsonLdProcessor.compact(infos, context, options);
        final Object result = JsonLdProcessor.flatten(infos, options);

        // write response
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setHeader("Content-Type", CONTENT_TYPE_HEADER_VALUE_JSON);
        try (OutputStreamWriter writer = new OutputStreamWriter(resp.getOutputStream())) {
            JsonUtils.write(writer, result);
        } catch (IOException ex) {
            logger.error("Error while writing to response stream", ex);
        }
        
        logger.debug("Done");
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
        final String schemeUri = getSingleParamValue(req, PARAM_URI);
        logger.debug("Retrieving information from service");
        if (schemeUri == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing query parameter: " + PARAM_URI);
        } else if (!getSkosmosService().getConceptSchemeUriMap().containsKey(schemeUri)) {
            logger.debug("Requested scheme URI not found ({}), sending 404 response", schemeUri);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No scheme with URI " + schemeUri);
        } else {
            final List<Object> infos = getSkosmosService().getConceptsInScheme(schemeUri);
            
            logger.debug("Constructing response");
            // make aggregated JSON-LD output
            //final Map context = new HashMap();
            final JsonLdOptions options = new JsonLdOptions();
            //final Object result = JsonLdProcessor.compact(infos, context, options);
            final Object result = JsonLdProcessor.flatten(infos, options);

            // write response
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setHeader("Content-Type", CONTENT_TYPE_HEADER_VALUE_JSON);
            try (OutputStreamWriter writer = new OutputStreamWriter(resp.getOutputStream())) {
                JsonUtils.write(writer, result);
            } catch (IOException ex) {
                logger.error("Error while writing to response stream", ex);
            }
            
            logger.debug("Done");
        }
    }
    
    private void serveVocabularyPage(HttpServletRequest req, HttpServletResponse resp) throws IllegalArgumentException, UriBuilderException, IOException {
        final String schemeUri = getSingleParamValue(req, PARAM_URI);
        if (schemeUri == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing query parameter: " + PARAM_URI);
        } else {
            final Collection<String> vocabUris = getSkosmosService().getConceptSchemeUriMap().get(schemeUri);
            if (vocabUris.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
            } else {
                final String vocabUri = vocabUris.iterator().next(); // skip any additional vocabs registered this scheme
                // redirect client to the right page at the service                
                resp.setStatus(HttpServletResponse.SC_SEE_OTHER);
                resp.sendRedirect(resp.encodeRedirectURL(getVocabPageUri(vocabUri).toString()));
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
