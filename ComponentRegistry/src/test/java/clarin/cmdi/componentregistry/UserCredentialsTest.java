package clarin.cmdi.componentregistry;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import clarin.cmdi.componentregistry.rest.DummyPrincipal;
import de.mpg.aai.shhaa.model.AuthAttribute;
import de.mpg.aai.shhaa.model.AuthAttributes;
import de.mpg.aai.shhaa.model.AuthPrincipal;
import de.mpg.aai.shhaa.model.BaseAuthAttribute;

public class UserCredentialsTest {

    @Test
    public void testUserCredentials() throws Exception {
        ShhaaUserCredentials creds = new ShhaaUserCredentials(new DummyPrincipal("noot"));
        assertEquals("noot", creds.getDisplayName());
        assertEquals("noot", creds.getPrincipalName());
        assertEquals(DigestUtils.md5Hex("noot"), creds.getPrincipalNameMD5Hex());

        AuthPrincipal principal = new AuthPrincipal("noot");
        creds = new ShhaaUserCredentials(principal);
        assertEquals("noot", creds.getDisplayName());
        assertEquals("noot", creds.getPrincipalName());
        assertEquals(DigestUtils.md5Hex("noot"), creds.getPrincipalNameMD5Hex());

        principal = new AuthPrincipal("noot");
        Set<AuthAttribute<?>> values = new HashSet<AuthAttribute<?>>();
        values.add(new BaseAuthAttribute<String>("displayName", "Mr. Noot"));
        AuthAttributes attributes = new AuthAttributes(values);
        principal.setAttribues(attributes);
        creds = new ShhaaUserCredentials(principal);
        assertEquals("Mr. Noot", creds.getDisplayName());
        assertEquals("noot", creds.getPrincipalName());
        assertEquals(DigestUtils.md5Hex("noot"), creds.getPrincipalNameMD5Hex());
    }
}
