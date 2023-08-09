package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.Owner;
import clarin.cmdi.componentregistry.OwnerUser;
import clarin.cmdi.componentregistry.RegistrySpace;
import clarin.cmdi.componentregistry.UserCredentials;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

/**
 * Implementation of ComponentRegistryFactory that uses the
 * ComponentRegistryDbImpl implementation of ComponentRegistry for accessing the
 * registry
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author George.Georgovassilis@mpi.nl
 */
public class ComponentRegistryFactoryDbImpl implements ComponentRegistryFactory {

    private final static Logger LOG = LoggerFactory
            .getLogger(ComponentRegistryFactoryDbImpl.class);
    @Autowired
    private Configuration configuration;
    @Autowired
    private ComponentRegistryBeanFactory componentRegistryBeanFactory;
    @Autowired
    private UserDao userDao;
    private ComponentRegistry publicComponentRegistry = null;

    @Override
    public List<ComponentRegistry> getAllUserRegistries() {
        // TODO: this probably could use some caching
        try {
            List<RegistryUser> users = userDao.getAllUsers();
            List<ComponentRegistry> registries = new ArrayList<ComponentRegistry>();
            for (RegistryUser user : users) {
                registries.add(getNewComponentRegistryForUser(user.getId()));
            }
            return registries;
        } catch (DataAccessException ex) {
            LOG.error("Could not retrieve users", ex);
            throw ex;
        }
    }

    @Override
    public ComponentRegistry getComponentRegistry(RegistrySpace space,
            Owner owner, UserCredentials credentials, Number groupId)
            throws UserUnauthorizedException {

        switch (space) {
            case PRIVATE:
                return this.getPrivateRegistry(owner, credentials);
            case PUBLISHED:
                return this.getPublicRegistry();
            case GROUP:
                return this.getGroupRegistry(groupId, owner, credentials);
            default:
                // TODO: Add support for "review" types
                throw new UnsupportedOperationException(
                        "Unsupported component space" + space);
        }
    }

    private ComponentRegistry getGroupRegistry(Number groupId, Owner owner, UserCredentials credentials) {
        if (owner == null || owner instanceof OwnerUser) {
            RegistryUser user = this.getOrCreateUser(credentials);
            owner = new OwnerUser(user.getId());
        }
        ComponentRegistry cr = componentRegistryBeanFactory.getNewComponentRegistry();// default public registry
        cr.setRegistrySpace(RegistrySpace.GROUP);
        cr.setRegistryOwner(owner);
        cr.setGroupId(groupId);
        return cr;
    }

    private ComponentRegistry getPrivateRegistry(Owner owner,
            UserCredentials credentials) throws IllegalArgumentException,
            DataAccessException, UserUnauthorizedException {
        if (owner == null || owner instanceof OwnerUser) {
            RegistryUser user = getOrCreateUser(credentials);
            if (user != null) {
                if (owner != null && !user.getId().equals(owner.getId())) {
                    throw new UserUnauthorizedException(
                            "User cannot access other user's private registry");
                }

                try {
                    return this.getNewComponentRegistryForUser(user.getId());
                } catch (DataAccessException ex) {
                    LOG.error("Could not retrieve or create user", ex);
                    throw ex;
                }
            } else {
                throw new UserUnauthorizedException(
                        "No user credentials available cannot load userspace.");
            }
        } else {
            // TODO: Support group owners
            throw new UnsupportedOperationException(
                    "Group owners not supported");
        }
    }

    @Override
    public ComponentRegistry getOtherUserComponentRegistry(
            Principal adminPrincipal, Owner owner) {
        try {
            Optional<RegistryUser> user;
            if (owner instanceof OwnerUser) {
                user = userDao.findById(owner.getId().longValue());
            } else {
                // TODO: Implement for groups ??? Twan
                throw new UnsupportedOperationException(
                        "Groups not implemented yet");
            }
            ComponentRegistry result = null;
            if (user.isPresent()) {
                if (configuration.isAdminUser(adminPrincipal)) {
                    result = this.getNewComponentRegistryForUser(user.get().getId());
                } else {
                    LOG.info("{} not found in list of {}",
                            adminPrincipal.getName(),
                            configuration.getAdminUsersArray().length);
                    throw new IllegalArgumentException("User "
                            + adminPrincipal.getName()
                            + " is not admin user cannot load userspace.");
                }
            }
            return result;
        } catch (DataAccessException ex) {
            LOG.error("Could not retrieve user by id", ex);
            throw ex;
        }
    }

    @Override
    public ComponentRegistry getPublicRegistry() {
        if (publicComponentRegistry == null) {
            publicComponentRegistry = this.getNewComponentRegistryForUser(null);
        }
        return publicComponentRegistry;
    }

    private ComponentRegistry getNewComponentRegistryForUser(Number userId) {
        ComponentRegistry componentRegistry = componentRegistryBeanFactory.getNewComponentRegistry();
        if (userId != null) {
            OwnerUser ou = new OwnerUser(userId);
            componentRegistry.setRegistrySpace(RegistrySpace.PRIVATE);
            componentRegistry.setRegistryOwner(ou);
            componentRegistry.setGroupId(null);
        }
        return componentRegistry;
    }

    @Override
    public ComponentRegistry getBaseRegistry(UserCredentials credentials) {
        RegistryUser user = this.getOrCreateUser(credentials);
        ComponentRegistry componentRegistryDbImpl = componentRegistryBeanFactory.getNewComponentRegistry();// default public registry
        if (user != null) {
            Number userId = user.getId();
            Owner owner = new OwnerUser(userId);
            componentRegistryDbImpl.setRegistrySpace(null);
            componentRegistryDbImpl.setRegistryOwner(owner);
            componentRegistryDbImpl.setGroupId(null);
        }
        return componentRegistryDbImpl;
    }

    @Override
    public RegistryUser getOrCreateUser(UserCredentials credentials) {
        if (credentials != null
                && !ANONYMOUS_USER.equals(credentials.getPrincipalName())) {
            String principalName = credentials.getPrincipalName();
            return this.getOrCreateUser(principalName, credentials.getDisplayName());
        }
        return null;
    }

    private synchronized RegistryUser getOrCreateUser(String principalName,
            String displayName) {
        // Try getting it from db
        RegistryUser user = userDao.getByPrincipalName(principalName);
        if (user == null) {
            LOG.info(
                    "Request to create user with principal name {} and display name {}",
                    new Object[]{principalName, displayName});
            // Create the new user
            user = new RegistryUser();
            user.setPrincipalName(principalName);
            user.setName(displayName);
            userDao.saveAndFlush(user);
            // Retrieve from db
            user = userDao.getByPrincipalName(principalName);
        }
        return user;
    }
}
