package clarin.cmdi.componentregistry;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class Configuration {

    private final static Logger LOG = LoggerFactory.getLogger(Configuration.class);
    //NOTE: Default values, can be overwritten in applicationContext.xml
    private String toolkitLocation = "https://infra.clarin.eu/CMDI/1.x";
    private String generalComponentSchema = "https://infra.clarin.eu/CMDI/1.x/xsd/cmd-component.xsd";
    private String ccrRestUrl = "https://openskos.meertens.knaw.nl/ccr/api/";
    private String clavasRestUrl = "https://openskos.meertens.knaw.nl/clavas/api/";
    private Collection<String> adminUsers = new HashSet<String>();
    private List<String> displayNameShibbolethKeys = new ArrayList<String>();
    private long skosmosCacheRefreshRateSeconds = 3600;

    {//Default values
        displayNameShibbolethKeys.add("displayName");
        displayNameShibbolethKeys.add("commonName");
    }
    private final static Configuration INSTANCE = new Configuration();

    private Configuration() {
    }

    public static Configuration getInstance() {
        return INSTANCE;
    }

    public List<String> getDisplayNameShibbolethKeys() {
        return displayNameShibbolethKeys;
    }

    public String getGeneralComponentSchema() {
        return generalComponentSchema;
    }

    public String getCcrRestUrl() {
        return ccrRestUrl;
    }

    public String getClavasRestUrl() {
        return clavasRestUrl;
    }

    public String getToolkitLocation() {
        return toolkitLocation;
    }

    public String[] getAdminUsersArray() {
        return adminUsers.toArray(new String[0]);
    }

    public boolean isAdminUser(Principal principal) {
        if (principal != null) {
            return principal.getName().trim().length() > 0 // user name must be set (in case an empty entry is in admin users list)
                    && adminUsers.contains(principal.getName());
        }
        return false;
    }

    public boolean isAdminUser(String name) {
        if (name != null) {
            return name.trim().length() > 0 // user name must be set (in case an empty entry is in admin users list)
                    && adminUsers.contains(name);
        }
        return false;
    }

    public void setAdminUsers(Collection<String> adminUsers) {
        LOG.debug("Setting adminUsers to {}", Arrays.toString(adminUsers.toArray()));
        this.adminUsers = adminUsers;
    }

    /**
     *
     * @param adminUsersList list of admin users
     */
    public void setAdminUsersList(String adminUsersList) {
        String[] adminUsersArray = adminUsersList.trim().split("\\s+");
        if (LOG.isDebugEnabled()) {
            LOG.info("Setting adminUsersList to {}", Arrays.toString(adminUsersArray));
        }
        setAdminUsers(Arrays.asList(adminUsersArray));
    }

    public void setDisplayNameShibbolethKeys(List<String> displayNameShibbolethKeys) {
        LOG.info("Setting displayNameShibbolethKeys to {}", displayNameShibbolethKeys);
        this.displayNameShibbolethKeys = displayNameShibbolethKeys;
    }

    public void setGeneralComponentSchema(String generalComponentSchema) {
        LOG.info("Setting generalComponentSchema to {}", generalComponentSchema);
        this.generalComponentSchema = generalComponentSchema;
    }

    public void setCcrRestUrl(String ccrRestUrl) {
        LOG.info("Setting ccrRestUrl to {}", ccrRestUrl);
        this.ccrRestUrl = ccrRestUrl;
    }

    public void setClavasRestUrl(String clavasRestUrl) {
        LOG.info("Setting clavasRestUrl to {}", clavasRestUrl);
        this.clavasRestUrl = clavasRestUrl;
    }

    public void setToolkitLocation(String toolkitLocation) {
        LOG.info("Setting toolkitLocation to {}", toolkitLocation);
        this.toolkitLocation = toolkitLocation;
    }

    public void setSkosmosCacheRefreshRateSeconds(long skosmosCacheRefreshRateSeconds) {
        LOG.info("Setting skosmosCacheRefreshRateSeconds to {}", skosmosCacheRefreshRateSeconds);
        this.skosmosCacheRefreshRateSeconds = skosmosCacheRefreshRateSeconds;
    }

    public long getSkosmosCacheRefreshRateSeconds() {
        return skosmosCacheRefreshRateSeconds;
    }

}
