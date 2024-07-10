package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.AuthenticationRequiredException;
import java.io.IOException;
import java.security.Principal;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.IMarshaller;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.ShhaaUserCredentials;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.ComponentSpec;
import clarin.cmdi.componentregistry.frontend.CMDItemInfo;
import clarin.cmdi.componentregistry.frontend.SubmitFailedException;
import clarin.cmdi.componentregistry.BaseDescription;
import clarin.cmdi.componentregistry.ComponentDescription;
import clarin.cmdi.componentregistry.ProfileDescription;
import clarin.cmdi.componentregistry.persistence.ComponentDao;

/**
 *
 * @author george.georgovassilis@mpi.nl
 *
 */
public class AdminRegistry {

    private final static Logger LOG = LoggerFactory
            .getLogger(AdminRegistry.class);
    private ComponentRegistryFactory componentRegistryFactory;
    private ComponentDao componentDao;
    private IMarshaller marshaller;

    public void setComponentRegistryFactory(
            ComponentRegistryFactory componentRegistryFactory) {
        this.componentRegistryFactory = componentRegistryFactory;
    }

    public void setComponentDao(ComponentDao componentDao) {
        this.componentDao = componentDao;
    }

    public void setMarshaller(IMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    public void submitFile(CMDItemInfo info, Principal userPrincipal, boolean publish)
            throws SubmitFailedException, UserUnauthorizedException {
        if (userPrincipal == null) {
            LOG.info("Null user principal, nothings is submitted.");
            return;
        }

        try {
            BaseDescription originalDescription = info.getDataNode()
                    .getDescription();
            BaseDescription description = null;
            ComponentSpec spec = null;
            if (originalDescription.isProfile()) {
                description = marshaller.unmarshal(ProfileDescription.class,
                        IOUtils.toInputStream(info.getDescription(), "UTF-8"),
                        null);
            } else {
                description = marshaller.unmarshal(ComponentDescription.class,
                        IOUtils.toInputStream(info.getDescription(), "UTF-8"),
                        null);
            }
            spec = marshaller.unmarshal(ComponentSpec.class,
                    IOUtils.toInputStream(info.getContent(), "UTF-8"), null);
            checkId(originalDescription.getId(), description.getId());
            ComponentRegistry cr = this.getRegistry(new ShhaaUserCredentials(userPrincipal));
            int result;
            if (publish) {
                description.setPublic(true);
                result = cr.publish(description, spec, userPrincipal);
            } else {
                result = cr.update(description, spec, info.isForceUpdate());
            }
            if (result < 0) {
                throw new SubmitFailedException(
                        "Problem occured while registering, please check the tomcat logs for errors.");
            }
        } catch (JAXBException e) {
            throw new SubmitFailedException(e);
        } catch (ItemNotFoundException e) {
            throw new SubmitFailedException(e);
        } catch (AuthenticationRequiredException e) {
            throw new SubmitFailedException(e);
        }
    }

    private void checkId(String id, String id2) throws SubmitFailedException {
        if (id == null || id2 == null || !id.equals(id2)) {
            throw new SubmitFailedException(
                    "Id's do not match up, you cannot edit id's: id1=" + id
                    + ", id2=" + id2);
        }
    }

    public void undelete(CMDItemInfo info) throws SubmitFailedException {
        BaseDescription desc = info.getDataNode().getDescription();
        try {
            componentDao.setDeleted(desc, false);
        } catch (DataAccessException e) {
            throw new SubmitFailedException("Undelete failed", e);
        }
    }

    public void delete(CMDItemInfo info, Principal userPrincipal)
            throws SubmitFailedException {
        String id = info.getName();
        BaseDescription desc = info.getDataNode().getDescription();
        try {
            this.deleteFromRegistry(userPrincipal, desc, info);
            LOG.info("Deleted item: " + id);
        } catch (IOException e) {
            throw new SubmitFailedException(e);
        } catch (UserUnauthorizedException e) {
            throw new SubmitFailedException(e);
        } catch (DeleteFailedException e) {
            throw new SubmitFailedException(e);
        } catch (ComponentRegistryException e) {
            throw new SubmitFailedException(e);
        } catch (ItemNotFoundException e) {
            throw new SubmitFailedException(e);
        } catch (AuthenticationRequiredException e) {
            throw new SubmitFailedException(e);
        }

    }

    private void deleteFromRegistry(Principal userPrincipal,
            BaseDescription desc, CMDItemInfo info) throws IOException,
            UserUnauthorizedException, ComponentRegistryException, ItemNotFoundException, AuthenticationRequiredException {

        if (userPrincipal == null) {
            LOG.info("Bnull user principal, nothing is deleted");
            return;
        }

        ComponentRegistry registry = this.getRegistry(new ShhaaUserCredentials(userPrincipal));
        LOG.info("Deleting item: " + desc);
        if (desc.isProfile()) {
            registry.deleteMDProfile(desc.getId());
        } else {
            registry.deleteMDComponent(desc.getId(), info.isForceUpdate());
        }
    }

    private ComponentRegistry getRegistry(ShhaaUserCredentials credentials) throws UserUnauthorizedException {
        return componentRegistryFactory.getBaseRegistry(credentials);
    }

}
