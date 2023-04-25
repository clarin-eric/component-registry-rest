/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.RegistrySpace;
import clarin.cmdi.componentregistry.components.ComponentSpec;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryTestDatabase;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.CommentResponse;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.Group;
import clarin.cmdi.componentregistry.model.Ownership;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.jpa.CommentsDao;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestServiceTestCase.COMPONENT_LIST_GENERICTYPE;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.GROUPID_PARAM;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.REGISTRY_SPACE_GROUP;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.REGISTRY_SPACE_PARAM;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestServiceTest.REGISTRY_BASE;
import clarin.cmdi.componentregistry.rss.Rss;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.multipart.FormDataMultiPart;
import java.text.ParseException;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.*;
import org.junit.Test;
import org.springframework.util.Assert;
import clarin.cmdi.componentregistry.GroupService;

/**
 *
 * Test groups:
 *
 * A
 * Owner: Dummy Members: - Items: profile-1, component-1, component-2
 *
 * B
 * Owner: anotherPrincipal Members: Dummy Items: Bprofile-1, Bcomponent-1,
 * Bcomponent-2
 *
 * C
 * Owner: anotherPrincipal Members: - Items: Cprofile-1, Ccomponent-1,
 * Ccomponent-2
 *
 * D
 * Owner: anotherPrincipal Members: Dummy, anotherPrincipal Items: Dprofile-1,
 * Dcomponent-1, Dcomponent-2
 *
 * @author olhsha
 * @author twagoo
 */
public class RestGroupServiceTest extends ComponentRegistryRestServiceTestCase {

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

    private void fillUpGroupA() throws ParseException, JAXBException, ItemNotFoundException {

        MakeGroupA();

        RegistryTestHelper.addProfile(baseRegistry, "profile-1", false);
        RegistryTestHelper.addComponent(baseRegistry, "component-1", false);
        RegistryTestHelper.addComponent(baseRegistry, "component-2", false);

        {
            final Ownership ownership = new Ownership();
            ownership.setComponentRef(ProfileDescription.PROFILE_PREFIX + "profile-1");
            ownership.setGroupId(1);
            ownership.setUserId(0);
            groupService.addOwnership(ownership);
        }

        {

            final Ownership ownership = new Ownership();
            ownership.setComponentRef(ComponentDescription.COMPONENT_PREFIX + "component-1");
            ownership.setGroupId(1);
            ownership.setUserId(0);
            groupService.addOwnership(ownership);
        }

        {
            final Ownership ownership = new Ownership();
            ownership.setComponentRef(ComponentDescription.COMPONENT_PREFIX + "component-2");
            ownership.setGroupId(1);
            ownership.setUserId(0);
            groupService.addOwnership(ownership);
        }

    }

    private void MakeGroupB() throws ItemNotFoundException {
        createAntherUserRecord();
        groupService.createNewGroup("group B", "anotherPrincipal");
        groupService.makeMember(DummyPrincipal.DUMMY_PRINCIPAL.getName(), "group B");
    }

    private void fillUpGroupB() throws ParseException, JAXBException, ItemNotFoundException {

        MakeGroupB();

        RegistryTestHelper.addProfile(baseRegistry, "Bprofile-1", false);
        RegistryTestHelper.addComponent(baseRegistry, "Bcomponent-1", false);
        RegistryTestHelper.addComponent(baseRegistry, "Bcomponent-2", false);

        {
            Ownership ownership = new Ownership();
            ownership.setComponentRef(ProfileDescription.PROFILE_PREFIX + "Bprofile-1");
            ownership.setGroupId(2);
            ownership.setUserId(0);
            groupService.addOwnership(ownership);
        }

        {

            Ownership ownership = new Ownership();
            ownership.setComponentRef(ComponentDescription.COMPONENT_PREFIX + "Bcomponent-1");
            ownership.setGroupId(2);
            ownership.setUserId(0);
            groupService.addOwnership(ownership);
        }

        {

            Ownership ownership = new Ownership();
            ownership.setComponentRef(ComponentDescription.COMPONENT_PREFIX + "Bcomponent-2");
            ownership.setGroupId(2);
            ownership.setUserId(0);
            groupService.addOwnership(ownership);
        }

    }

    private void MakeGroupC() throws ItemNotFoundException {
        groupService.createNewGroup("group C", "anotherPrincipal");
    }

    private void fillUpGroupC() throws ParseException, JAXBException, ItemNotFoundException {

        MakeGroupC();

        RegistryTestHelper.addProfileAnotherPrincipal(baseRegistry, "Cprofile-1", false);
        RegistryTestHelper.addComponentAnotherPrincipal(baseRegistry, "Ccomponent-1", false);
        RegistryTestHelper.addComponentAnotherPrincipal(baseRegistry, "Ccomponent-2", false);

        {
            Ownership ownership = new Ownership();
            ownership.setComponentRef(ProfileDescription.PROFILE_PREFIX + "Cprofile-1");
            ownership.setGroupId(3);
            ownership.setUserId(0);
            groupService.addOwnership(ownership);
        }

        {

            Ownership ownership = new Ownership();
            ownership.setComponentRef(ComponentDescription.COMPONENT_PREFIX + "Ccomponent-1");
            ownership.setGroupId(3);
            ownership.setUserId(0);
            groupService.addOwnership(ownership);
        }

        {
            Ownership ownership = new Ownership();
            ownership.setComponentRef(ComponentDescription.COMPONENT_PREFIX + "Ccomponent-2");
            ownership.setGroupId(3);
            ownership.setUserId(0);
            groupService.addOwnership(ownership);
        }
    }

