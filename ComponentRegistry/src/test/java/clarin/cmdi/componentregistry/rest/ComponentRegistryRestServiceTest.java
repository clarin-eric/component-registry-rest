package clarin.cmdi.componentregistry.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryImplTest;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.inmemory.InMemoryTestContainerFactory;

public class ComponentRegistryRestServiceTest extends JerseyTest {

    //CommandLine test e.g.:  curl -i -H "Accept:application/json" -X GET  http://localhost:8080/ComponentRegistry/rest/registry/profiles

    private static ComponentRegistry testRegistry;
    private static File registryDir;

    private final static GenericType<List<ProfileDescription>> PROFILE_LIST_GENERICTYPE = new GenericType<List<ProfileDescription>>() {
    };
    private final static GenericType<List<ComponentDescription>> COMPONENT_LIST_GENERICTYPE = new GenericType<List<ComponentDescription>>() {
    };

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new InMemoryTestContainerFactory();
    }

    @Override
    protected AppDescriptor configure() {
        String packageName = ComponentRegistryRestService.class.getPackage().getName();
        return new LowLevelAppDescriptor.Builder(packageName).build();
    }

    @Test
    public void testGetRegisteredProfiles() throws Exception {
        List<ProfileDescription> response = resource().path("/registry/profiles").accept(MediaType.APPLICATION_XML).get(
                PROFILE_LIST_GENERICTYPE);
        assertEquals(2, response.size());
        response = resource().path("/registry/profiles").accept(MediaType.APPLICATION_JSON).get(PROFILE_LIST_GENERICTYPE);
        assertEquals(2, response.size());
    }

    @Test
    public void testGetRegisteredComponents() {
        List<ComponentDescription> response = resource().path("/registry/components").accept(MediaType.APPLICATION_XML).get(
                COMPONENT_LIST_GENERICTYPE);
        assertEquals(2, response.size());
        response = resource().path("/registry/components").accept(MediaType.APPLICATION_JSON).get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(2, response.size());
    }

    @Test
    public void testGetRegisteredComponent() {
        CMDComponentSpec component = resource().path("/registry/components/clarin.eu:cr1:component1").accept(MediaType.APPLICATION_JSON)
                .get(CMDComponentSpec.class);
        assertNotNull(component);
        assertEquals("Access", component.getCMDComponent().get(0).getName());
        component = resource().path("/registry/components/clarin.eu:cr1:component2").accept(MediaType.APPLICATION_XML).get(
                CMDComponentSpec.class);
        assertNotNull(component);
        assertEquals("Access", component.getCMDComponent().get(0).getName());
    }

    @Test
    public void testGetRegisteredProfile() throws Exception {
        CMDComponentSpec profile = resource().path("/registry/profiles/clarin.eu:cr1:profile1").accept(MediaType.APPLICATION_JSON).get(
                CMDComponentSpec.class);
        assertNotNull(profile);
        assertEquals("Actor", profile.getCMDComponent().get(0).getName());
        profile = resource().path("/registry/profiles/clarin.eu:cr1:profile2").accept(MediaType.APPLICATION_XML)
                .get(CMDComponentSpec.class);
        assertNotNull(profile);
        assertEquals("Actor", profile.getCMDComponent().get(0).getName());
        //TODO Patrick, test header id when available
    }

    @Test
    public void testGetRegisteredProfileRawData() throws Exception {
        String profile = resource().path("/registry/profiles/clarin.eu:cr1:profile1/xsd").accept(MediaType.TEXT_XML).get(String.class);
        assertTrue(profile.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xs:schema"));
        assertTrue(profile.endsWith("</xs:schema>"));

        profile = resource().path("/registry/profiles/clarin.eu:cr1:profile1/xml").accept(MediaType.TEXT_XML).get(String.class);
        assertTrue(profile.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<CMD_ComponentSpec"));
        assertTrue(profile.endsWith("</CMD_ComponentSpec>\n"));

        try {
            resource().path("/registry/components/clarin.eu:cr1:component1/xsl").accept(MediaType.TEXT_XML).get(String.class);
            fail("Should have thrown exception, unsopported path parameter");
        } catch (UniformInterfaceException e) {//server error
        }
    }

    @Test
    public void testGetRegisteredComponentRawData() throws Exception {
        String component = resource().path("/registry/components/clarin.eu:cr1:component1/xsd").accept(MediaType.TEXT_XML)
                .get(String.class);
        assertTrue(component.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xs:schema"));
        assertTrue(component.endsWith("</xs:schema>"));

        component = resource().path("/registry/components/clarin.eu:cr1:component1/xml").accept(MediaType.TEXT_XML).get(String.class);
        assertTrue(component.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<CMD_ComponentSpec"));
        assertTrue(component.endsWith("</CMD_ComponentSpec>\n"));

        try {
            resource().path("/registry/components/clarin.eu:cr1:component1/jpg").accept(MediaType.TEXT_XML).get(String.class);
            fail("Should have thrown exception, unsopported path parameter");
        } catch (UniformInterfaceException e) {
        }
    }

    @Test
    public void testRegisterProfile() throws Exception {
        FormDataMultiPart form = new FormDataMultiPart();
        form.field("data", getTestProfileContent(), MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field("name", "ProfileTest1");
        form.field("description", "My Test Profile");
        form.field("creatorName", "J. Unit");
        RegisterResponse response = resource().path("/registry/profiles").type(MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class,
                form);
        assertTrue(response.isProfile());
        ProfileDescription profileDesc = (ProfileDescription) response.getDescription();
        assertNotNull(profileDesc);
        assertEquals("ProfileTest1", profileDesc.getName());
        assertEquals("My Test Profile", profileDesc.getDescription());
        assertEquals("J. Unit", profileDesc.getCreatorName());
        assertTrue(profileDesc.getId().startsWith("p_"));
        assertNotNull(profileDesc.getRegistrationDate());
    }
    
    @Test
    public void testRegisterComponent() throws Exception {
        FormDataMultiPart form = new FormDataMultiPart();
        form.field("data", getComponentTestContent(), MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field("name", "ComponentTest1");
        form.field("description", "My Test Component");
        form.field("creatorName", "J. Unit");
        form.field("group", "TestGroup");
        RegisterResponse response = resource().path("/registry/components").type(MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class,
                form);
        assertTrue(response.isRegistered());
        assertFalse(response.isProfile());
        ComponentDescription desc = (ComponentDescription) response.getDescription();
        assertNotNull(desc);
        assertEquals("ComponentTest1", desc.getName());
        assertEquals("My Test Component", desc.getDescription());
        assertEquals("J. Unit", desc.getCreatorName());
        assertEquals("TestGroup", desc.getGroupName());
        assertTrue(desc.getId().startsWith("c_"));
        assertNotNull(desc.getRegistrationDate());
    }

    @Test
    public void testRegisterProfileInvalidData() throws Exception {
        FormDataMultiPart form = new FormDataMultiPart();
        String notAValidProfile = "<CMD_ComponentSpec> </CMD_ComponentSpec>";
        form.field("data", new ByteArrayInputStream(notAValidProfile.getBytes()), MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field("name", "ProfileTest1");
        form.field("description", "My Test Profile");
        form.field("creatorName", "J. Unit");
        RegisterResponse postResponse = resource().path("/registry/profiles").type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);
        assertTrue(postResponse.isProfile());
        assertFalse(postResponse.isRegistered());
        assertEquals(1, postResponse.getErrors().size());
        assertTrue(postResponse.getErrors().get(0).contains("SAXParseException"));
    }

    @Test
    public void testRegisterProfileInvalidDescriptionAndContent() throws Exception {
        FormDataMultiPart form = new FormDataMultiPart();
        String profileContent = "";
        profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        profileContent += "<CMD_ComponentSpec> \n"; //No isProfile attribute
        profileContent += "    <Header />\n";
        profileContent += "    <CMD_Component name=\"Actor\" CardinalityMin=\"0\" CardinalityMax=\"unbounded\">\n";
        profileContent += "        <AttributeList>\n";
        profileContent += "            <Attribute>\n";
        profileContent += "                <Name>Name</Name>\n";
        profileContent += "                <Type>string</Type>\n";
        profileContent += "            </Attribute>\n";
        profileContent += "        </AttributeList>\n";
        profileContent += "    </CMD_Component>\n";
        profileContent += "</CMD_ComponentSpec>\n";
        form.field("data", new ByteArrayInputStream(profileContent.getBytes()), MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field("name", "");//Empty name so invalid
        form.field("description", "My Test Profile");
        form.field("creatorName", "J. Unit");
        RegisterResponse response = resource().path("/registry/profiles").type(MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class,
                form);
        assertFalse(response.isRegistered());
        assertEquals(2, response.getErrors().size());
        assertNotNull(response.getErrors().get(0));
        assertEquals(MDValidator.ISPROFILE_NOT_SET_ERROR, response.getErrors().get(1));
    }

    @Test
    public void testRegisterComponentAsProfile() throws Exception {
        FormDataMultiPart form = new FormDataMultiPart();
        form.field("data", getComponentTestContent(), MediaType.APPLICATION_OCTET_STREAM_TYPE); 
        form.field("name", "t");
        form.field("description", "My Test");
        form.field("creatorName", "J. Unit");
        RegisterResponse response = resource().path("/registry/profiles").type(MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class,
                form);
        assertFalse(response.isRegistered());
        assertTrue(response.isProfile());
        assertEquals(1, response.getErrors().size());
        assertEquals(MDValidator.MISMATCH_ERROR, response.getErrors().get(0));
    }

    @Test
    public void testRegisterProfileBothDescAndDataInvalid() throws Exception {
        //TODO PD Test also check for components
    }

    @BeforeClass
    public static void setUpTestRegistry() throws ParseException, JAXBException {
        registryDir = ComponentRegistryImplTest.createTempRegistryDir();
        testRegistry = ComponentRegistryImplTest.getTestRegistry(registryDir);
        addProfile(testRegistry, "profile1");
        addProfile(testRegistry, "profile2");
        addComponent(testRegistry, "component1");
        addComponent(testRegistry, "component2");
    }

    private static void addProfile(ComponentRegistry testRegistry, String id) throws ParseException, JAXBException {
        ProfileDescription desc = new ProfileDescription();
        desc.setCreatorName("J. Unit");
        desc.setName("testProfile");
        desc.setRegistrationDate("" + SimpleDateFormat.getDateInstance(DateFormat.SHORT).parse("1/1/2009"));
        desc.setDescription("Test Description");
        desc.setId(id);
        desc.setXlink("link:" + id);

        testRegistry.registerMDProfile(desc, getTestProfile());
    }

    private static InputStream getTestProfileContent() {
        String profileContent = "";
        profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        profileContent += "<CMD_ComponentSpec isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        profileContent += "    xsi:noNamespaceSchemaLocation=\"general-component-schema.xsd\">\n";
        profileContent += "    <Header />\n";
        profileContent += "    <CMD_Component name=\"Actor\" CardinalityMin=\"0\" CardinalityMax=\"unbounded\">\n";
        profileContent += "        <AttributeList>\n";
        profileContent += "            <Attribute>\n";
        profileContent += "                <Name>Name</Name>\n";
        profileContent += "                <Type>string</Type>\n";
        profileContent += "            </Attribute>\n";
        profileContent += "        </AttributeList>\n";
        profileContent += "        <CMD_Element name=\"Age\">\n";
        profileContent += "            <ValueScheme>\n";
        profileContent += "                <pattern>[23][0-9]</pattern>\n";
        profileContent += "            </ValueScheme>\n";
        profileContent += "        </CMD_Element>\n";
        profileContent += "    </CMD_Component>\n";
        profileContent += "</CMD_ComponentSpec>\n";
        return new ByteArrayInputStream(profileContent.getBytes());
    }

    public static CMDComponentSpec getTestProfile() throws JAXBException {
        return MDMarshaller.unmarshal(CMDComponentSpec.class, getTestProfileContent(), MDMarshaller.getCMDComponentSchema());
    }

    private static void addComponent(ComponentRegistry testRegistry, String id) throws ParseException, JAXBException {
        ComponentDescription desc = new ComponentDescription();
        desc.setCreatorName("J. Unit");
        desc.setName("testComponent");
        desc.setRegistrationDate("" + SimpleDateFormat.getDateInstance(DateFormat.SHORT).parse("1/1/2009"));
        desc.setDescription("Test Description");
        desc.setId(id);
        desc.setXlink("link:" + id);

        testRegistry.registerMDComponent(desc, MDMarshaller.unmarshal(CMDComponentSpec.class, getComponentTestContent(), MDMarshaller
                .getCMDComponentSchema()));
    }

    private static InputStream getComponentTestContent() {
        String compContent = "";
        compContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        compContent += "\n";
        compContent += "<CMD_ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        compContent += "    xsi:noNamespaceSchemaLocation=\"../../general-component-schema.xsd\">\n";
        compContent += "    \n";
        compContent += "    <Header/>\n";
        compContent += "    \n";
        compContent += "    <CMD_Component name=\"Access\" CardinalityMin=\"1\" CardinalityMax=\"1\">\n";
        compContent += "        <CMD_Element name=\"Availability\" ValueScheme=\"string\" />\n";
        compContent += "        <CMD_Element name=\"Date\">\n";
        compContent += "            <ValueScheme>\n";
        compContent += "                <!-- matching dates of the pattern yyyy-mm-dd (ISO 8601); this only matches dates from the years 1000 through 2999 and does allow some invalid dates (e.g. February, the 30th) -->\n";
        compContent += "                <pattern>(1|2)\\d{3}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])</pattern>                \n";
        compContent += "            </ValueScheme>\n";
        compContent += "        </CMD_Element>\n";
        compContent += "        <CMD_Element name=\"Owner\" ValueScheme=\"string\" />\n";
        compContent += "        <CMD_Element name=\"Publisher\" ValueScheme=\"string\" />\n";
        compContent += "    </CMD_Component>\n";
        compContent += "\n";
        compContent += "</CMD_ComponentSpec>\n";
        return new ByteArrayInputStream(compContent.getBytes());
    }

    @AfterClass
    public static void deleteRegistry() {
        if (registryDir != null && registryDir.exists()) {
            assertTrue(FileUtils.deleteQuietly(registryDir));
        }
    }

}
