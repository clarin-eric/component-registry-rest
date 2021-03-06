package clarin.cmdi.componentregistry.impl;

import clarin.cmdi.componentregistry.CmdVersion;
import java.io.Writer;
import clarin.cmdi.componentregistry.ComponentSpecConverter;
import eu.clarin.cmdi.toolkit.CMDToolkit;
import java.io.InputStream;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
@Service("ComponentSpecConverter")
public class ComponentSpecConverterImpl implements ComponentSpecConverter {

    private final static Logger logger = LoggerFactory.getLogger(ComponentSpecConverterImpl.class);

    private final Templates Cmdi1_1To1_2Templates;
    private final Templates Cmdi1_2To1_1Templates;

    public ComponentSpecConverterImpl() throws TransformerConfigurationException {
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        this.Cmdi1_1To1_2Templates = transformerFactory.newTemplates(
                new StreamSource(
                        CMDToolkit.class.getResourceAsStream(CMDToolkit.XSLT_COMPONENT_UPGRADE)));
        this.Cmdi1_2To1_1Templates = transformerFactory.newTemplates(
                new StreamSource(
                        CMDToolkit.class.getResourceAsStream(CMDToolkit.XSLT_COMPONENT_DOWNGRADE)));
    }

    @Override
    public void convertComponentSpec(CmdVersion sourceVersion, CmdVersion targetVersion, InputStream specStream, Writer writer) {
        switch (sourceVersion) {
            case CMD_1_1:
                convert1_1Source(targetVersion, specStream, writer);
                return;
            case CMD_1_2:
                convert1_2Source(targetVersion, specStream, writer);
                return;
        }
        logger.warn("Requested conversion from {} to {}, unsupported", sourceVersion, targetVersion);
    }

    private void convert1_1Source(CmdVersion targetVersion, InputStream specStream, Writer writer) {
        if (targetVersion == CmdVersion.CMD_1_2) {
            convert(Cmdi1_1To1_2Templates, specStream, writer);
        } else {
            logger.warn("Requested conversion from 1.1 to {}, unsupported", targetVersion);
        }
    }

    private void convert1_2Source(CmdVersion targetVersion, InputStream specStream, Writer writer) {
        if (targetVersion == CmdVersion.CMD_1_1) {
            convert(Cmdi1_2To1_1Templates, specStream, writer);
        } else {
            logger.warn("Requested conversion from 1.2 to {}, unsupported", targetVersion);
        }
    }

    private void convert(final Templates templates, InputStream specStream, Writer writer) {
        try {
            final Transformer transformer = templates.newTransformer();
            transformer.transform(new StreamSource(specStream), new StreamResult(writer));

        } catch (TransformerException ex) {
            logger.error("Could not carry out transformation for conversion", ex);
        }
    }

}