    private void MakeGroupD() throws ItemNotFoundException {
        groupService.createNewGroup("group D", "anotherPrincipal");
        groupService.makeMember("anotherPrincipal", "group D");
        groupService.makeMember(DummyPrincipal.DUMMY_PRINCIPAL.getName(), "group D");
    }

    private void fillUpGroupD() throws ParseException, JAXBException, ItemNotFoundException {

        MakeGroupD();

        RegistryTestHelper.addProfileAnotherPrincipal(baseRegistry, "Dprofile-1", false);
        RegistryTestHelper.addComponentAnotherPrincipal(baseRegistry, "Dcomponent-1", false);
        RegistryTestHelper.addComponentAnotherPrincipal(baseRegistry, "Dcomponent-2", false);

        {
            Ownership ownership = new Ownership();
            ownership.setComponentRef(ProfileDescription.PROFILE_PREFIX + "Dprofile-1");
            ownership.setGroupId(4);
            ownership.setUserId(0);
            groupService.addOwnership(ownership);
        }

        {

            Ownership ownership = new Ownership();
            ownership.setComponentRef(ComponentDescription.COMPONENT_PREFIX + "Dcomponent-1");
            ownership.setGroupId(4);
            ownership.setUserId(0);
            groupService.addOwnership(ownership);
        }

        {
            Ownership ownership = new Ownership();
            ownership.setComponentRef(ComponentDescription.COMPONENT_PREFIX + "Dcomponent-2");
            ownership.setGroupId(4);
            ownership.setUserId(0);
            groupService.addOwnership(ownership);
        }
    }

//    List<Group> getGroupsTheCurrentUserIsAMemberOf();   
    @Test
    public void testGetGroupsTheCurrentUserIsAMemberOf() throws ItemNotFoundException {
        System.out.println("test getGroupsTheCurrentUserIsAMemberOfr");

        MakeGroupA();
        MakeGroupB();
        // test itself

        List<Group> result = this.getAuthenticatedResource(getResource()
                .path("/groups/usermembership"))
                .accept(MediaType.APPLICATION_XML).get(GROUP_LIST_GENERICTYPE);

        assertEquals(1, result.size());
        assertEquals("group B", result.get(0).getName());
    }
//    Response transferItemOwnershipToGroup(String itemId, long groupId) throws IOException;   
//   

    @Test
    public void testTransferOwnership() throws Exception {
        System.out.println("test makeTransferOwnership");

        fillUpGroupA();
        fillUpGroupB();
        fillUpGroupC();
        fillUpGroupD();
        // test itself

        RegistryTestHelper.addComponent(baseRegistry, "test_component", false);
        RegistryTestHelper.addProfile(baseRegistry, "test_profile", false);
        String test_profile_id = ProfileDescription.PROFILE_PREFIX + "test_profile";
        String test_component_id = ComponentDescription.COMPONENT_PREFIX + "test_component";
        //I'm not a member
        ClientResponse cr = this.getAuthenticatedResource(getResource()
                .path("/items/" + test_profile_id + "/transferownership").queryParam("groupId", "3")).accept(MediaType.APPLICATION_XML).post(ClientResponse.class);
        assertEquals(403, cr.getStatus());

        //make me a member
        groupService.makeMember(DummyPrincipal.DUMMY_PRINCIPAL.getName(), "group C");
        assertTrue(groupService.userGroupMember(DummyPrincipal.DUMMY_PRINCIPAL.getName(), 3));

        cr = this.getAuthenticatedResource(getResource()
                .path("/items/" + test_profile_id + "/transferownership").queryParam("groupId", "3")).accept(MediaType.APPLICATION_XML).post(ClientResponse.class);
        assertEquals(200, cr.getStatus());
        cr = this.getAuthenticatedResource(getResource()
                .path("/items/" + test_component_id + "/transferownership").queryParam("groupId", "3")).accept(MediaType.APPLICATION_XML).post(ClientResponse.class);
        assertEquals(200, cr.getStatus());

        List<String> components = groupService.getComponentIdsInGroup(3);
        assertEquals(3, components.size());
        assertEquals(test_component_id, components.get(2));
        List<String> profiles = groupService.getProfileIdsInGroup(3);
        assertEquals(2, profiles.size());
        assertEquals(test_profile_id, profiles.get(1));

    }

