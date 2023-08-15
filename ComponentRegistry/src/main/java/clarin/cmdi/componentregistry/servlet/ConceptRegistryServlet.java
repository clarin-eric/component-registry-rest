package clarin.cmdi.componentregistry.servlet;

import clarin.cmdi.componentregistry.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
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
public class ConceptRegistryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final static Logger logger = LoggerFactory.getLogger(ConceptRegistryServlet.class);
    private static final String CCR2DCIF_XSL_RESOURCE = "/ccr2dcif.xsl";

    private transient WebTarget service;
    private Templates ccr2dcifTemplates;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        final URI uri = UriBuilder.fromUri(Configuration.getInstance().getCcrRestUrl()).build();
        final Client client = ClientBuilder.newClient();
        service = client.target(uri);

        try {
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Source ccr2dcifSource = new StreamSource(getClass().getResourceAsStream(CCR2DCIF_XSL_RESOURCE));
            ccr2dcifTemplates = transformerFactory.newTemplates(ccr2dcifSource);
        } catch (TransformerConfigurationException ex) {
            throw new ServletException("Could not initialise CCR to DCIF transformation templates", ex);
        }

        logger.info("Instantiated CCR servlet on URI {}", uri);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // Set keywords
        final String keywords = req.getParameter("keywords");
        logger.debug("ISOcat request: keywords = {}", keywords);

        //TODO: Scope by concept scheme?
        //TODO: Set fields to be returned (fl=...)
        final WebTarget requestResource = service.path("find-concepts").queryParam("q", keywords);
        logger.debug("Forwarding CCR request to {}", requestResource.getUri());

        // Send request to CCR
        final InputStream result = requestResource.request(MediaType.APPLICATION_XML).get(InputStream.class);
        logger.debug("CCR result: {}", result);

        final String acceptHeader = req.getHeader("Accept");
        if (acceptHeader.contains(MediaType.APPLICATION_JSON)) {
            serveJSON(resp, result);
        } else {
            serveDCIF(resp, result);
        }
    }

    private void serveDCIF(HttpServletResponse resp, final InputStream ccrResponse) throws IOException, ServletException {
        // Prepare response
        resp.setContentType(MediaType.APPLICATION_XML);
        resp.setCharacterEncoding("UTF-8");

        try {
            logger.debug("Transforming CCR response to response writer");
            transformCCRResponse(ccrResponse, new StreamResult(resp.getWriter()));
        } finally {
            ccrResponse.close();
            resp.flushBuffer();
        }
    }

    private void serveJSON(HttpServletResponse resp, final InputStream ccrResponse) throws IOException, ServletException {
        // Prepare response
        resp.setContentType(MediaType.APPLICATION_JSON);
        resp.setCharacterEncoding("UTF-8");

        try {
            logger.debug("Transforming CCR response to JSON");
            // Make DCIF XML
            final StringWriter xmlWriter = new StringWriter();
            transformCCRResponse(ccrResponse, new StreamResult(xmlWriter));

            // convert to JSON
            logger.debug("Converting DCIF XML to JSON");
            final JSONObject dcifJson = XML.toJSONObject(xmlWriter.toString());

            // extract Concept objects (strip off envelope)
            final JSONObject dcSelection = dcifJson.getJSONObject("dcif:dataCategorySelection");
            final JSONArray dcArray;
            if (dcSelection.has("dcif:dataCategory")) {
                final JSONArray optJSONArray = dcSelection.optJSONArray("dcif:dataCategory");
                if (optJSONArray == null) {
                    //single result, must be a JSON object so wrap that in array
                    dcArray = new JSONArray();
                    dcArray.put(dcSelection.getJSONObject("dcif:dataCategory"));
                } else {
                    dcArray = optJSONArray;
                }
            } else {
                // no DCs, return an empty array
                dcArray = new JSONArray();
            }
            // send back to client
            resp.getWriter().write(dcArray.toString(4));
        } catch (JSONException ex) {
            throw new ServletException("Could not create JSON object", ex);
        } finally {
            ccrResponse.close();
            resp.flushBuffer();
        }
    }

    private void transformCCRResponse(final InputStream ccrResponse, final Result result) throws IOException, ServletException {
        try {
            final Transformer transformer = ccr2dcifTemplates.newTransformer();
            final Source source = new StreamSource(ccrResponse);
            transformer.transform(source, result);
        } catch (TransformerConfigurationException ex) {
            throw new ServletException("Could not configure transformer for converting CCR output to DCIF", ex);
        } catch (TransformerException ex) {
            throw new ServletException("Could not transform CCR output to DCIF", ex);
        }
    }
}
