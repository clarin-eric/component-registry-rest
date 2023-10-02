package clarin.cmdi.componentregistry;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
    private Collection<String> adminUsers = new HashSet<>();
    private List<String> displayNameShibbolethKeys = new ArrayList<>();

    private Set<String> includedSchemesForConcepts = Collections.emptySet();
    private Set<String> includedVocabsForConcepts = Collections.emptySet();

    private Set<String> includedSchemesForVocabularies = Collections.emptySet();
    private Set<String> excludedSchemesForVocabularies = Collections.emptySet();
    private Set<String> includedVocabsForVocabularies = Collections.emptySet();
    private Set<String> excludedVocabsForVocabularies = Collections.emptySet();

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

    public long getSkosmosCacheRefreshRateSeconds() {
        return skosmosCacheRefreshRateSeconds;
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

    public Set<String> getIncludedSchemesForConcepts() {
        return includedSchemesForConcepts;
    }

    public Set<String> getIncludedVocabsForConcepts() {
        return includedVocabsForConcepts;
    }

    public Set<String> getIncludedSchemesForVocabularies() {
        return includedSchemesForVocabularies;
    }

    public Set<String> getExcludedSchemesForVocabularies() {
        return excludedSchemesForVocabularies;
    }

    public Set<String> getIncludedVocabsForVocabularies() {
        return includedVocabsForVocabularies;
    }

    public Set<String> getExcludedVocabsForVocabularies() {
        return excludedVocabsForVocabularies;
    }

    public void setIncludedVocabsForConcepts(String includedVocabs) {
        includedVocabsForConcepts = stringPropertyToSet(includedVocabs);
    }

    public void setIncludedSchemesForConcepts(String includedSchemes) {
        includedSchemesForConcepts = stringPropertyToSet(includedSchemes);
    }

    public void setExcludedSchemesForVocabularies(String excludedSchemes) {
        excludedSchemesForVocabularies = stringPropertyToSet(excludedSchemes);
    }

    public void setIncludedVocabsForVocabularies(String includedVocabs) {
        includedVocabsForVocabularies = stringPropertyToSet(includedVocabs);
    }

    public void setIncludedSchemesForVocabularies(String includedSchemes) {
        includedSchemesForVocabularies = stringPropertyToSet(includedSchemes);
    }

    public void setExcludedVocabsForVocabularies(String excludedVocabs) {
        excludedVocabsForVocabularies = stringPropertyToSet(excludedVocabs);
    }

    private Set<String> stringPropertyToSet(String value) {
        //split on whitespace
        final Splitter stringToSet = Splitter.on(CharMatcher.breakingWhitespace())
                //with some added tolerance
                .trimResults()
                .omitEmptyStrings();

        if (value == null) {
            return Collections.emptySet();
        } else {
            //split and collect into an unmodifiable set
            return stringToSet
                    .splitToStream(value)
                    .collect(Collectors.toUnmodifiableSet());
        }
    }

}
