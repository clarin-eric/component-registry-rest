package clarin.cmdi.componentregistry.validation;

import clarin.cmdi.componentregistry.BaseUnitTest;
import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.OwnerUser;
import clarin.cmdi.componentregistry.RegistrySpace;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.ComponentSpec;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.rest.DummyPrincipal;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author george.georgovassilis@mpi.nl
 *
 */
public class ComponentSpecValidatorTest extends BaseUnitTest {

    @Autowired
    private ComponentRegistryFactory componentRegistryFactory;
    @Autowired
    private ComponentRegistry testRegistry;

    private RegistryUser user;

    @Before
    @Override
    public void setUp() throws TransformerException, UserUnauthorizedException {
        testRegistry = componentRegistryFactory.getPublicRegistry();
        user = componentRegistryFactory.getOrCreateUser(DummyPrincipal.DUMMY_CREDENTIALS);
    }

    @Test
    public void testValidateSucces() throws UserUnauthorizedException {
        ComponentSpecValidator validator = this.getValidProfileValidator();
        boolean result = validator.validate();
        String messages = result || validator.getErrorMessages() == null ? "" : validator.getErrorMessages().toString();
        assertTrue(messages, result);
    }

    @Test
    public void testValidateNoComponentId() throws Exception {
        String profileContent = "";
        profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        profileContent += "<ComponentSpec CMDVersion=\"1.2\" isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        profileContent += "    xsi:noNamespaceSchemaLocation=\"cmd-component.xsd\">\n";
        profileContent += "    <Header>\n";
        profileContent += "     <ID>clarin.eu:cr1:p_12345678</ID>\n";
        profileContent += "     <Name>Test</Name>\n";
        profileContent += "     <Status>development</Status>\n";
        profileContent += "    </Header>\n";
        profileContent += "    <Component name=\"Test\"/>\n";
        profileContent += "</ComponentSpec>\n";
        InputStream input = new ByteArrayInputStream(profileContent.getBytes());

        ProfileDescription desc = ProfileDescription.createNewDescription();
        desc.setPublic(true);
        ComponentSpecValidator validator = new ComponentSpecValidator(input, desc, testRegistry, marshaller);
        assertFalse(validator.validate());
        assertEquals(1, validator.getErrorMessages().size());
        assertTrue(validator.getErrorMessages().get(0).startsWith(ComponentSpecValidator.VALIDATION_ERROR));
    }

    @Test
    public void testValidateComponentIdNotRegistered() throws Exception {
        String id1 = "component1";
        String id2 = "component2";

        String profileContent = "";
        profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        profileContent += "<ComponentSpec CMDVersion=\"1.2\" isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        profileContent += "    xsi:noNamespaceSchemaLocation=\"cmd-component.xsd\">";
        profileContent += "    <Header>\n";
        profileContent += "     <ID>clarin.eu:cr1:p_12345678</ID>\n";
        profileContent += "     <Name>Test</Name>\n";
        profileContent += "     <Status>development</Status>\n";
        profileContent += "    </Header>\n";
        profileContent += "    <Component name=\"Test\">";
        profileContent += "	<Component ComponentRef=\""
                + ComponentDescription.COMPONENT_PREFIX + id1 + "\"/>"; // id not
        // registered
        profileContent += "	<Component ComponentRef=\""
                + ComponentDescription.COMPONENT_PREFIX + id2 + "\"/>"; // id not
        // registered
        profileContent += "    </Component>";
        profileContent += "</ComponentSpec>";

        // Ids not registered. two points of failure: one per each id.
        ProfileDescription desc = ProfileDescription.createNewDescription();
        desc.setPublic(true);
        ComponentSpecValidator validator = new ComponentSpecValidator(new ByteArrayInputStream(
                profileContent.getBytes()), desc, testRegistry, marshaller);
        assertFalse(validator.validate());
        assertEquals(1, validator.getErrorMessages().size());
        assertTrue(validator
                .getErrorMessages()
                .get(0)
                .startsWith(ComponentSpecValidator.COMPONENT_NOT_REGISTERED_ERROR));
        
        // id1 will be added as public and therefore only id2 is not registered
        RegistryTestHelper.addComponent(testRegistry, id1, true);
        validator = new ComponentSpecValidator(new ByteArrayInputStream(
                profileContent.getBytes()), desc, testRegistry, marshaller);
        assertFalse(validator.validate());
        assertEquals(1, validator.getErrorMessages().size());
        assertTrue(validator
                .getErrorMessages()
                .get(0)
                .startsWith(ComponentSpecValidator.COMPONENT_NOT_REGISTERED_ERROR));

        // id2 is added as public, no more errors shoud be return
        RegistryTestHelper.addComponent(testRegistry, id2, true);
        validator = new ComponentSpecValidator(new ByteArrayInputStream(
                profileContent.getBytes()), desc, testRegistry, marshaller);
        assertTrue("component is registered should be valid now",
                validator.validate());
        assertEquals(0, validator.getErrorMessages().size());
    }