    @Test
    public void testTransferOwnershipBetweenGroups() throws Exception {
        System.out.println("test makeTransferOwnership");

        fillUpGroupA();
        fillUpGroupB();
        fillUpGroupC();
        fillUpGroupD();
        // test itself

        // this profile was created and moved into group D by someone else
        String test_profile_id = ProfileDescription.PROFILE_PREFIX + "Dprofile-1";
        // try to move to group C
        ClientResponse cr = this.getAuthenticatedResource(getResource()
                .path("/items/" + test_profile_id + "/transferownership").queryParam("groupId", "2")).accept(MediaType.APPLICATION_XML).post(ClientResponse.class);
        // I am member of both groups, so should be allowed
        assertEquals("Moving between groups should be allowed as long as user is member", 200, cr.getStatus());

        // this component was created and moved into group D by someone else
        String test_component_id = ComponentDescription.COMPONENT_PREFIX + "Dcomponent-1";
        // try to move to group C
        cr = this.getAuthenticatedResource(getResource()
                .path("/items/" + test_component_id + "/transferownership").queryParam("groupId", "3")).accept(MediaType.APPLICATION_XML).post(ClientResponse.class);
        // I am not a member of groups C, so should not be allowed
        assertEquals("Moving between groups should not be allowed if user is not member", 403, cr.getStatus());

        // component in group C
        test_component_id = ComponentDescription.COMPONENT_PREFIX + "Ccomponent-1";
        // try to move to group B (which I am a member of)
        cr = this.getAuthenticatedResource(getResource()
                .path("/items/" + test_component_id + "/transferownership").queryParam("groupId", "2")).accept(MediaType.APPLICATION_XML).post(ClientResponse.class);
        // I am not a member of group C, so should not be allowed
        assertEquals("Moving between groups should not be allowed if user is not member", 403, cr.getStatus());

    }

