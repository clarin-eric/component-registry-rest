package clarin.cmdi.componentregistry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBException;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.model.UserMapping;
import clarin.cmdi.componentregistry.model.UserMapping.User;

public class ComponentRegistryFactory {

    public static final String ANONYMOUS_USER = "anonymous"; //Default shibboleth fallback.
    private final static Logger LOG = LoggerFactory.getLogger(ComponentRegistryFactory.class);
    private static final ComponentRegistryFactory INSTANCE = new ComponentRegistryFactory();

    private UserMapping userMap = null;

    private ComponentRegistryImpl publicRegistry = new ComponentRegistryImpl(true);
    private Map<String, ComponentRegistry> registryMap = new ConcurrentHashMap<String, ComponentRegistry>();

    private ComponentRegistryFactory() {
        init();
    }

    private void init() {
        publicRegistry.setResourceConfig(Configuration.getInstance().getPublicResourceConfig());
        try {
            loadUserMap();
        } catch (IOException e) {
            throw new RuntimeException("Cannot load userMap", e);
        } catch (JAXBException e) {
            throw new RuntimeException("Cannot load userMap", e);
        }
    }

    private void loadUserMap() throws IOException, JAXBException {
        File userDirMappingFile = Configuration.getInstance().getUserDirMappingFile();
        if (userDirMappingFile.exists()) {
            userMap = MDMarshaller.unmarshal(UserMapping.class, userDirMappingFile, null);
        } else {
            userMap = new UserMapping();
        }
    }

    void reset() {
        registryMap = new ConcurrentHashMap<String, ComponentRegistry>();
        userMap = new UserMapping();
        publicRegistry = new ComponentRegistryImpl(true);
    }

    public static ComponentRegistryFactory getInstance() {
        return INSTANCE;
    }

    public ComponentRegistry getPublicRegistry() {
        return publicRegistry;
    }

    public synchronized ComponentRegistry getComponentRegistry(boolean userspace, UserCredentials credentials) {
        ComponentRegistry result = null;
        if (userspace) {
            if (credentials != null && !ANONYMOUS_USER.equals(credentials.getPrincipalName())) {
                String principalName = credentials.getPrincipalName();
                String userDir = getOrCreateUserDir(principalName, credentials.getDisplayName()); //TODO PD Need to get real displayname here.
                result = loadWorkspace(principalName, userDir);
            } else {
                throw new IllegalArgumentException("No user credentials available cannot load userspace.");//TODO Patrick make better error maybe some 403 or something so user knows a little better what is going on.
            }
        } else {
            result = getPublicRegistry();
        }
        return result;
    }

    private synchronized ComponentRegistry loadWorkspace(String name, String userDir) {
        ComponentRegistry result;
        result = registryMap.get(userDir);
        if (result == null) {
            LOG.info("Loading workspace for user: " + name + " workspace name: " + userDir);
            result = createNewUserRegistry(userDir);
            registryMap.put(userDir, result);
        }
        return result;
    }

    public synchronized ComponentRegistry getOtherUserComponentRegistry(Principal adminPrincipal, String principalNameMD5) {
        User user = getUserDir(principalNameMD5);
        ComponentRegistry result = null;
        if (user != null) {
            if (Configuration.getInstance().isAdminUser(adminPrincipal)) {
                result = loadWorkspace(adminPrincipal.getName(), user.getUserDir());
            } else {
                throw new IllegalArgumentException("User is not admin user cannot load userspace.");
            }
        } else {
            result = getPublicRegistry();
        }
        return result;
    }

    private ComponentRegistry createNewUserRegistry(String user) {
        ComponentRegistryImpl result = new ComponentRegistryImpl(false);
        ResourceConfig config = new ResourceConfig();
        File userResourceDir = new File(Configuration.getInstance().getRegistryRoot(), ResourceConfig.USERS_DIR_NAME + File.separator
                + user);
        config.setResourceRoot(userResourceDir);
        config.init();
        result.setResourceConfig(config);
        return result;
    }

    private User getUserDir(String principalNameMD5) {
        UserMapping.User user = userMap.findUser(principalNameMD5);
        return user;
    }

    String getOrCreateUserDir(String principalName, String displayName) {
        if (principalName == null) {
            return null;
        }
        UserMapping.User user = getUserDir(DigestUtils.md5Hex(principalName));
        if (user == null) {
            user = new UserMapping.User();
            user.setPrincipalName(principalName);
            user.setName(displayName);
            user.setUserDir(ResourceConfig.USER_DIR_PREFIX + userMap.getUsers().size());
            userMap.addUsers(user);
            saveUserMap();
        }
        return user.getUserDir();
    }
    
    @Deprecated
    public UserMapping getUserMap() {
        return userMap;
    }

    private void saveUserMap() {
        try {
            MDMarshaller.marshal(userMap, new FileOutputStream(Configuration.getInstance().getUserDirMappingFile()));
        } catch (IOException e) {//Manual intervention is probably needed so just throwing RuntimeExceptions if we cannot save the mapping we cannot do a lot so that needs to be addressed asap.
            throw new RuntimeException("Cannot save userMapping.", e);
        } catch (JAXBException e) {
            throw new RuntimeException("Cannot save userMapping.", e);
        }
    }

}