    @Test
    public void testValidateBaseAndUserRegistry() throws Exception {
        String id1 = "component1";
        String id2 = "component2";

        String profileContent = "";
        profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        profileContent += "<ComponentSpec CMDVersion=\"1.2\" isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
        profileContent += "    xsi:noNamespaceSchemaLocation=\"cmd-component.xsd\">";
        profileContent += "    <Header>\n";
        profileContent += "     <ID>clarin.eu:cr1:p_12345678</ID>\n";
        profileContent += "     <Name>Test</Name>\n";
        profileContent += "     <Status>development</Status>\n";
        profileContent += "    </Header>\n";
        profileContent += "    <Component name=\"Test\">";
        profileContent += "	<Component ComponentRef=\""
                + ComponentDescription.COMPONENT_PREFIX + id1 + "\"/>"; // id not
        // registered
        profileContent += "	<Component name=\"ComponentTwo\" ComponentRef=\""
                + ComponentDescription.COMPONENT_PREFIX + id2 + "\"/>"; // id not
        // registered
        profileContent += "    </Component>";
        profileContent += "</ComponentSpec>";

        // Public Registry
        ProfileDescription desc = ProfileDescription.createNewDescription();
        ComponentSpecValidator validator = new ComponentSpecValidator(new ByteArrayInputStream(
                profileContent.getBytes()), desc, testRegistry, marshaller);
        assertFalse(validator.validate());
        assertEquals(1, validator.getErrorMessages().size()); // the exception is thrown and propagated on the first non-registered component
        assertTrue(validator.getErrorMessages().get(0)
                .startsWith(ComponentSpecValidator.COMPONENT_NOT_REGISTERED_ERROR));

        // registering publically the first component
        RegistryTestHelper.addComponent(testRegistry, id1, true);
        validator = new ComponentSpecValidator(new ByteArrayInputStream(
                profileContent.getBytes()), desc, testRegistry, marshaller);
        assertFalse(validator.validate());
        assertEquals(1, validator.getErrorMessages().size()); // the exception is thrown on the second non-registered component
        assertTrue(validator.getErrorMessages().get(0)
                .startsWith(ComponentSpecValidator.COMPONENT_NOT_REGISTERED_ERROR));

        RegistryTestHelper.addComponent(testRegistry, id2, false);
        validator = new ComponentSpecValidator(new ByteArrayInputStream(
                profileContent.getBytes()), desc, testRegistry, marshaller);
        assertFalse(validator.validate());
        assertEquals(1, validator.getErrorMessages().size());
        assertTrue(validator
                .getErrorMessages()
                .get(0)
                .startsWith(ComponentSpecValidator.COMPONENT_NOT_REGISTERED_IN_APPROPRIATE_SPACE_ERROR));

        // make it user registry
        testRegistry.setRegistryOwner(new OwnerUser(user.getId()));
        testRegistry.setRegistrySpace(RegistrySpace.PRIVATE);

        validator = new ComponentSpecValidator(new ByteArrayInputStream(
                profileContent.getBytes()), desc, testRegistry, marshaller);
        Boolean result = validator.validate();
        assertTrue(result);
        assertEquals(0, validator.getErrorMessages().size());
    }