    @Test
    public void testGetGroupProfilesAndComponents() throws Exception {

        System.out.println("test getGroupProfiles");

        fillUpGroupA();
        fillUpGroupB();
        fillUpGroupC();

        // lists
        List<ProfileDescription> response = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles").queryParam("registrySpace", REGISTRY_SPACE_GROUP).queryParam("groupId", "1")).accept(MediaType.APPLICATION_XML)
                .get(PROFILE_LIST_GENERICTYPE);
        assertEquals(1, response.size());

        List<ComponentDescription> responseC = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components").queryParam("registrySpace", REGISTRY_SPACE_GROUP).queryParam("groupId", "1")).accept(MediaType.APPLICATION_XML)
                .get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(2, responseC.size());

        response = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles").queryParam("registrySpace", REGISTRY_SPACE_GROUP).queryParam("groupId", "2")).accept(MediaType.APPLICATION_XML)
                .get(PROFILE_LIST_GENERICTYPE);
        assertEquals(1, response.size());

        responseC = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components").queryParam("registrySpace", REGISTRY_SPACE_GROUP).queryParam("groupId", "2")).accept(MediaType.APPLICATION_XML)
                .get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(2, responseC.size());

        ClientResponse clientResponse = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components").queryParam("registrySpace", REGISTRY_SPACE_GROUP).queryParam("groupId", "3")).accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);

        assertEquals(403, clientResponse.getStatus());

        // particular components and profiles
        ComponentSpec component = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "profile-1"))
                .accept(MediaType.APPLICATION_JSON).get(ComponentSpec.class);
        assertNotNull(component);
        assertEquals("profile-1", component.getComponent().getName());

        component = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "component-1"))
                .accept(MediaType.APPLICATION_JSON).get(ComponentSpec.class);
        assertNotNull(component);
        assertEquals("component-1", component.getComponent().getName());

        component = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "Bprofile-1"))
                .accept(MediaType.APPLICATION_JSON).get(ComponentSpec.class);
        assertNotNull(component);
        assertEquals("Bprofile-1", component.getComponent().getName());

        component = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "Bcomponent-1"))
                .accept(MediaType.APPLICATION_JSON).get(ComponentSpec.class);
        assertNotNull(component);
        assertEquals("Bcomponent-1", component.getComponent().getName());

        clientResponse = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "Cprofile-1"))
                .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(403, clientResponse.getStatus());

        clientResponse = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "Ccomponent-1"))
                .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(403, clientResponse.getStatus());

    }

    @Test
    public void testGetGroupComments() throws Exception {

        System.out.println("test getGroupComments");

        fillUpGroupA();
        fillUpGroupB();
        fillUpGroupC();

        RegistryTestHelper.addComment(baseRegistry, "COMMENTc1", ComponentDescription.COMPONENT_PREFIX + "component-1",
                "JUnit@test.com");
        RegistryTestHelper.addComment(baseRegistry, "COMMENTp1", ProfileDescription.PROFILE_PREFIX + "profile-1",
                "JUnit@test.com");
        RegistryTestHelper.addComment(baseRegistry, "COMMENTBc1", ComponentDescription.COMPONENT_PREFIX + "Bcomponent-1",
                "anotherPrincipal");
        RegistryTestHelper.addComment(baseRegistry, "COMMENTBp1", ProfileDescription.PROFILE_PREFIX + "Bprofile-1",
                "anotherPrincipal");
        (new RegistryTestHelper()).addCommentBypassAuthorisation(commentsDao, "COMMENTCc1", ComponentDescription.COMPONENT_PREFIX + "Ccomponent-1",
                "anotherPrincipal");
        (new RegistryTestHelper()).addCommentBypassAuthorisation(commentsDao, "COMMENTCp1", ProfileDescription.PROFILE_PREFIX + "Cprofile-1", "anotherPrincipal");

        // lists 
        List<Comment> response = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "component-1" + "/comments/"))
                .accept(MediaType.APPLICATION_XML)
                .get(COMMENT_LIST_GENERICTYPE);
        assertEquals(1, response.size());

        response = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + ProfileDescription.PROFILE_PREFIX + "profile-1" + "/comments/"))
                .accept(MediaType.APPLICATION_XML)
                .get(COMMENT_LIST_GENERICTYPE);
        assertEquals(1, response.size());

        response = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "Bcomponent-1" + "/comments/"))
                .accept(MediaType.APPLICATION_XML)
                .get(COMMENT_LIST_GENERICTYPE);
        assertEquals(1, response.size());

        response = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + ProfileDescription.PROFILE_PREFIX + "Bprofile-1" + "/comments/"))
                .accept(MediaType.APPLICATION_XML)
                .get(COMMENT_LIST_GENERICTYPE);
        assertEquals(1, response.size());

        ClientResponse clientResponse = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "Cprofile-1/comments"))
                .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(403, clientResponse.getStatus());

        clientResponse = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "Ccomponent-1/comments"))
                .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(403, clientResponse.getStatus());

        // particular comments
        Comment responseComment = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "component-1" + "/comments/1"))
                .accept(MediaType.APPLICATION_XML)
                .get(Comment.class);
        assertNotNull(responseComment);

        assertEquals(1, Long.parseLong(responseComment.getId()));

        responseComment = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "profile-1" + "/comments/2"))
                .accept(MediaType.APPLICATION_XML)
                .get(Comment.class);
        assertNotNull(responseComment);

        assertEquals(2, Long.parseLong(responseComment.getId()));

        responseComment = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "Bcomponent-1" + "/comments/3"))
                .accept(MediaType.APPLICATION_XML)
                .get(Comment.class);
        assertNotNull(responseComment);
        assertEquals(3, Long.parseLong(responseComment.getId()));

        responseComment = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "Bprofile-1" + "/comments/4"))
                .accept(MediaType.APPLICATION_XML)
                .get(Comment.class);
        assertNotNull(responseComment);
        assertEquals(4, Long.parseLong(responseComment.getId()));

        clientResponse = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "Cprofile-1/comments/6"))
                .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(403, clientResponse.getStatus());

        clientResponse = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "Ccomponent-1/comments/5"))
                .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(403, clientResponse.getStatus());

    }

    @Test
    public void testGetGroupRss() throws Exception {

        System.out.println("test getGroupRss");

        fillUpGroupA();
        fillUpGroupB();
        fillUpGroupC();

        // lists of profiles and components
        Rss response = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/rss").queryParam("registrySpace", REGISTRY_SPACE_GROUP).queryParam("groupId", "1")).accept(MediaType.APPLICATION_XML)
                .get(Rss.class);
        assertEquals(1, response.getChannel().getItem().size());

        response = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/rss").queryParam("registrySpace", REGISTRY_SPACE_GROUP).queryParam("groupId", "1")).accept(MediaType.APPLICATION_XML)
                .get(Rss.class);
        assertEquals(2, response.getChannel().getItem().size());

        response = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/rss").queryParam("registrySpace", REGISTRY_SPACE_GROUP).queryParam("groupId", "2")).accept(MediaType.APPLICATION_XML)
                .get(Rss.class);
        assertEquals(1, response.getChannel().getItem().size());

        response = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/rss").queryParam("registrySpace", REGISTRY_SPACE_GROUP).queryParam("groupId", "2")).accept(MediaType.APPLICATION_XML)
                .get(Rss.class);
        assertEquals(2, response.getChannel().getItem().size());

        ClientResponse clientResponse = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components").queryParam("registrySpace", REGISTRY_SPACE_GROUP).queryParam("groupId", "3")).accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);

        assertEquals(403, clientResponse.getStatus());

        RegistryTestHelper.addComment(baseRegistry, "COMMENTc1", ComponentDescription.COMPONENT_PREFIX + "component-1",
                "JUnit@test.com");
        RegistryTestHelper.addComment(baseRegistry, "COMMENTp1", ProfileDescription.PROFILE_PREFIX + "profile-1",
                "JUnit@test.com");
        RegistryTestHelper.addComment(baseRegistry, "COMMENTBc1", ComponentDescription.COMPONENT_PREFIX + "Bcomponent-1",
                "anotherPrincipal");
        RegistryTestHelper.addComment(baseRegistry, "COMMENTBp1", ProfileDescription.PROFILE_PREFIX + "Bprofile-1",
                "anotherPrincipal");
        (new RegistryTestHelper()).addCommentBypassAuthorisation(commentsDao, "COMMENTCc1", ComponentDescription.COMPONENT_PREFIX + "Ccomponent-1",
                "anotherPrincipal");
        (new RegistryTestHelper()).addCommentBypassAuthorisation(commentsDao, "COMMENTCp1", ProfileDescription.PROFILE_PREFIX + "Cprofile-1", "anotherPrincipal");

        // lists 
        response = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "component-1" + "/comments/rss"))
                .accept(MediaType.APPLICATION_XML)
                .get(Rss.class);
        assertEquals(1, response.getChannel().getItem().size());

        response = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "profile-1" + "/comments/rss"))
                .accept(MediaType.APPLICATION_XML)
                .get(Rss.class);
        assertEquals(1, response.getChannel().getItem().size());

        response = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "Bcomponent-1" + "/comments/rss"))
                .accept(MediaType.APPLICATION_XML)
                .get(Rss.class);
        assertEquals(1, response.getChannel().getItem().size());

        response = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "Bprofile-1" + "/comments/rss"))
                .accept(MediaType.APPLICATION_XML)
                .get(Rss.class);
        assertEquals(1, response.getChannel().getItem().size());

        clientResponse = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "Cprofile-1/comments/rss"))
                .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(403, clientResponse.getStatus());

        clientResponse = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "Ccomponent-1/comments/rss"))
                .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(403, clientResponse.getStatus());

        // particular comments
        Comment responseComment = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "component-1" + "/comments/1"))
                .accept(MediaType.APPLICATION_XML)
                .get(Comment.class);
        assertNotNull(responseComment);

        assertEquals(1, Long.parseLong(responseComment.getId()));

        responseComment = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "profile-1" + "/comments/2"))
                .accept(MediaType.APPLICATION_XML)
                .get(Comment.class);
        assertNotNull(responseComment);

        assertEquals(2, Long.parseLong(responseComment.getId()));

        responseComment = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "Bcomponent-1" + "/comments/3"))
                .accept(MediaType.APPLICATION_XML)
                .get(Comment.class);
        assertNotNull(responseComment);
        assertEquals(3, Long.parseLong(responseComment.getId()));

        responseComment = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "Bprofile-1" + "/comments/4"))
                .accept(MediaType.APPLICATION_XML)
                .get(Comment.class);
        assertNotNull(responseComment);
        assertEquals(4, Long.parseLong(responseComment.getId()));

        clientResponse = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "Cprofile-1/comments/6"))
                .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(403, clientResponse.getStatus());

        clientResponse = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "Ccomponent-1/comments/5"))
                .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(403, clientResponse.getStatus());

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
    public void testUpdateGroupComponentAndProfile() throws Exception {

        System.out.println("test updateGorupComponent");

        fillUpGroupA();
        fillUpGroupB();
        fillUpGroupC();

        FormDataMultiPart form = createFormData(
                RegistryTestHelper.getComponentTestContentAsStream("TESTNAME"),
                "UPDATE DESCRIPTION!");
        ClientResponse cResponse = getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "component-1" + "/update")).type(
                        MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                cResponse.getStatus());
        RegisterResponse response = cResponse.getEntity(RegisterResponse.class);
        assertTrue(response.isRegistered());
        assertFalse(response.isProfile());
        assertTrue(response.isPrivate());
        ComponentDescription desc = (ComponentDescription) response.getDescription();
        assertNotNull(desc);
        assertEquals("TESTNAME", desc.getName());
        assertEquals("TESTNAME description", desc.getDescription());

        form = createFormData(
                RegistryTestHelper.getComponentTestContentAsStream("TESTNAME2"),
                "UPDATE DESCRIPTION!");
        cResponse = getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "Bcomponent-1" + "/update")).type(
                        MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                cResponse.getStatus());
        response = cResponse.getEntity(RegisterResponse.class);
        assertTrue(response.isRegistered());
        assertFalse(response.isProfile());
        assertTrue(response.isPrivate());
        desc = (ComponentDescription) response.getDescription();
        assertNotNull(desc);
        assertEquals("TESTNAME2", desc.getName());
        assertEquals("TESTNAME2 description", desc.getDescription());

        form = createFormData(
                RegistryTestHelper.getComponentTestContentAsStream("TESTNAME3"),
                "UPDATE DESCRIPTION!");
        cResponse = getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "Ccomponent-1" + "/update")).type(
                        MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
        assertEquals(403, cResponse.getStatus());

        // profile
        form = createFormData(RegistryTestHelper.getTestProfileContent("TESTNAME4"),
                "UPDATE DESCRIPTION!");
        cResponse = getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "profile-1" + "/update")).type(
                        MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                cResponse.getStatus());
        response = cResponse.getEntity(RegisterResponse.class);
        assertTrue(response.isRegistered());
        assertTrue(response.isProfile());
        assertTrue(response.isPrivate());
        ProfileDescription pdesc = (ProfileDescription) response.getDescription();
        assertNotNull(pdesc);
        assertEquals("TESTNAME4", pdesc.getName());
        assertEquals("TESTNAME4 description", pdesc.getDescription());

        form = createFormData(
                RegistryTestHelper.getTestProfileContent("TESTNAME5"),
                "UPDATE DESCRIPTION!");
        cResponse = getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "Bprofile-1" + "/update")).type(
                        MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                cResponse.getStatus());
        response = cResponse.getEntity(RegisterResponse.class);
        assertTrue(response.isRegistered());
        assertTrue(response.isProfile());
        assertTrue(response.isPrivate());
        pdesc = (ProfileDescription) response.getDescription();
        assertNotNull(pdesc);
        assertEquals("TESTNAME5", pdesc.getName());
        assertEquals("TESTNAME5 description", pdesc.getDescription());

        form = createFormData(
                RegistryTestHelper.getTestProfileContent("TESTNAME6"),
                "UPDATE DESCRIPTION!");
        cResponse = getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "Cprofile-1" + "/update")).type(
                        MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
        assertEquals(403, cResponse.getStatus());

    }

    @Test
    public void testPublishGroupProfileAndComponent() throws Exception {

        System.out.println("testPublishProfile");

        fillUpGroupA();
        fillUpGroupB();
        fillUpGroupC();

        baseRegistry.setRegistrySpace(RegistrySpace.PUBLISHED);

        FormDataMultiPart form = createFormData(
                RegistryTestHelper.getTestProfileContent("publishedName"),
                "Published");
        RegisterResponse response = getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "profile-1" + "/publish"))
                .type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);
        assertFalse(response.isPrivate());
        assertTrue(response.isProfile());
        assertEquals(ProfileDescription.PROFILE_PREFIX + "profile-1", response.getDescription().getId());

        List<ProfileDescription> profiles = baseRegistry.getProfileDescriptions(null);
        assertEquals(1, profiles.size());
        ProfileDescription profileDescription = profiles.get(0);
        assertEquals(ProfileDescription.PROFILE_PREFIX + "profile-1", profileDescription.getId());
        assertEquals("publishedName description", profileDescription.getDescription());

        // not my profile from "my" group
        form = createFormData(
                RegistryTestHelper.getTestProfileContent("publishedName2"),
                "Published");
        response = getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "Bprofile-1" + "/publish"))
                .type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);
        assertFalse(response.isPrivate());
        assertTrue(response.isProfile());
        assertEquals(ProfileDescription.PROFILE_PREFIX + "Bprofile-1", response.getDescription().getId());

        profiles = baseRegistry.getProfileDescriptions(null);
        assertEquals(2, profiles.size());
        profileDescription = profiles.get(1);
        assertEquals(ProfileDescription.PROFILE_PREFIX + "Bprofile-1", profileDescription.getId());
        assertEquals("publishedName2 description", profileDescription.getDescription());

        // not my profile, not my group 
        form = createFormData(
                RegistryTestHelper.getTestProfileContent("publishedName3"),
                "Published");
        ClientResponse cr = getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "Cprofile-1" + "/publish"))
                .type(MediaType.MULTIPART_FORM_DATA).post(
                ClientResponse.class, form);
        assertEquals(403, cr.getStatus());
        profiles = baseRegistry.getProfileDescriptions(null);
        assertEquals(2, profiles.size());

        /// components 
        form = createFormData(
                RegistryTestHelper.getComponentTestContentAsStream("publishedName4"),
                "Published");
        response = getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "component-1" + "/publish"))
                .type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);
        assertFalse(response.isPrivate());
        assertFalse(response.isProfile());
        assertEquals(ComponentDescription.COMPONENT_PREFIX + "component-1", response.getDescription().getId());

        List<ComponentDescription> components = baseRegistry.getComponentDescriptions(null);
        assertEquals(1, components.size());
        ComponentDescription componentDescription = components.get(0);
        assertEquals(ComponentDescription.COMPONENT_PREFIX + "component-1", componentDescription.getId());
        assertEquals("publishedName4 description", componentDescription.getDescription());

        // not my profile from "my" group
        form = createFormData(
                RegistryTestHelper.getComponentTestContentAsStream("publishedName5"),
                "Published");
        response = getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "Bcomponent-1" + "/publish"))
                .type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);
        assertFalse(response.isPrivate());
        assertFalse(response.isProfile());
        assertEquals(ComponentDescription.COMPONENT_PREFIX + "Bcomponent-1", response.getDescription().getId());

        components = baseRegistry.getComponentDescriptions(null);
        assertEquals(2, components.size());
        componentDescription = components.get(1);
        assertEquals(ComponentDescription.COMPONENT_PREFIX + "Bcomponent-1", componentDescription.getId());
        assertEquals("publishedName5 description", componentDescription.getDescription());

        // not my profile, not my group 
        form = createFormData(
                RegistryTestHelper.getComponentTestContentAsStream("publishedName6"),
                "Published");
        cr = getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "Ccomponent-1" + "/publish"))
                .type(MediaType.MULTIPART_FORM_DATA).post(
                ClientResponse.class, form);
        assertEquals(403, cr.getStatus());
        components = baseRegistry.getComponentDescriptions(null);
        assertEquals(2, components.size());
    }

    @Test
    public void testDeleteProfileAndComponentFromGroup() throws Exception {

        System.out.println("test deleteProfileAndComponentFromGroup");

        fillUpGroupA();
        fillUpGroupB();
        fillUpGroupC();

        ClientResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX
                + "component-1").delete(ClientResponse.class);
        assertEquals(200, response.getStatus());

        response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX
                + "component-1").get(ClientResponse.class);
        assertEquals(404, response.getStatus());

        // my group, not my component 
        response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX
                + "Bcomponent-1").delete(ClientResponse.class);
        assertEquals(200, response.getStatus());

        response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX
                + "Bcomponent-1").get(ClientResponse.class);
        assertEquals(404, response.getStatus());

        // not my group
        response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX
                + "Ccomponent-1").delete(ClientResponse.class);
        assertEquals(403, response.getStatus());

        baseRegistry.setRegistrySpace(RegistrySpace.GROUP);
        baseRegistry.setGroupId(1);
        assertEquals(1, baseRegistry.getComponentDescriptions(null).size());
        baseRegistry.setGroupId(2);
        assertEquals(1, baseRegistry.getComponentDescriptions(null).size());
