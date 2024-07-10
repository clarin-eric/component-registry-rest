package clarin.cmdi.componentregistry.impl;

import clarin.cmdi.componentregistry.AuthenticationRequiredException;
import clarin.cmdi.componentregistry.CmdVersion;
import static clarin.cmdi.componentregistry.CmdVersion.CANONICAL_CMD_VERSION;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.model.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ComponentSpecConverter;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.ComponentSpec;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.ComponentStatus;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;
import javax.xml.transform.TransformerException;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ComponentRegistryImplBase implements ComponentRegistry {

    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryImplBase.class);

    protected abstract MDMarshaller getMarshaller();

    protected abstract ComponentSpecConverter getSpecConverter();

    /**
     *
     * @return List of profile descriptions ordered by name ascending, only the
     * ones marked for showing in metadata editor
     * @throws ComponentRegistryException
     */
    @Override
    public List<ProfileDescription> getProfileDescriptionsForMetadaEditor(Set<ComponentStatus> statusFilter) throws ComponentRegistryException {
        // TODO: Below can also be done by accepting and passing a parameter in the ProfileDescriptionDaoImpl, should have *much* better performance

        // Get all profile descriptions
        List<String> descriptionsCollectionIds = getAllNonDeletedProfileIds(null, statusFilter);
        // Filter out ones that do should not be shown for metadata editor
        ArrayList<ProfileDescription> descriptions = new ArrayList<ProfileDescription>();
        for (String id : descriptionsCollectionIds) {
            try {
                ProfileDescription profile = getProfileDescriptionAccessControlled(id);
                if (profile.isShowInEditor()) {
                    descriptions.add(profile);
                }
            } catch (AuthenticationRequiredException e) {
            } catch (UserUnauthorizedException e) {
            } catch (ItemNotFoundException e) {
            }

        }
        // Return filtered list
        return descriptions;
    }

    /* HELPER METHODS */
    protected static String stripRegistryId(String id) {
        return StringUtils.removeStart(id, ComponentRegistry.REGISTRY_ID);
    }

    protected void syncSpecDescriptionHeaders(ComponentSpec spec, BaseDescription description) throws ComponentRegistryException {
        final ComponentSpec.Header compHeader = spec.getHeader();

        //description id overrules spec id
        compHeader.setID(description.getId());

        //other header fields form spec override description
        description.setDescription(compHeader.getDescription());
        description.setName(compHeader.getName());
        description.setDerivedfrom(compHeader.getDerivedFrom());
        description.setSuccessor(compHeader.getSuccessor());
        
        if(description.getName() == null || description.getDescription() == null) {
            throw new ComponentRegistryException("Name and description are required header fields");
        }

        //status
        try {
            //convert to enum value
            final ComponentStatus status = ComponentStatus.valueOf(compHeader.getStatus().toUpperCase());
            description.setStatus(status);
        } catch (IllegalArgumentException ex) {
            LOG.warn("Encountered invalid component status {} in {}", compHeader.getStatus(), compHeader.getID(), ex);
            throw new ComponentRegistryException("Encountered invalid component status " + compHeader.getStatus(), ex);
        }
    }

    protected void writeXsd(ComponentSpec expandedSpec, CmdVersion[] cmdVersions, OutputStream outputStream) throws JAXBException, TransformerException {
        getMarshaller().generateXsd(expandedSpec, cmdVersions, outputStream);
    }

    protected void writeXml(ComponentSpec spec, CmdVersion cmdVersion, OutputStream outputStream) {
        try {
            if (cmdVersion == CANONICAL_CMD_VERSION) {
                getMarshaller().marshal(spec, outputStream);
            } else {
                // If cmdVersion not current version convert
                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    //marshall to temporary byte array so that we get XML in the canonical version
                    getMarshaller().marshal(spec, os);

                    //turn marshaller output into converter input and write converted XML to output stream
                    final ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
                    final OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                    getSpecConverter().convertComponentSpec(CANONICAL_CMD_VERSION, cmdVersion, is, writer);
                }
            }

        } catch (UnsupportedEncodingException e) {
            LOG.error("Error in encoding: ", e);
        } catch (JAXBException | IOException e) {
            LOG.error("Cannot marshall spec: " + spec, e);
        }
    }

}