    @Test
    public void testValidateNestedComponents() throws Exception {
        String id1 = "component1";

        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "<ComponentSpec CMDVersion=\"1.2\" isProfile=\"false\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"cmd-component.xsd\">\n";
        content += "    <Header>\n";
        content += "     <ID>clarin.eu:cr1:p_12345678</ID>\n";
        content += "     <Name>Actor</Name>\n";
        content += "     <Status>development</Status>\n";
        content += "    </Header>\n";
        content += "    <Component name=\"Actor\" CardinalityMin=\"1\" CardinalityMax=\"1\">\n";
        content += "        <Element name=\"Name\" ValueScheme=\"string\" />\n";
        content += "      <Component ComponentRef=\""
                + ComponentDescription.COMPONENT_PREFIX + id1 + "\"/>\n"; // id not
        // registered
        content += "    </Component>\n";
        content += "</ComponentSpec>\n";

        ComponentDescription desc = ComponentDescription.createNewDescription();
        ComponentSpecValidator validator = new ComponentSpecValidator(new ByteArrayInputStream(
                content.getBytes()), desc, testRegistry, marshaller);
        assertFalse(validator.validate());
        assertEquals(1, validator.getErrorMessages().size());
        assertTrue(validator
                .getErrorMessages()
                .get(0)
                .startsWith(ComponentSpecValidator.COMPONENT_NOT_REGISTERED_ERROR));

        RegistryTestHelper.addComponent(testRegistry, id1, true);
        validator = new ComponentSpecValidator(
                new ByteArrayInputStream(content.getBytes()), desc, testRegistry, marshaller);
        assertTrue(validator.validate());
        assertEquals(0, validator.getErrorMessages().size());
    }

    /**
     * Test of getComponentSpec method, of class MDValidator.
     */
    @Test
    public void testGetCMDComponentSpec() throws Exception {
        String profileContent = getValidProfileString();
        InputStream input = new ByteArrayInputStream(profileContent.getBytes());

        ProfileDescription desc = ProfileDescription.createNewDescription();
        ComponentSpecValidator validator = new ComponentSpecValidator(input, desc, testRegistry, marshaller);

        // Spec is created during validation, before it should be null
        assertNull(validator.getComponentSpec());
        validator.validate();

        // Get spec created during validation
        final ComponentSpec cmdComponentSpec = validator
                .getComponentSpec();
        assertNotNull(cmdComponentSpec);

        // Spec content should match XML
        assertTrue(cmdComponentSpec.isIsProfile());
        assertEquals("Actor", cmdComponentSpec.getComponent().getName());

        // Spec copy should be a freshly unmarshalled copy
        final ComponentSpec specCopy = validator.getCopyOfCMDComponentSpec();
        assertNotSame(cmdComponentSpec, specCopy);

        // Content should still match XML
        assertTrue(specCopy.isIsProfile());
        assertEquals("Actor", specCopy.getComponent().getName());
    }

    private ComponentSpecValidator getValidProfileValidator() {
        final String profileContent = getValidProfileString();
        InputStream input = new ByteArrayInputStream(profileContent.getBytes());
        ProfileDescription desc = ProfileDescription.createNewDescription();
        desc.setPublic(true);
        desc.setName("test_name");
        ComponentSpecValidator validator = new ComponentSpecValidator(input, desc, testRegistry, marshaller);
        return validator;
    }

    private String getValidProfileString() {
        String profileContent = "";
        profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        profileContent += "<ComponentSpec CMDVersion=\"1.2\" isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        profileContent += "    xsi:noNamespaceSchemaLocation=\"cmd-component.xsd\">\n";
        profileContent += "    <Header>\n";
        profileContent += "     <ID>clarin.eu:cr1:p_12345678</ID>\n";
        profileContent += "     <Name>Actor</Name>\n";
        profileContent += "     <Status>development</Status>\n";
        profileContent += "    </Header>\n";
        profileContent += "    <Component name=\"Actor\" CardinalityMin=\"1\" CardinalityMax=\"1\">\n";
        profileContent += "        <Element name=\"Age\">\n";
        profileContent += "            <ValueScheme>\n";
        profileContent += "                <pattern>[23][0-9]</pattern>\n";
        profileContent += "            </ValueScheme>\n";
        profileContent += "        </Element>\n";
        profileContent += "    </Component>\n";
        profileContent += "</ComponentSpec>\n";
        return profileContent;
    }
}