//        baseRegistry.setGroupId(3);
//        assertEquals(2, baseRegistry.getComponentDescriptions(null).size());

        baseRegistry.setRegistrySpace(null);
        baseRegistry.setGroupId(null);

        // profiles
        response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile-1").delete(ClientResponse.class);
        assertEquals(200, response.getStatus());

        response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile-1").get(ClientResponse.class);
        assertEquals(404, response.getStatus());

        // my group, not my component 
        response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "Bprofile-1").delete(ClientResponse.class);
        assertEquals(200, response.getStatus());

        response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "Bprofile-1").get(ClientResponse.class);
        assertEquals(404, response.getStatus());

        // not my group
        response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "Cprofile-1").delete(ClientResponse.class);
        assertEquals(403, response.getStatus());

        baseRegistry.setRegistrySpace(RegistrySpace.GROUP);
        baseRegistry.setGroupId(1);
        assertEquals(0, baseRegistry.getProfileDescriptions(null).size());
        baseRegistry.setGroupId(2);
        assertEquals(0, baseRegistry.getProfileDescriptions(null).size());
    }

    @Test
    public void testDeleteCommentFromGroupComponentAndProfile() throws Exception {

        System.out.println("test deleteCommentFromGroupComponent");

        fillUpGroupA();
        fillUpGroupB();
        fillUpGroupC();

        RegistryTestHelper.addComment(baseRegistry, "COMMENTc1", ComponentDescription.COMPONENT_PREFIX + "component-1",
                "JUnit@test.com");
        RegistryTestHelper.addComment(baseRegistry, "COMMENTp1", ProfileDescription.PROFILE_PREFIX + "profile-1",
                "JUnit@test.com");
        RegistryTestHelper.addComment(baseRegistry, "COMMENTBc1", ComponentDescription.COMPONENT_PREFIX + "Bcomponent-1",
                "anotherPrincipal");
        RegistryTestHelper.addComment(baseRegistry, "COMMENTBp1", ProfileDescription.PROFILE_PREFIX + "Bprofile-1",
                "anotherPrincipal");
        (new RegistryTestHelper()).addCommentBypassAuthorisation(commentsDao, "COMMENTCc1", ComponentDescription.COMPONENT_PREFIX + "Ccomponent-1",
                "anotherPrincipal");
        (new RegistryTestHelper()).addCommentBypassAuthorisation(commentsDao, "COMMENTCp1", ProfileDescription.PROFILE_PREFIX + "Cprofile-1", "anotherPrincipal");

        ClientResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "component-1/comments/1").delete(
                        ClientResponse.class);
        assertEquals(200, response.getStatus());
        response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "component-1/comments/1").get(
                        ClientResponse.class);
        assertEquals(404, response.getStatus());

        response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "Bcomponent-1/comments/3").delete(
                        ClientResponse.class);
        assertEquals(403, response.getStatus());

        response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX + "Ccomponent-1/comments/5").delete(
                        ClientResponse.class);
        assertEquals(403, response.getStatus());

        response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "profile-1/comments/2").delete(
                        ClientResponse.class);
        assertEquals(200, response.getStatus());
        response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "profile-1/comments/2").get(
                        ClientResponse.class);
        assertEquals(404, response.getStatus());

        response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "Bprofile-1/comments/4").delete(
                        ClientResponse.class);
        assertEquals(403, response.getStatus());

        response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "Cprofile-1/comments/6").delete(
                        ClientResponse.class);
        assertEquals(403, response.getStatus());

    }

    @Test
    public void testRegisterCommentInGroup() throws Exception {

        System.out.println("testRegisterCommmentInGroup");

        fillUpGroupB();
        fillUpGroupC();

        FormDataMultiPart form = new FormDataMultiPart();
        String id = ProfileDescription.PROFILE_PREFIX + "Bprofile-1";
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD,
                RegistryTestHelper.getCommentTestContentStringForProfile("comment1", id),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        CommentResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + id + "/comments").type(
                        MediaType.MULTIPART_FORM_DATA)
                .post(CommentResponse.class, form);
        assertTrue(response.isRegistered());
        assertTrue(response.isPrivate());
        Comment comment = response.getComment();
        assertNotNull(comment);
        assertEquals("comment1", comment.getComment());
        assertEquals("Database test user", comment.getUserName());
        Assert.notNull(comment.getCommentDate());
        assertEquals(1, Long.parseLong(comment.getId()));

        // User id should not be serialized!
        assertEquals(0, comment.getUserId());

        form = new FormDataMultiPart();
        id = ComponentDescription.COMPONENT_PREFIX + "Bcomponent-1";
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD,
                RegistryTestHelper.getCommentTestContentStringForComponent("comment2", id),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + id + "/comments").type(
                        MediaType.MULTIPART_FORM_DATA)
                .post(CommentResponse.class, form);
        assertTrue(response.isRegistered());
        assertTrue(response.isPrivate());
        comment = response.getComment();
        assertNotNull(comment);
        assertEquals("comment2", comment.getComment());
        assertEquals("Database test user", comment.getUserName());
        Assert.notNull(comment.getCommentDate());
        assertEquals(2, Long.parseLong(comment.getId()));

        // User id should not be serialized!
        assertEquals(0, comment.getUserId());

        // not my group
        form = new FormDataMultiPart();
        id = ProfileDescription.PROFILE_PREFIX + "Cprofile-1";
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD,
                RegistryTestHelper.getCommentTestContentStringForProfile("comment3", id),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        ClientResponse cresponse = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + id + "/comments").type(
                        MediaType.MULTIPART_FORM_DATA)
                .post(ClientResponse.class, form);
        assertEquals(403, cresponse.getStatus());

        form = new FormDataMultiPart();
        id = ComponentDescription.COMPONENT_PREFIX + "Ccomponent-1";
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD,
                RegistryTestHelper.getCommentTestContentStringForComponent("comment4", id),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        cresponse = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + id + "/comments").type(
                        MediaType.MULTIPART_FORM_DATA)
                .post(ClientResponse.class, form);
        assertEquals(403, cresponse.getStatus());
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

    @Test //ok
    public void testGetGroupComponentsUnauthenticated() throws Exception {
        fillUpGroupA();

        ClientResponse response = getResource()
                .path(REGISTRY_BASE + "/components")
                .queryParam(REGISTRY_SPACE_PARAM, REGISTRY_SPACE_GROUP)
                .queryParam("groupId", "1")
                .accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);

        assertEquals(401, response.getStatus());
    }

    @Test //ok
    public void testGetGroupComponentsNoMember() throws Exception {
        fillUpGroupA();
        fillUpGroupB();
        fillUpGroupC();

        ClientResponse response = getAuthenticatedResource(
                getResource()
                        .path(REGISTRY_BASE + "/components")
                        .queryParam(REGISTRY_SPACE_PARAM, REGISTRY_SPACE_GROUP)
                        .queryParam(GROUPID_PARAM, "3"))
                .accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);
        assertEquals(403, response.getStatus());
    }

    @Test //ok
    public void testGetGroupComponentsAuthenticated() throws Exception {
        fillUpGroupA();

        List<ComponentDescription> response = getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components")
                .queryParam(REGISTRY_SPACE_PARAM, REGISTRY_SPACE_GROUP)
                .queryParam(GROUPID_PARAM, "1"))
                .accept(MediaType.APPLICATION_XML)
                .get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(2, response.size());
        assertEquals("component-1", response.get(0).getName());
    }

    @Test //ok
    public void testGetGroupProfilesUnauthenticated() throws Exception {
        fillUpGroupA();

        ClientResponse response = (getResource()
                .path(REGISTRY_BASE + "/profiles")
                .queryParam(REGISTRY_SPACE_PARAM, REGISTRY_SPACE_GROUP)
                .queryParam(GROUPID_PARAM, "1"))
                .accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);
        assertEquals(401, response.getStatus());
    }

    @Test //ok
    public void testGetGroupProfilesAuthenticated() throws Exception {
        fillUpGroupA();

        List<ProfileDescription> response = getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles")
                .queryParam(REGISTRY_SPACE_PARAM, REGISTRY_SPACE_GROUP)
                .queryParam(GROUPID_PARAM, "1"))
                .accept(MediaType.APPLICATION_XML)
                .get(PROFILE_LIST_GENERICTYPE);
        assertEquals(1, response.size());
        assertEquals("profile-1", response.get(0).getName());
    }
}
