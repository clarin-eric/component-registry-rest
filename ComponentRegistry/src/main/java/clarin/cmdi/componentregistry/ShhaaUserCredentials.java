package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.model.UserCredentials;
import java.security.Principal;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import de.mpg.aai.shhaa.model.AuthAttribute;
import de.mpg.aai.shhaa.model.AuthAttributes;
import de.mpg.aai.shhaa.model.AuthPrincipal;

/**
 * Wrapper class to hold the userPrincipal and a displayName
 * 
 */
public class ShhaaUserCredentials implements UserCredentials {

    private final Principal userPrincipal;

    public ShhaaUserCredentials(Principal userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

    @Override
    public Principal getPrincipal() {
        return userPrincipal;
    }

    @Override
    public String getPrincipalName() {
        return userPrincipal.getName();
    }

    @Override
    public String getPrincipalNameMD5Hex() {
        return getPrincipalNameMD5Hex(userPrincipal.getName());
    }

    public static String getPrincipalNameMD5Hex(String name){
	return DigestUtils.md5Hex(name);
    }

    @Override
    public String getDisplayName() {
        String result = null;
        if (userPrincipal instanceof AuthPrincipal) {
            List<String> displayNamesAttributes = Configuration.getInstance().getDisplayNameShibbolethKeys();
            AuthPrincipal authPrincipal = (AuthPrincipal) userPrincipal;
            for (String key : displayNamesAttributes) {
                result = getValue(authPrincipal, key);
                if (result != null) {
                    break;
                }
            }
        }
        if (result == null) {
            result = getPrincipalName();
        }
        return result;
    }

    private String getValue(AuthPrincipal authPrincipal, String key) {
        String result = null;
        AuthAttributes attributes = authPrincipal.getAttribues();
        if (attributes != null) {
            AuthAttribute<String> authAttribute = (AuthAttribute<String>) attributes.get(key);
            if (authAttribute != null) {
                result = authAttribute.getValue();
            }
        }
        return result;
    }

    @Override
    public String toString() {
	return getPrincipal().toString();
    }
}
