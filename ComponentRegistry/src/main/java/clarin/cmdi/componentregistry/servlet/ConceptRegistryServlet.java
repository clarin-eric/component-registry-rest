package clarin.cmdi.componentregistry.servlet;

import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.skosmos.SkosmosService;
import com.github.jsonldjava.utils.JsonUtils;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Servlet responsible for providing a bridge to the CLARIN Concept Registry
 * (CCR) REST interface. Can be called by the front end to circumvent
 * cross-scripting and FLASH/Browser limitation in setting the headers of a
 * request.
 *
 */
public class ConceptRegistryServlet extends SkosmosServiceServlet {

    private static final long serialVersionUID = 1L;
    private final static Logger logger = LoggerFactory.getLogger(ConceptRegistryServlet.class);

    @Override
    public void init(ServletConfig servletConf) throws ServletException {
        super.init(servletConf);

        //apply include/exclude configuration for skosmos schemes and vocabs
        final SkosmosService service = getSkosmosService();
        final Configuration config = getConfiguration();
        service.setIncludedSchemes(config.getIncludedSchemesForConcepts());
        service.setIncludedVocabs(config.getIncludedVocabsForConcepts());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Set keywords
        final String keywords = req.getParameter("keywords");
        logger.debug("CCR request: keywords = {}", keywords);

        //final List<Object> concepts = getSkosmosService().searchConcepts(keywordToQuery(keywords));
        final Object concepts = getSkosmosService().searchConceptsJsonLd(keywordToQuery(keywords));

        if (concepts == null) {
            resp.sendError(500, "No response from CCR");
        } else {
            //logger.debug("CCR response: {} concepts", concepts.size());

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setHeader("Content-Type", CONTENT_TYPE_HEADER_VALUE_JSON);
            try (OutputStreamWriter writer = new OutputStreamWriter(resp.getOutputStream())) {
                JsonUtils.write(writer, concepts);
            } catch (IOException ex) {
                logger.error("Error while writing to response stream", ex);
            }
        }
    }

    protected static String keywordToQuery(final String keywords) {
        return "*" + keywords.replaceAll("\\*", "\\\\*") + "*";
    }
}
