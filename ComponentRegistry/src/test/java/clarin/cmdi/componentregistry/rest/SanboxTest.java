/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryTestDatabase;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.Ownership;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.jpa.CommentsDao;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestServiceTest.REGISTRY_BASE;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.multipart.FormDataMultiPart;
import java.text.ParseException;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.*;
import org.junit.Test;
import clarin.cmdi.componentregistry.GroupService;

/**
 *
 * @author olhsha
 */
public class SanboxTest extends ComponentRegistryRestServiceTestCase {

    @Autowired
    private ComponentRegistryFactory componentRegistryFactory;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private GroupService groupService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private CommentsDao commentsDao;
    private ComponentRegistry baseRegistry;

    @Before
    public void init() {
        ComponentRegistryTestDatabase.resetAndCreateAllTables(jdbcTemplate);
        createUserRecord();
        baseRegistry = componentRegistryFactory.getBaseRegistry(DummyPrincipal.DUMMY_CREDENTIALS);
    }

    protected void createAntherUserRecord() {
        RegistryUser user = new RegistryUser();
        user.setName("Another database test user");
        user.setPrincipalName("anotherPrincipal");
        userDao.save(user);
    }

    private void MakeGroupA() {
        groupService.createNewGroup("group A", DummyPrincipal.DUMMY_PRINCIPAL.getName());
    }

    private void MakeGroupB() throws ItemNotFoundException {
        createAntherUserRecord();
        groupService.createNewGroup("group B", "anotherPrincipal");
        groupService.makeMember(DummyPrincipal.DUMMY_PRINCIPAL.getName(), "group B");
    }

    private void MakeGroupC() throws ItemNotFoundException {
        groupService.createNewGroup("group C", "anotherPrincipal");
    }

    private void fillUpGroupB() throws ParseException, JAXBException, ItemNotFoundException {

        MakeGroupB();

        RegistryTestHelper.addProfile(baseRegistry, "Bprofile-1", false);
        RegistryTestHelper.addComponent(baseRegistry, "Bcomponent-1", false);
        RegistryTestHelper.addComponent(baseRegistry, "Bcomponent-2", false);

        Ownership ownership = new Ownership();
        ownership.setComponentRef(ProfileDescription.PROFILE_PREFIX + "Bprofile-1");
        ownership.setGroupId(2);
        ownership.setUserId(0);
        groupService.addOwnership(ownership);

        ownership.setComponentRef(ComponentDescription.COMPONENT_PREFIX + "Bcomponent-1");
        ownership.setGroupId(2);
        ownership.setUserId(0);
        groupService.addOwnership(ownership);

        ownership.setComponentRef(ComponentDescription.COMPONENT_PREFIX + "Bcomponent-2");
        ownership.setGroupId(2);
        ownership.setUserId(0);
        groupService.addOwnership(ownership);

    }

    private void fillUpGroupC() throws ParseException, JAXBException, ItemNotFoundException {

        MakeGroupC();

        RegistryTestHelper.addProfileAnotherPrincipal(baseRegistry, "Cprofile-1", false);
        RegistryTestHelper.addComponentAnotherPrincipal(baseRegistry, "Ccomponent-1", false);
        RegistryTestHelper.addComponentAnotherPrincipal(baseRegistry, "Ccomponent-2", false);

        Ownership ownership = new Ownership();
        ownership.setComponentRef(ProfileDescription.PROFILE_PREFIX + "Cprofile-1");
        ownership.setGroupId(3);
        ownership.setUserId(0);
        groupService.addOwnership(ownership);

        ownership.setComponentRef(ComponentDescription.COMPONENT_PREFIX + "Ccomponent-1");
        ownership.setGroupId(3);
        ownership.setUserId(0);
        groupService.addOwnership(ownership);

        ownership.setComponentRef(ComponentDescription.COMPONENT_PREFIX + "Ccomponent-2");
        ownership.setGroupId(3);
        ownership.setUserId(0);
        groupService.addOwnership(ownership);

    }

    private FormDataMultiPart createFormData(Object content) {
        return createFormData(content, "My Test");
    }

    private FormDataMultiPart createFormData(Object content, String description) {
        FormDataMultiPart form = new FormDataMultiPart();
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD, content,
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field(ComponentRegistryRestService.NAME_FORM_FIELD, "Test1");
        form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD,
                description);
        form.field(ComponentRegistryRestService.DOMAIN_FORM_FIELD, "My domain");
        form.field(ComponentRegistryRestService.GROUP_FORM_FIELD, "TestGroup");
        return form;
    }

    @Test
    public void testGetRegisteredGroupProfilecomponentRawData() throws Exception {

        System.out.println("test getRegisteredComponentAndProfileRawData");

        fillUpGroupB();
        fillUpGroupC();

        String id = ComponentDescription.COMPONENT_PREFIX + "Bcomponent-1";
        String component = this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components/" + id + "/xml"))
                .accept(MediaType.TEXT_XML).get(String.class).trim();
        assertTrue(component
                .startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<ComponentSpec"));
        assertTrue(component.endsWith("</ComponentSpec>"));
        assertTrue(component.contains("xsi:noNamespaceSchemaLocation"));

        id = ProfileDescription.PROFILE_PREFIX + "Bprofile-1";
        String profile = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + id + "/xsd"))
                .accept(MediaType.TEXT_XML).get(String.class).trim();
        assertTrue(profile
                .startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?><xs:schema"));
        assertTrue(profile.endsWith("</xs:schema>"));

        profile = this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/profiles/" + id + "/xml"))
                .accept(MediaType.TEXT_XML).get(String.class).trim();
        assertTrue(profile
                .startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<ComponentSpec"));
        assertTrue(profile.endsWith("</ComponentSpec>"));
        assertTrue(profile.contains("xsi:noNamespaceSchemaLocation"));

        id = ProfileDescription.PROFILE_PREFIX + "Cprofile-1";
        ClientResponse resp = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + id + "/xsd"))
                .accept(MediaType.TEXT_XML).get(ClientResponse.class);
        assertEquals("XSD should always be available", 200, resp.getStatus());

    }

}
