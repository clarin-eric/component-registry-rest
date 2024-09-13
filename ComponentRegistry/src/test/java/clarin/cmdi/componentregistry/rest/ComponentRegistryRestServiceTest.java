package clarin.cmdi.componentregistry.rest;

import static clarin.cmdi.componentregistry.ComponentRegistryConstants.REGISTRY_ID;
import clarin.cmdi.componentregistry.validation.ComponentSpecValidator;
import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.components.ComponentSpec;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryTestDatabase;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.CommentResponse;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ComponentStatus;
import static clarin.cmdi.componentregistry.model.ComponentStatus.DEVELOPMENT;
import static clarin.cmdi.componentregistry.model.ComponentStatus.PRODUCTION;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.ProfileDescriptionsObject;
import clarin.cmdi.componentregistry.model.RegisterResponse;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestServiceTestCase.COMPONENT_LIST_GENERICTYPE;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.REGISTRY_SPACE_PARAM;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.REGISTRY_SPACE_PRIVATE;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.STATUS_FILTER_PARAM;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.multipart.FormDataMultiPart;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

/**
 * Launches a servlet container environment and performs HTTP calls to the
 * {@link ComponentRegistryRestService}
 *
 * @author twan.goosen@mpi.nl
 * @author george.georgovassilis@mpi.nl
 *
 */
public class ComponentRegistryRestServiceTest extends ComponentRegistryRestServiceTestCase {

    @Autowired
    private ComponentRegistryFactory componentRegistryFactory;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private ComponentRegistry baseRegistry;

    public static final String REGISTRY_BASE = "/registry/1.x";
    public static final String NON_CANONICAL_REGISTRY_BASE = "/registry/1.1";

    @Before
    public void init() {
        ComponentRegistryTestDatabase.resetAndCreateAllTables(jdbcTemplate);
        createUserRecord();
        baseRegistry = componentRegistryFactory.getBaseRegistry(DummyPrincipal.DUMMY_CREDENTIALS);
    }

    private String expectedUserId(String principal) {
        return getExpectedUserId(principal).toString();
    }
    private Comment profile1Comment1;
    private Comment profile1Comment2;
    private Comment component1Comment3;
    private Comment component1Comment4;

    private void fillUpPublicItems() throws Exception {

        RegistryTestHelper.addProfile(baseRegistry, "profile2", true);
        RegistryTestHelper.addProfile(baseRegistry, "profile1", true);
        RegistryTestHelper.addComponent(baseRegistry,
                "component2", true);
        RegistryTestHelper.addComponent(baseRegistry,
                "component1", true);
        profile1Comment2 = RegistryTestHelper.addComment(baseRegistry, "comment2",
                ProfileDescription.PROFILE_PREFIX + "profile1",
                "JUnit@test.com");
        profile1Comment1 = RegistryTestHelper.addComment(baseRegistry, "comment1",
                ProfileDescription.PROFILE_PREFIX + "profile1",
                "JUnit@test.com");
        component1Comment3 = RegistryTestHelper.addComment(baseRegistry, "comment3",
                ComponentDescription.COMPONENT_PREFIX + "component1",
                "JUnit@test.com");
        component1Comment4 = RegistryTestHelper.addComment(baseRegistry, "comment4",
                ComponentDescription.COMPONENT_PREFIX + "component1",
                "JUnit@test.com");
    }

    /**
     * Will insert: - profile 3: private by current user - profile 4: private by
     * other user - component 3: private by current user - component 4: private
     * by other user
     *
     * @throws Exception
     */
    private void fillUpPrivateItems() throws Exception {
        RegistryTestHelper.addProfile(baseRegistry, "profile3", false);
        RegistryTestHelper.addProfileAnotherPrincipal(baseRegistry, "profile4", false);
        RegistryTestHelper.addComponent(baseRegistry,
                "component3", false);
        RegistryTestHelper.addComponentAnotherPrincipal(baseRegistry, "component4", false);
        RegistryTestHelper.addComment(baseRegistry, "comment5",
                ProfileDescription.PROFILE_PREFIX + "profile3",
                "JUnit@test.com");
        RegistryTestHelper.addComment(baseRegistry, "comment7",
                ComponentDescription.COMPONENT_PREFIX + "component3",
                "JUnit@test.com");
    }

    @Test
    public void testGetPublicProfilesUnauthenticated() throws Exception {

        System.out.println("testGetPublicProfiles");

        fillUpPublicItems();

        RegistryTestHelper.addProfile(baseRegistry, "PROFILE2", true);
        List<ProfileDescription> response = getResource()
                .path(REGISTRY_BASE + "/profiles").accept(MediaType.APPLICATION_XML)
                .get(PROFILE_LIST_GENERICTYPE);
        assertEquals(3, response.size());
        response = getResource()
                .path(REGISTRY_BASE + "/profiles")
                .accept(MediaType.APPLICATION_JSON)
                .get(PROFILE_LIST_GENERICTYPE);
        assertEquals(3, response.size());
        assertEquals("profile1", response.get(0).getName());
        assertEquals("PROFILE2", response.get(1).getName());
        assertEquals("profile2", response.get(2).getName());
    }

    @Test
    public void testGetPublicProfilesWithStatus() throws Exception {

        System.out.println("testGetPublicProfiles");

        fillUpPublicItems();

        //add profile and deprecate
        ProfileDescription newProfile = RegistryTestHelper.addProfile(baseRegistry, "PROFILE2", true);
        newProfile.setStatus(ComponentStatus.DEPRECATED);
        baseRegistry.update(newProfile, RegistryTestHelper.getTestProfile("PROFILE2", "deprecated"), true);

        List<ProfileDescription> response = getResource()
                .path(REGISTRY_BASE + "/profiles").accept(MediaType.APPLICATION_XML)
                .get(PROFILE_LIST_GENERICTYPE);
        assertEquals(2, response.size());
        response = getResource()
                .path(REGISTRY_BASE + "/profiles").queryParam(STATUS_FILTER_PARAM, "production").accept(MediaType.APPLICATION_XML)
                .get(PROFILE_LIST_GENERICTYPE);
        assertEquals(2, response.size());
        response = getResource()
                .path(REGISTRY_BASE + "/profiles").queryParam(STATUS_FILTER_PARAM, "deprecated").accept(MediaType.APPLICATION_XML)
                .get(PROFILE_LIST_GENERICTYPE);
        assertEquals(1, response.size());
        response = getResource()
                .path(REGISTRY_BASE + "/profiles").queryParam(STATUS_FILTER_PARAM, "*").accept(MediaType.APPLICATION_XML)
                .get(PROFILE_LIST_GENERICTYPE);
        assertEquals(3, response.size());
    }

    @Test
    public void testGetPublicProfilesAuthenticated() throws Exception {

        System.out.println("testGetPublicProfiles");

        fillUpPublicItems();

        RegistryTestHelper.addProfile(baseRegistry, "PROFILE2", true);
        List<ProfileDescription> response = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles")).accept(MediaType.APPLICATION_XML)
                .get(PROFILE_LIST_GENERICTYPE);
        assertEquals(3, response.size());
        response = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles"))
                .accept(MediaType.APPLICATION_JSON)
                .get(PROFILE_LIST_GENERICTYPE);
        assertEquals(3, response.size());
        assertEquals("profile1", response.get(0).getName());
        assertEquals("PROFILE2", response.get(1).getName());
        assertEquals("profile2", response.get(2).getName());
    }

    @Test //ok
    public void testGetPublicComponentsUnauthenticated() throws Exception {
        fillUpPublicItems();

        RegistryTestHelper.addComponent(baseRegistry, "COMPONENT2", true);
        List<ComponentDescription> response = (getResource()
                .path(REGISTRY_BASE + "/components")).accept(MediaType.APPLICATION_XML)
                .get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(3, response.size());
        response = (getResource()
                .path(REGISTRY_BASE + "/components"))
                .accept(MediaType.APPLICATION_JSON)
                .get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(3, response.size());
        assertEquals("component1", response.get(0).getName());
        assertEquals("COMPONENT2", response.get(1).getName());
        assertEquals("component2", response.get(2).getName());
    }

    @Test //ok
    public void testGetPublicComponentsAuthenticated() throws Exception {
        fillUpPublicItems();

        RegistryTestHelper.addComponent(baseRegistry, "COMPONENT2", true);
        List<ComponentDescription> response = (getResource()
                .path(REGISTRY_BASE + "/components")).accept(MediaType.APPLICATION_XML)
                .get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(3, response.size());
        response = (getResource()
                .path(REGISTRY_BASE + "/components"))
                .accept(MediaType.APPLICATION_JSON)
                .get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(3, response.size());
        assertEquals("component1", response.get(0).getName());
        assertEquals("COMPONENT2", response.get(1).getName());
        assertEquals("component2", response.get(2).getName());
    }

    @Test //ok
    public void testGetPublicComponentsWithStatus() throws Exception {
        fillUpPublicItems();

        //add component and deprecate
        ComponentDescription newComponent = RegistryTestHelper.addComponent(baseRegistry, "COMPONENT2", true);
        newComponent.setStatus(ComponentStatus.DEPRECATED);
        baseRegistry.update(newComponent, RegistryTestHelper.getTestComponent("COMPONENT2", "deprecated"), true);

        List<ComponentDescription> response = getResource()
                .path(REGISTRY_BASE + "/components").accept(MediaType.APPLICATION_XML)
                .get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(2, response.size());
        response = getResource()
                .path(REGISTRY_BASE + "/components").queryParam(STATUS_FILTER_PARAM, "production").accept(MediaType.APPLICATION_XML)
                .get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(2, response.size());
        response = getResource()
                .path(REGISTRY_BASE + "/components").queryParam(STATUS_FILTER_PARAM, "deprecated").accept(MediaType.APPLICATION_XML)
                .get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(1, response.size());
        response = getResource()
                .path(REGISTRY_BASE + "/components").queryParam(STATUS_FILTER_PARAM, "*").accept(MediaType.APPLICATION_XML)
                .get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(3, response.size());
    }

    @Test  //ok
    public void testGetUserComponents() throws Exception {

        System.out.println("testGetUserComponents");

        fillUpPrivateItems();

        List<ComponentDescription> response = this.getUserComponents();
        assertEquals(1, response.size());

        ////
        fillUpPublicItems();
        response = getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components")).accept(
                MediaType.APPLICATION_JSON).get(COMPONENT_LIST_GENERICTYPE);
        assertEquals("Get public components", 2, response.size());

        ClientResponse cResponse = getResource().path(REGISTRY_BASE + "/components")
                .queryParam(REGISTRY_SPACE_PARAM, REGISTRY_SPACE_PRIVATE)
                .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals("Trying to get userspace components without credentials", 401,
                cResponse.getStatus());
    }

    @Test  //ok
    public void testGetUserProfiles() throws Exception {

        System.out.println("testGetUserComponents");

        fillUpPrivateItems();

        List<ProfileDescription> response = this.getUserProfiles();
        assertEquals(1, response.size());

        fillUpPublicItems();
        response = getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/profiles")).accept(
                MediaType.APPLICATION_JSON).get(PROFILE_LIST_GENERICTYPE);
        assertEquals("Get public profiles", 2, response.size());

        ClientResponse cResponse = getResource().path(REGISTRY_BASE + "/profiles")
                .queryParam(REGISTRY_SPACE_PARAM, REGISTRY_SPACE_PRIVATE)
                .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals("Trying to get userspace profiles without credentials", 401,
                cResponse.getStatus());
    }

    @Test
    public void testGetPublicComponent() throws Exception {

        fillUpPublicItems();

        String id = ComponentDescription.COMPONENT_PREFIX + "component1";
        String id2 = ComponentDescription.COMPONENT_PREFIX + "component2";

        // make an unauthenticated reponse
        ComponentSpec component = getResource()
                .path(REGISTRY_BASE + "/components/" + id)
                .accept(MediaType.APPLICATION_XML).get(ComponentSpec.class);
        assertNotNull("unauthenticated component request", component);
        assertEquals("component1", component.getComponent().getName());

        // make an authenticated reponse
        component = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + id))
                .accept(MediaType.APPLICATION_XML).get(ComponentSpec.class);
        assertNotNull("authenticated component request", component);
        assertEquals("component1", component.getComponent().getName());

        component = this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components/" + id2))
                .accept(MediaType.APPLICATION_XML).get(ComponentSpec.class);
        assertNotNull(component);
        assertEquals("component2", component.getComponent().getName());
        assertEquals(id2, component.getHeader().getID());
        assertEquals("component2", component.getHeader().getName());
        assertEquals("component2 description", component.getHeader().getDescription());
    }

    @Test
    public void testGetPublicComponentJson() throws Exception {

        fillUpPublicItems();

        String id = ComponentDescription.COMPONENT_PREFIX + "component1";
        String id2 = ComponentDescription.COMPONENT_PREFIX + "component2";

        // make an unauthenticated reponse
        ComponentSpec component = getResource()
                .path(REGISTRY_BASE + "/components/" + id)
                .accept(MediaType.APPLICATION_JSON).get(ComponentSpec.class);
        assertNotNull("unauthenticated component request", component);
        assertEquals("component1", component.getComponent().getName());

        // make an authenticated reponse
        component = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + id))
                .accept(MediaType.APPLICATION_JSON).get(ComponentSpec.class);
        assertNotNull("authenticated component request", component);
        assertEquals("component1", component.getComponent().getName());

        component = this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components/" + id2))
                .accept(MediaType.APPLICATION_JSON).get(ComponentSpec.class);
        assertNotNull(component);
        assertEquals("component2", component.getComponent().getName());
        assertEquals(id2, component.getHeader().getID());
        assertEquals("component2", component.getHeader().getName());
        assertEquals("component2 description", component.getHeader().getDescription());
    }

    @Test
    public void testGetPublicProfile() throws Exception {

        fillUpPublicItems();

        String id = ProfileDescription.PROFILE_PREFIX + "profile1";
        String id2 = ProfileDescription.PROFILE_PREFIX + "profile2";

        // make an unauthenticated reponse
        ComponentSpec profile = getResource()
                .path(REGISTRY_BASE + "/profiles/" + id)
                .accept(MediaType.APPLICATION_XML).get(ComponentSpec.class);
        assertNotNull("unauthenticated component request", profile);
//        assertEquals("Access", profile.getComponent().getName());

        // make an authenticated reponse
        profile = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + id))
                .accept(MediaType.APPLICATION_XML).get(ComponentSpec.class);
        assertNotNull("authenticated component request", profile);
//        assertEquals("Access", profile.getComponent().getName());

        profile = this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/profiles/" + id2))
                .accept(MediaType.APPLICATION_XML).get(ComponentSpec.class);
        assertNotNull(profile);
//        assertEquals("Access", profile.getComponent().getName());
        assertEquals(id2, profile.getHeader().getID());
        assertEquals("profile2", profile.getHeader().getName());
        assertEquals("profile2 description", profile.getHeader().getDescription());
    }

    @Test
    public void testGetPublicProfileJson() throws Exception {

        fillUpPublicItems();

        String id = ProfileDescription.PROFILE_PREFIX + "profile1";
        String id2 = ProfileDescription.PROFILE_PREFIX + "profile2";

        // make an unauthenticated reponse
        ComponentSpec profile = getResource()
                .path(REGISTRY_BASE + "/profiles/" + id)
                .accept(MediaType.APPLICATION_JSON).get(ComponentSpec.class);
        assertNotNull("unauthenticated component request", profile);
//        assertEquals("Access", profile.getComponent().getName());

        // make an authenticated reponse
        profile = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + id))
                .accept(MediaType.APPLICATION_JSON).get(ComponentSpec.class);
        assertNotNull("authenticated component request", profile);
//        assertEquals("Access", profile.getComponent().getName());

        profile = this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/profiles/" + id2))
                .accept(MediaType.APPLICATION_JSON).get(ComponentSpec.class);
        assertNotNull(profile);
//        assertEquals("Access", profile.getComponent().getName());
        assertEquals(id2, profile.getHeader().getID());
        assertEquals("profile2", profile.getHeader().getName());
        assertEquals("profile2 description", profile.getHeader().getDescription());
    }

    @Test   // ok    
    public void testGetCommentsInPublicProfile() throws Exception {

        System.out.println("testGetCommentsInPublicPrfile");

        fillUpPublicItems();

        String id = ProfileDescription.PROFILE_PREFIX + "profile1";
        RegistryTestHelper.addComment(baseRegistry, "COMMENT1", id,
                "JUnit@test.com");
        List<Comment> response = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + id + "/comments/"))
                .accept(MediaType.APPLICATION_XML)
                .get(COMMENT_LIST_GENERICTYPE);
        assertEquals(3, response.size());

        response = this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/profiles/" + id + "/comments"))
                .accept(MediaType.APPLICATION_JSON)
                .get(COMMENT_LIST_GENERICTYPE);
        assertEquals(3, response.size());
        assertEquals("comment2", response.get(0).getComment());
        assertEquals("comment1", response.get(1).getComment());
        assertEquals("COMMENT1", response.get(2).getComment());

        assertEquals("Database test user", response.get(0).getUserName());
    }

    @Test // ok
    public void testGetCommentsInPubliComponent() throws Exception {

        System.out.println("testGetCoomentsInPublicComponent");

        fillUpPublicItems();

        String id = ComponentDescription.COMPONENT_PREFIX + "component1";
        RegistryTestHelper.addComment(baseRegistry, "COMMENT2", id,
                "JUnit@test.com");
        List<Comment> response = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + id + "/comments/"))
                .accept(MediaType.APPLICATION_XML)
                .get(COMMENT_LIST_GENERICTYPE);
        assertEquals(3, response.size());
        response = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + id + "/comments"))
                .accept(MediaType.APPLICATION_JSON)
                .get(COMMENT_LIST_GENERICTYPE);
        assertEquals(3, response.size());
        assertEquals("comment3", response.get(0).getComment());
        assertEquals("comment4", response.get(1).getComment());
        assertEquals("COMMENT2", response.get(2).getComment());

        assertEquals("Database test user", response.get(0).getUserName());
    }

    @Test // ok
    public void testGetSpecifiedCommentInPublicComponent() throws Exception {

        System.out.println("testSpecifiedCommentInPublicComponent");

        fillUpPublicItems();

        String id = ComponentDescription.COMPONENT_PREFIX + "component1";
        Comment comment = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + id + "/comments/" + component1Comment3.getId()))
                .accept(MediaType.APPLICATION_JSON).get(Comment.class);
        assertNotNull(comment);
        assertEquals("comment3", comment.getComment());
        assertEquals(component1Comment3.getId(), comment.getId());
        comment = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + id + "/comments/" + component1Comment4.getId()))
                .accept(MediaType.APPLICATION_JSON).get(Comment.class);
        assertNotNull(comment);
        assertEquals("comment4", comment.getComment());
        assertEquals(component1Comment4.getId(), comment.getId());
        assertEquals(id, comment.getComponentRef());
    }

    @Test //ok
    public void testGetSpecifiedCommentInPublicProfile() throws Exception {

        System.out.println("testGetSpecifiedCoomentInPublicProfile");

        fillUpPublicItems();

        String id = ProfileDescription.PROFILE_PREFIX + "profile1";
        Comment comment = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + id + "/comments/" + profile1Comment2.getId()))
                .accept(MediaType.APPLICATION_JSON).get(Comment.class);
        assertNotNull(comment);
        assertEquals("comment2", comment.getComment());
        assertEquals(profile1Comment2.getId(), comment.getId());

        comment = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + id + "/comments/" + profile1Comment1.getId()))
                .accept(MediaType.APPLICATION_JSON).get(Comment.class);
        assertNotNull(comment);
        assertEquals("comment1", comment.getComment());
        assertEquals(profile1Comment1.getId(), comment.getId());
        assertEquals(id, comment.getComponentRef());
    }

    @Test  //ok
    public void testDeleteCommentFromPublicComponent() throws Exception {

        System.out.println("testDeleteCommentFromPublicComponent");

        fillUpPublicItems();

        String id = ComponentDescription.COMPONENT_PREFIX + "component1";
        String id2 = ComponentDescription.COMPONENT_PREFIX + "component2";
        List<Comment> comments = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components/" + id + "/comments")).get(
                        COMMENT_LIST_GENERICTYPE);
        assertEquals(2, comments.size());
        Comment aComment = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components/" + id + "/comments/" + component1Comment3.getId()))
                .get(Comment.class);
        assertNotNull(aComment);

        // Try to delete from other component
        ClientResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + id2 + "/comments/" + component1Comment4.getId()).delete(
                ClientResponse.class);
        assertEquals(404, response.getStatus());
        // Delete from correct component
        response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + id + "/comments/" + component1Comment3.getId()).delete(
                ClientResponse.class);
        assertEquals(200, response.getStatus());

        comments = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components/" + id + "/comments/")).get(
                        COMMENT_LIST_GENERICTYPE);
        assertEquals(1, comments.size());

        response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + id + "/comments/" + component1Comment4.getId()).delete(
                ClientResponse.class);
        assertEquals(200, response.getStatus());

        comments = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components/" + id + "/comments")).get(
                        COMMENT_LIST_GENERICTYPE);
        assertEquals(0, comments.size());
    }

    @Test
    public void testManipulateCommentFromPublicComponent() throws Exception {

        System.out.println("testManipulateCommentFromPublicComponent");

        fillUpPublicItems();

        List<Comment> comments = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX
                + "component1/comments")).get(COMMENT_LIST_GENERICTYPE);
        assertEquals(2, comments.size());
        Comment aComment = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX
                + "component1/comments/" + component1Comment3.getId())).get(Comment.class);
        assertNotNull(aComment);

        Form manipulateForm = new Form();
        manipulateForm.add("method", "delete");

        // Try to delete from other component
        ClientResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX
                + "component1/comments/" + component1Comment3.getId()).post(ClientResponse.class,
                manipulateForm);
        assertEquals(200, response.getStatus());

        comments = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX
                + "component1/comments/")).get(COMMENT_LIST_GENERICTYPE);
        assertEquals(1, comments.size());
    }

    @Test
    public void testDeleteCommentFromPublicProfile() throws Exception {

        System.out.println("testDeleteCommentFromPublicProfile");

        fillUpPublicItems();

        List<Comment> comments = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile1/comments")).get(COMMENT_LIST_GENERICTYPE);
        assertEquals(2, comments.size());
        Comment aComment = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile1/comments/" + profile1Comment1.getId())).get(Comment.class);
        assertNotNull(aComment);

        // Try to delete from other profile
        ClientResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile2/comments/9999").delete(ClientResponse.class);
        assertEquals(404, response.getStatus());
        // Delete from correct profile
        response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile1/comments/" + profile1Comment1.getId()).delete(ClientResponse.class);
        assertEquals(200, response.getStatus());

        comments = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile1/comments/")).get(COMMENT_LIST_GENERICTYPE);
        assertEquals(1, comments.size());

        response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile1/comments/" + profile1Comment2.getId()).delete(ClientResponse.class);
        assertEquals(200, response.getStatus());

        comments = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile1/comments")).get(COMMENT_LIST_GENERICTYPE);
        assertEquals(0, comments.size());
    }

    @Test
    public void testManipulateCommentFromPublicProfile() throws Exception {

        System.out.println("testManipulatreCommentFromPublicProfile");

        fillUpPublicItems();

        List<Comment> comments = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile1/comments")).get(COMMENT_LIST_GENERICTYPE);
        assertEquals(2, comments.size());
        Comment aComment = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile1/comments/" + profile1Comment2.getId())).get(Comment.class);
        assertNotNull(aComment);

        Form manipulateForm = new Form();
        manipulateForm.add("method", "delete");
        // Delete from correct profile
        ClientResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile1/comments/" + profile1Comment2.getId()).post(ClientResponse.class,
                manipulateForm);
        assertEquals(200, response.getStatus());

        comments = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile1/comments/")).get(COMMENT_LIST_GENERICTYPE);
        assertEquals(1, comments.size());
    }

    @Test
    public void testDeletePrivateComponent() throws Exception {
        fillUpPrivateItems();

        List<ComponentDescription> components = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components").queryParam(REGISTRY_SPACE_PARAM, REGISTRY_SPACE_PRIVATE)).get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(1, components.size());
        ComponentSpec profile = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX
                + "component3")).get(ComponentSpec.class);
        assertNotNull(profile);
        ClientResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX
                + "component3").delete(ClientResponse.class);
        assertEquals(200, response.getStatus());

        components = this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components").queryParam(REGISTRY_SPACE_PARAM, REGISTRY_SPACE_PRIVATE)).get(
                COMPONENT_LIST_GENERICTYPE);
        assertEquals(0, components.size());

        response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX
                + "component3").delete(ClientResponse.class);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testDeletePrivateProfile() throws Exception {
        fillUpPrivateItems();

        List<ComponentDescription> components = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles").queryParam(REGISTRY_SPACE_PARAM, REGISTRY_SPACE_PRIVATE)).get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(1, components.size());
        ComponentSpec profile = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile3").queryParam(REGISTRY_SPACE_PARAM, REGISTRY_SPACE_PRIVATE)).get(ComponentSpec.class);
        assertNotNull(profile);
        ClientResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile3").delete(ClientResponse.class);
        assertEquals(200, response.getStatus());

        components = this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/profiles").queryParam(REGISTRY_SPACE_PARAM, REGISTRY_SPACE_PRIVATE)).get(
                COMPONENT_LIST_GENERICTYPE);
        assertEquals(0, components.size());

        response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile3").delete(ClientResponse.class);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testDeleteOtherUsersComponent() throws Exception {
        fillUpPrivateItems();

        List<ComponentDescription> components = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components").queryParam(REGISTRY_SPACE_PARAM, REGISTRY_SPACE_PRIVATE)).get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(1, components.size());
        ClientResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX
                + "component4").delete(ClientResponse.class);
        assertEquals("Deleting another user's profile should not be allowed", 403, response.getStatus());

        // should not affect listing of own private profiles
        components = this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components").queryParam(REGISTRY_SPACE_PARAM, REGISTRY_SPACE_PRIVATE)).get(
                COMPONENT_LIST_GENERICTYPE);
        assertEquals(1, components.size());
    }

    @Test
    public void testDeleteOtherUsersProfile() throws Exception {
        fillUpPrivateItems();

        List<ComponentDescription> components = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles").queryParam(REGISTRY_SPACE_PARAM, REGISTRY_SPACE_PRIVATE)).get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(1, components.size());

        ClientResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile4").delete(ClientResponse.class);
        assertEquals("Deleting another user's profile should not be allowed", 403, response.getStatus());

        // should not affect listing of own private profiles
        components = this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/profiles").queryParam(REGISTRY_SPACE_PARAM, REGISTRY_SPACE_PRIVATE)).get(
                COMPONENT_LIST_GENERICTYPE);
        assertEquals(1, components.size());
    }

    @Test
    public void testDeletePublicComponent() throws Exception {

        System.out.println("testDeletePublicComponent");

        fillUpPublicItems();

        List<ComponentDescription> components = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components")).get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(2, components.size());
        ComponentSpec profile = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX
                + "component1")).get(ComponentSpec.class);
        assertNotNull(profile);
        ClientResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX
                + "component1").delete(ClientResponse.class);
        assertEquals(200, response.getStatus());

        components = this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components")).get(
                COMPONENT_LIST_GENERICTYPE);
        assertEquals(1, components.size());

        response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX
                + "component2").delete(ClientResponse.class);
        assertEquals(200, response.getStatus());

        components = this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components")).get(
                COMPONENT_LIST_GENERICTYPE);
        assertEquals(0, components.size());

        response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX
                + "component1").delete(ClientResponse.class);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testDeletePublicComponentStillUsed() throws Exception {

        System.out.println("testDeletePublicComponentStillUsed");

        fillUpPublicItems();

        String content = "";
        content += "<ComponentSpec CMDVersion=\"1.2\" isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"cmd-component.xsd\">\n";
        content += "    <Header>\n";
        content += "        <ID>clarin.eu:cr1:p_12345678a</ID>\n";
        content += "        <Name>XXX</Name>\n";
        content += "        <Description>p_12345678a</Description>";
        content += "        <Status>production</Status>\n";
        content += "    </Header>\n";
        content += "    <Component name=\"XXX\">\n";
        content += "        <Element name=\"Availability\" ValueScheme=\"string\" />\n";
        content += "    </Component>\n";
        content += "</ComponentSpec>\n";
        ComponentDescription compDesc1 = RegistryTestHelper.addComponent(
                baseRegistry, "XXX1", content, true, PRODUCTION);

        content = "";
        content += "<ComponentSpec CMDVersion=\"1.2\" isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"cmd-component.xsd\">\n";
        content += "    <Header>\n";
        content += "        <ID>clarin.eu:cr1:p_12345678b</ID>\n";
        content += "        <Name>YYY</Name>\n";
        content += "        <Description>p_12345678b</Description>";
        content += "        <Status>production</Status>\n";
        content += "    </Header>\n";
        content += "    <Component name=\"YYY\">\n";
        content += "        <Component ComponentRef=\"" + compDesc1.getId()
                + "\" CardinalityMin=\"0\" CardinalityMax=\"99\">\n";
        content += "        </Component>\n";
        content += "    </Component>\n";
        content += "</ComponentSpec>\n";
        ComponentDescription compDesc2 = RegistryTestHelper.addComponent(
                baseRegistry, "YYY1", content, true, PRODUCTION);

        content = "";
        content += "<ComponentSpec CMDVersion=\"1.2\" isProfile=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"cmd-component.xsd\">\n";
        content += "    <Header>\n";
        content += "        <ID>clarin.eu:cr1:p_12345678c</ID>\n";
        content += "        <Name>ZZZ</Name>\n";
        content += "        <Description>p_12345678c</Description>";
        content += "        <Status>production</Status>\n";
        content += "    </Header>\n";
        content += "    <Component name=\"ZZZ\">\n";
        content += "        <Component ComponentRef=\"" + compDesc1.getId()
                + "\" CardinalityMin=\"0\" CardinalityMax=\"99\">\n";
        content += "        </Component>\n";
        content += "    </Component>\n";
        content += "</ComponentSpec>\n";
        ProfileDescription profile = RegistryTestHelper.addProfile(
                baseRegistry, "TestProfile3", content, true, PRODUCTION);

        List<ComponentDescription> components = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components")).get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(4, components.size());

        ClientResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + compDesc1.getId()).delete(
                ClientResponse.class);
        assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertEquals(
                "Component is still in use by other components or profiles. Request component usage for details.",
                response.getEntity(String.class));

        components = this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components")).get(
                COMPONENT_LIST_GENERICTYPE);
        assertEquals(4, components.size());

        response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + profile.getId()).delete(
                ClientResponse.class);
        assertEquals(200, response.getStatus());
        response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + compDesc2.getId()).delete(
                ClientResponse.class);
        assertEquals(200, response.getStatus());
        response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + compDesc1.getId()).delete(
                ClientResponse.class);
        assertEquals(200, response.getStatus());
        components = this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components")).get(
                COMPONENT_LIST_GENERICTYPE);
        assertEquals(2, components.size());
    }

    @Test
    public void testManipulatePublicComponent() throws Exception {

        System.out.println("testManipulatePublicComponent");

        fillUpPublicItems();

        List<ComponentDescription> components = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components")).get(COMPONENT_LIST_GENERICTYPE);
        assertEquals(2, components.size());
        ComponentSpec profile = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX
                + "component1")).get(ComponentSpec.class);
        assertNotNull(profile);

        Form manipulateForm = new Form();
        manipulateForm.add("method", "delete");

        ClientResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX
                + "component1").post(ClientResponse.class,
                        manipulateForm);
        assertEquals(200, response.getStatus());

        components = this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components")).get(
                COMPONENT_LIST_GENERICTYPE);
        assertEquals(1, components.size());

        response = getAuthenticatedResource(
                REGISTRY_BASE + "/components/" + ComponentDescription.COMPONENT_PREFIX
                + "component2").post(ClientResponse.class,
                        manipulateForm);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetRegisteredProfile() throws Exception {

        System.out.println("testGetRegisteredProfile");

        fillUpPublicItems();

        String id = ProfileDescription.PROFILE_PREFIX + "profile1";
        String id2 = ProfileDescription.PROFILE_PREFIX + "profile2";
        ComponentSpec profile = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + id))
                .accept(MediaType.APPLICATION_XML).get(ComponentSpec.class);
        assertNotNull(profile);
        assertEquals("profile1", profile.getComponent().getName());
        profile = this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/profiles/" + id2))
                .accept(MediaType.APPLICATION_XML).get(ComponentSpec.class);
        assertNotNull(profile);
        assertEquals("profile2", profile.getComponent().getName());

        assertEquals(id2, profile.getHeader().getID());
        assertEquals("profile2", profile.getHeader().getName());
        assertEquals("profile2 description", profile.getHeader().getDescription());
        assertEquals(ComponentStatus.PRODUCTION.toString(), profile.getHeader().getStatus());
        assertEquals("1.2", profile.getCMDVersion());
        assertEquals("1.2", profile.getCMDOriginalVersion());

        try {
            this.getAuthenticatedResource(getResource()
                    .path(REGISTRY_BASE + "/profiles/"
                            + ProfileDescription.PROFILE_PREFIX + "profileXXXX"))
                    .accept(MediaType.APPLICATION_XML)
                    .get(ComponentSpec.class);
            fail("Exception should have been thrown resource does not exist, HttpStatusCode 404");
        } catch (UniformInterfaceException e) {
            assertEquals(404, e.getResponse().getStatus());
        }
    }

    @Test
    public void testGetRegisteredProfileRawData() {

        System.out.println("testGetRegisteredProfileRawData");

        try {
            fillUpPublicItems();
        } catch (Exception e) {
            System.out.println("test fails due to exception: " + e.getMessage());
            return;
        }

        //make unauthenticated request
        String profile = getResource()
                .path(REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                        + "profile1/xsd").accept(MediaType.TEXT_XML)
                .get(String.class).trim();
        assertNotNull("Unauthenticated request for profile raw data", profile);
        assertTrue(profile
                .startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?><xs:schema"));
        assertTrue(profile.endsWith("</xs:schema>"));

        //make authenticated request
        profile = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                        + "profile1/xsd")).accept(MediaType.TEXT_XML)
                .get(String.class).trim();
        assertNotNull("Authenticated request for profile raw data", profile);
        assertTrue(profile
                .startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?><xs:schema"));
        assertTrue(profile.endsWith("</xs:schema>"));

        profile = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                        + "profile1/xml")).accept(MediaType.TEXT_XML)
                .get(String.class).trim();
        assertTrue(profile
                .startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<ComponentSpec"));
        assertTrue(profile.endsWith("</ComponentSpec>"));
        assertTrue(profile.contains("xsi:noNamespaceSchemaLocation"));

        ClientResponse respie = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/"
                        + ProfileDescription.PROFILE_PREFIX
                        + "profile1/xsl")).accept(MediaType.TEXT_XML).head();
        assertEquals(404, respie.getStatus());
    }

    // 
    @Test
    public void testPrivateProfileXsd() {

        System.out.println("testPrivateProfileXsd");

        try {
            fillUpPublicItems();
        } catch (Exception e) {
            System.out.println("test fails due to exception: " + e.getMessage());
            return;
        }

        FormDataMultiPart form = createFormData(RegistryTestHelper
                .getTestProfileContent());
        RegisterResponse response = getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/profiles").queryParam(
                REGISTRY_SPACE_PARAM, REGISTRY_SPACE_PRIVATE)).type(
                        MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class,
                        form);
        assertTrue(response.isProfile());
        assertEquals(1, getUserProfiles().size());
        BaseDescription desc = response.getDescription();
        String profile = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + desc.getId() + "/xsd"))
                .accept(MediaType.TEXT_XML).get(String.class).trim();
        assertTrue(profile
                .startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?><xs:schema"));
        assertTrue(profile.endsWith("</xs:schema>"));
    }

    @Test
    public void testDeleteRegisteredProfile() throws Exception {

        System.out.println("testDeleteRegistredProfile");

        fillUpPublicItems();

        List<ProfileDescription> profiles = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles")).get(PROFILE_LIST_GENERICTYPE);
        assertEquals(2, profiles.size());
        ComponentSpec profile = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile1")).get(ComponentSpec.class);
        assertNotNull(profile);

        ClientResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile1").delete(ClientResponse.class);
        assertEquals(200, response.getStatus());

        profiles = this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/profiles")).get(
                PROFILE_LIST_GENERICTYPE);
        assertEquals(1, profiles.size());

        response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile2").delete(ClientResponse.class);
        assertEquals(200, response.getStatus());

        profiles = this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/profiles")).get(
                PROFILE_LIST_GENERICTYPE);
        assertEquals(0, profiles.size());

        response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile1").delete(ClientResponse.class);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testManipulateRegisteredProfile() throws Exception {

        System.out.println("testManipulateRegisteredProfile");

        fillUpPublicItems();

        List<ProfileDescription> profiles = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles")).get(PROFILE_LIST_GENERICTYPE);
        assertEquals(2, profiles.size());
        ComponentSpec profile = this.getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile1")).get(ComponentSpec.class);
        assertNotNull(profile);

        Form manipulateForm = new Form();
        manipulateForm.add("method", "delete");

        ClientResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile1")
                .post(ClientResponse.class, manipulateForm);
        assertEquals(200, response.getStatus());

        profiles = this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/profiles")).get(
                PROFILE_LIST_GENERICTYPE);
        assertEquals(1, profiles.size());

        response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX
                + "profile2")
                .post(ClientResponse.class, manipulateForm);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetRegisteredComponentRawData() throws Exception {

        System.out.println("testGetRegisteredComponentRawData");

        fillUpPublicItems();

        String id = ComponentDescription.COMPONENT_PREFIX + "component1";
        String component = this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components/" + id + "/xml"))
                .accept(MediaType.TEXT_XML).get(String.class).trim();
        assertTrue(component
                .startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<ComponentSpec"));
        assertTrue(component.endsWith("</ComponentSpec>"));
        assertTrue(component.contains("xsi:noNamespaceSchemaLocation"));

        ClientResponse respie = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/"
                        + ComponentDescription.COMPONENT_PREFIX
                        + "component1/jpg")).accept(MediaType.TEXT_XML)
                .head();
        assertEquals(404, respie.getStatus());
    }

    @Test
    public void testRegisterProfile() throws Exception {

        System.out.println("testRegisterProfile");

        fillUpPublicItems();

        FormDataMultiPart form = new FormDataMultiPart();
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD,
                RegistryTestHelper.getTestProfileContent(),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field(ComponentRegistryRestService.NAME_FORM_FIELD,
                "ProfileTest1");
        form.field(ComponentRegistryRestService.DOMAIN_FORM_FIELD,
                "TestDomain");
        form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD,
                "My Test Profile");
        form.field(ComponentRegistryRestService.GROUP_FORM_FIELD,
                "My Test Group");
        RegisterResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles").type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);
        assertTrue(response.isProfile());
        assertTrue(response.isPrivate());
        ProfileDescription profileDesc = (ProfileDescription) response
                .getDescription();
        assertNotNull(profileDesc);
        assertEquals("Spec header should override", "Actor", profileDesc.getName());
        assertEquals("Spec header should override", "Actor description", profileDesc.getDescription());
        assertEquals("TestDomain", profileDesc.getDomainName());
        assertEquals("My Test Group", profileDesc.getGroupName());
        assertEquals(expectedUserId("JUnit@test.com"), profileDesc.getUserId());
        assertEquals("Database test user", profileDesc.getCreatorName());
        assertTrue(profileDesc.getId().startsWith(
                REGISTRY_ID + "p_"));
        assertNotNull(profileDesc.getRegistrationDate());
        assertEquals(
                "http://localhost:9998" + REGISTRY_BASE + "/profiles/"
                + profileDesc.getId(), profileDesc.getHref());
    }

    @Test
    public void testPublishProfileAsProduction() throws Exception {
        System.out.println("testPublishProfileAsProduction");

        fillUpPrivateItems();
        fillUpPublicItems();

        assertEquals("user registered profiles", 1, getUserProfiles().size());
        assertEquals("public registered profiles", 2, getPublicProfiles()
                .size());
        FormDataMultiPart form = createFormData(
                RegistryTestHelper.getTestProfileContent(), "description");
        RegisterResponse response = getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/profiles")).type(
                MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class,
                        form);
        assertTrue(response.isProfile());
        BaseDescription desc = response.getDescription();
        assertEquals("Actor description", desc.getDescription());
        assertEquals(2, getUserProfiles().size());
        assertEquals(2, getPublicProfiles().size());
        form = createFormData(
                RegistryTestHelper.getTestProfileContent("publishedName3", PRODUCTION.toString()),
                "publishedName3 description");
        //post
        getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles/" + desc.getId() + "/publish"))
                .type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);

        assertEquals(1, getUserProfiles().size());

        List<ProfileDescription> profiles = getPublicProfiles();
        assertEquals(3, profiles.size());

        ProfileDescription profileDescription = profiles.get(2);
        assertEquals(desc.getId(), profileDescription.getId());
        assertEquals(PRODUCTION, profileDescription.getStatus());
        ComponentSpec spec = getPublicSpec(profileDescription);
        assertEquals(PRODUCTION.toString(), spec.getHeader().getStatus());
    }

    @Test
    public void testPublishProfileAsDevelopment() throws Exception {
        System.out.println("testPublishProfileAsDevelopment");

        fillUpPrivateItems();
        fillUpPublicItems();

        //skipping some tests that already take place in the same context in testPublishProfileAsProduction
        FormDataMultiPart form = createFormData(
                RegistryTestHelper.getTestProfileContent(), "description");
        RegisterResponse response = getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/profiles")).type(
                MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);
        BaseDescription desc = response.getDescription();
        form = createFormData(
                RegistryTestHelper.getTestProfileContent("publishedButDevelopment", DEVELOPMENT.toString()),
                "publishedButDevelopment description");
        //post
        getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles/" + desc.getId() + "/publish"))
                .type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);

        //skipping some tests that already take place in the same context in testPublishProfileAsProduction
        ProfileDescription profileDescription = getPublicProfiles().get(2);
        assertEquals(desc.getId(), profileDescription.getId());
        assertEquals(DEVELOPMENT, profileDescription.getStatus());

        ComponentSpec spec = getPublicSpec(profileDescription);
        assertEquals(DEVELOPMENT.toString(), spec.getHeader().getStatus());
    }

    @Test
    public void testPublishComponentAsProduction() throws Exception {
        fillUpPrivateItems();
        fillUpPublicItems();

        assertEquals("user registered components", 1, getUserComponents().size());
        assertEquals("public registered components", 2, getPublicComponents()
                .size());
        FormDataMultiPart form = createFormData(
                RegistryTestHelper.getComponentTestContent(), "description");
        RegisterResponse response = getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components")).type(
                MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class,
                        form);
        assertFalse(response.isProfile());
        BaseDescription desc = response.getDescription();
        assertEquals("Access description", desc.getDescription());
        assertEquals(2, getUserComponents().size());
        assertEquals(2, getPublicComponents().size());
        form = createFormData(
                RegistryTestHelper.getComponentTestContent("publishedName3", PRODUCTION.toString()),
                "publishedName3 description");
        //post
        getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components/" + desc.getId() + "/publish"))
                .type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);

        assertEquals(1, getUserComponents().size());

        List<ComponentDescription> components = getPublicComponents();
        assertEquals(3, components.size());

        ComponentDescription componentDescription = components.get(2);
        assertEquals(desc.getId(), componentDescription.getId());
        assertEquals(PRODUCTION, componentDescription.getStatus());
        ComponentSpec spec = getPublicSpec(componentDescription);
        assertEquals(PRODUCTION.toString(), spec.getHeader().getStatus());
    }

    @Test
    public void testPublishComponentAsDevelopment() throws Exception {
        fillUpPrivateItems();
        fillUpPublicItems();

        //skipping some tests that already take place in the same context in testPublishComponentAsProduction
        FormDataMultiPart form = createFormData(
                RegistryTestHelper.getComponentTestContent(), "description");
        RegisterResponse response = getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components")).type(
                MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class, form);
        BaseDescription desc = response.getDescription();
        form = createFormData(
                RegistryTestHelper.getComponentTestContent("publishedButDevelopment", DEVELOPMENT.toString()),
                "publishedButDevelopment description");
        //post
        getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components/" + desc.getId() + "/publish"))
                .type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);

        //skipping some tests that already take place in the same context in testPublishComponentAsProduction
        ComponentDescription componentDescription = getPublicComponents().get(2);
        assertEquals(desc.getId(), componentDescription.getId());
        assertEquals(DEVELOPMENT, componentDescription.getStatus());

        ComponentSpec spec = getPublicSpec(componentDescription);
        assertEquals(DEVELOPMENT.toString(), spec.getHeader().getStatus());
    }

    private ComponentSpec getPublicSpec(BaseDescription desc) {
        if (desc.isProfile()) {
            return this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/profiles/" + desc.getId()))
                    .get(ComponentSpec.class);
        } else {
            return this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components/" + desc.getId()))
                    .get(ComponentSpec.class);
        }
    }

    @Test
    public void testPublishRegisteredComponent() throws Exception {

        System.out.println("testPublishRegisteredComponent");

        fillUpPrivateItems();
        fillUpPublicItems();

        assertEquals(1, getUserComponents().size());
        assertEquals(2, getPublicComponents().size());

        FormDataMultiPart form = createFormData(
                RegistryTestHelper.getComponentTestContent(), "description");
        RegisterResponse response = getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components")).type(
                MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class,
                        form);
        assertFalse(response.isProfile());
        BaseDescription desc = response.getDescription();
        assertEquals("Access", desc.getName());
        assertEquals("Access description", desc.getDescription());
        assertEquals(2, getUserComponents().size());
        assertEquals(2, getPublicComponents().size());
        form = createFormData(
                RegistryTestHelper.getComponentTestContentAsStream("publishedName"),
                "Published");
        //post
        getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components/" + desc.getId() + "/publish"))
                .type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);

        assertEquals(1, getUserComponents().size());
        List<ComponentDescription> components = getPublicComponents();
        assertEquals(3, components.size());
        ComponentDescription componentDescription = components.get(2);
        assertNotNull(componentDescription.getId());
        assertEquals(desc.getId(), componentDescription.getId());
        assertEquals(
                "http://localhost:9998" + REGISTRY_BASE + "/components/" + desc.getId(),
                componentDescription.getHref());
        assertEquals("publishedName", componentDescription.getName());
        assertEquals("publishedName description", componentDescription.getDescription());
        ComponentSpec spec = getPublicSpec(componentDescription);
        assertEquals("publishedName", spec.getComponent().getName());
    }

    // duplicates ("extendedly") testRegisterProfile, since you can pot only in user space, and then publish or move to group or so
    @Test
    public void testRegisterUserspaceProfile() throws Exception {

        System.out.println("testRegisterUserspacProfile");

        fillUpPrivateItems();
        fillUpPublicItems();

        List<ProfileDescription> profiles = getUserProfiles();
        assertEquals("user registered profiles", 1, profiles.size());
        assertEquals("public registered profiles", 2, getPublicProfiles()
                .size());
        FormDataMultiPart form = createFormData(RegistryTestHelper
                .getTestProfileContent());
        RegisterResponse response = getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/profiles")).type(
                MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class,
                        form);
        assertTrue(response.isProfile());
        assertTrue(response.isPrivate());
        ProfileDescription profileDesc = (ProfileDescription) response
                .getDescription();
        assertNotNull(profileDesc);
        assertEquals("Actor", profileDesc.getName());
        assertEquals("Actor description", profileDesc.getDescription());
        assertEquals(expectedUserId("JUnit@test.com"), profileDesc.getUserId());
        assertEquals("Database test user", profileDesc.getCreatorName());
        assertTrue(profileDesc.getId().startsWith(
                REGISTRY_ID + "p_"));
        assertNotNull(profileDesc.getRegistrationDate());
        assertEquals(
                "http://localhost:9998" + REGISTRY_BASE + "/profiles/"
                + profileDesc.getId(),
                profileDesc.getHref());

        profiles = getUserProfiles();
        assertEquals(2, profiles.size());
        assertEquals(2, getPublicProfiles().size());

        // Try to post unauthenticated
        ClientResponse cResponse = getResource().path(REGISTRY_BASE + "/profiles")
                .type(MediaType.MULTIPART_FORM_DATA)
                .post(ClientResponse.class, form);
        assertEquals(401, cResponse.getStatus());

        ComponentSpec spec = getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/profiles/" + profileDesc.getId())
                .queryParam(REGISTRY_SPACE_PARAM, REGISTRY_SPACE_PRIVATE)).accept(
                MediaType.APPLICATION_XML).get(ComponentSpec.class);
        assertNotNull(spec);

        cResponse = this.getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + profileDesc.getId() + "/xsd"))
                .accept(MediaType.TEXT_XML).get(ClientResponse.class);
        assertEquals(200, cResponse.getStatus());
        String profile = cResponse.getEntity(String.class);
        assertTrue(profile.length() > 0);

        profile = getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles/" + profileDesc.getId() + "/xml"))
                .accept(MediaType.TEXT_XML).get(String.class);
        assertTrue(profile.length() > 0);

        cResponse = getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/profiles/" + profileDesc.getId())
                .queryParam(REGISTRY_SPACE_PARAM, REGISTRY_SPACE_PRIVATE)).delete(
                ClientResponse.class);
        assertEquals(200, cResponse.getStatus());

        profiles = getUserProfiles();
        assertEquals(1, profiles.size());
    }

    private List<ProfileDescription> getPublicProfiles() {
        return getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles")
                .queryParam(STATUS_FILTER_PARAM, "*"))
                .accept(MediaType.APPLICATION_XML).get(PROFILE_LIST_GENERICTYPE);
    }

    private List<ProfileDescription> getUserProfiles() {
        return getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles")
                .queryParam(STATUS_FILTER_PARAM, "*")
                .queryParam(REGISTRY_SPACE_PARAM, REGISTRY_SPACE_PRIVATE))
                .accept(MediaType.APPLICATION_XML).get(PROFILE_LIST_GENERICTYPE);
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
    public void testRegisterWithUserComponents() throws Exception {

        System.out.println("testRegisterWithUserComponents");

        fillUpPrivateItems();

        // kid component, not public
        String content = "";
        content += "<ComponentSpec CMDVersion=\"1.2\" isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"cmd-component.xsd\">\n";
        content += "    <Header>\n";
        content += "        <ID>clarin.eu:cr1:p_12345678x</ID>\n";
        content += "        <Name>XXX</Name>\n";
        content += "        <Description>DDD</Description>\n";
        content += "        <Status>development</Status>\n";
        content += "    </Header>\n";
        content += "    <Component name=\"XXX\">\n";
        content += "        <Element name=\"Availability\" ValueScheme=\"string\" />\n";
        content += "    </Component>\n";
        content += "</ComponentSpec>\n";
        ComponentDescription compDesc1 = RegistryTestHelper.addComponent(
                baseRegistry, "XXX1", content, false, DEVELOPMENT);

        // a containing component, referring to the kid (which is not public, so the containing component cannot be registered
        content = "";
        content += "<ComponentSpec CMDVersion=\"1.2\" isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"cmd-component.xsd\">\n";
        content += "    <Header>\n";
        content += "        <ID>clarin.eu:cr1:p_12345678y</ID>\n";
        content += "        <Name>YYY</Name>\n";
        content += "        <Description>DDD</Description>\n";
        content += "        <Status>development</Status>\n";
        content += "    </Header>\n";
        content += "    <Component name=\"YYY\">\n";
        content += "        <Component ComponentRef=\"" + compDesc1.getId()
                + "\" CardinalityMin=\"0\" CardinalityMax=\"99\">\n";
        content += "        </Component>\n";
        content += "    </Component>\n";
        content += "</ComponentSpec>\n";
        FormDataMultiPart form = createFormData(content);
        RegisterResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/components").type(MediaType.MULTIPART_FORM_DATA)
                .post(RegisterResponse.class, form);
        // we first alway register in a private space, so reference to a private component should work
        assertTrue(response.isRegistered());

        /// profiles ///
        content = "";
        content += "<ComponentSpec CMDVersion=\"1.2\" isProfile=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        content += "    xsi:noNamespaceSchemaLocation=\"cmd-component.xsd\">\n";
        content += "    <Header>\n";
        content += "        <ID>clarin.eu:cr1:p_12345678z</ID>\n";
        content += "        <Name>ZZZ</Name>\n";
        content += "        <Description>DDD</Description>\n";
        content += "        <Status>development</Status>\n";
        content += "    </Header>\n";
        content += "    <Component name=\"ZZZ\">\n";
        content += "        <Component ComponentRef=\"" + compDesc1.getId()
                + "\" CardinalityMin=\"0\" CardinalityMax=\"99\">\n";
        content += "        </Component>\n";
        content += "    </Component>\n";
        content += "</ComponentSpec>\n";

        form = createFormData(content);
        response = getAuthenticatedResource(REGISTRY_BASE + "/profiles").type(
                MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class,
                        form);
        assertTrue(response.isRegistered());

    }

    @Test
    public void testRegisterUserspaceComponent() throws Exception {

        System.out.println("testRegisterUserspaceComponent");

        fillUpPrivateItems();

        List<ComponentDescription> components = getUserComponents();
        assertEquals("user registered components", 1, components.size());
        assertEquals("public registered components", 0, getPublicComponents()
                .size());
        FormDataMultiPart form = createFormData(RegistryTestHelper
                .getComponentTestContent());

        RegisterResponse response = getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components")).type(
                MediaType.MULTIPART_FORM_DATA).post(RegisterResponse.class,
                        form);
        assertTrue(response.isRegistered());
        assertFalse(response.isProfile());
        assertTrue(response.isPrivate());
        ComponentDescription desc = (ComponentDescription) response
                .getDescription();
        assertNotNull(desc);
        assertEquals("Access", desc.getName());
        assertEquals("Access description", desc.getDescription());
        assertEquals(expectedUserId("JUnit@test.com"), desc.getUserId());
        assertEquals("Database test user", desc.getCreatorName());
        assertEquals("TestGroup", desc.getGroupName());
        assertTrue(desc.getId()
                .startsWith(REGISTRY_ID + "c_"));
        assertNotNull(desc.getRegistrationDate());
        {
            final String url = getResource().path(REGISTRY_BASE + "/components/" + desc.getId()).getUriBuilder().build().toString();
            assertEquals(url, desc.getHref());
        }

        components = getUserComponents();
        assertEquals(2, components.size());
        assertEquals(0, getPublicComponents().size());

        // Try to post unauthenticated
        ClientResponse cResponse = getResource().path(REGISTRY_BASE + "/components")
                .type(MediaType.MULTIPART_FORM_DATA)
                .post(ClientResponse.class, form);
        assertEquals(401, cResponse.getStatus());

        // Try to get 
        cResponse = this.getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components/" + desc.getId()))
                .accept(MediaType.APPLICATION_XML).get(ClientResponse.class);

        assertEquals(200, cResponse.getStatus());
        ComponentSpec spec = getUserComponent(desc);
        assertNotNull(spec);

        String result = getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components/" + desc.getId() + "/xml"))
                .accept(MediaType.TEXT_XML).get(String.class);
        assertTrue(result.length() > 0);

        cResponse = getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components/" + desc.getId())).delete(
                ClientResponse.class);
        assertEquals(200, cResponse.getStatus());

        components = getUserComponents();
        assertEquals(1, components.size());
    }
//

    @Test
    public void testCreatePrivateComponentWithRecursion() throws Exception {

        System.out.println("testCreatePrivateComponentWithRecursion");

        fillUpPrivateItems();
        // Create new componet
        FormDataMultiPart form = createFormData(RegistryTestHelper
                .getComponentTestContent());
        ClientResponse cResponse = getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components")).type(
                MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                cResponse.getStatus());
        RegisterResponse response = cResponse.getEntity(RegisterResponse.class);
        assertTrue(response.isRegistered());
        ComponentDescription desc = (ComponentDescription) response
                .getDescription();

        // Re-define with self-recursion
        String compContent = "";
        compContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        compContent += "\n";
        compContent += "<ComponentSpec CMDVersion=\"1.2\" isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        compContent += "    xsi:noNamespaceSchemaLocation=\"../../cmd-component.xsd\">\n";
        compContent += "    <Header>\n";
        compContent += "        <ID>clarin.eu:cr1:p_12345678</ID>\n";
        compContent += "        <Name>Nested</Name>\n";
        compContent += "        <Status>development</Status>\n";
        compContent += "    </Header>\n";
        compContent += "    <Component name=\"Nested\">\n";
        compContent += "        <Element name=\"Availability\" ValueScheme=\"string\" />\n";
        compContent += "        <Component ComponentRef=\""
                + desc.getId()
                + "\" name=\"Recursive\" CardinalityMin=\"1\" CardinalityMax=\"1\" />\n";
        compContent += "    </Component>\n";
        compContent += "\n";
        compContent += "</ComponentSpec>\n";

        // Update component
        form = createFormData(
                RegistryTestHelper.getComponentContentAsStream(compContent),
                "UPDATE DESCRIPTION!");
        cResponse = getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components/" + desc.getId() + "/update")).type(
                MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                cResponse.getStatus());
        response = cResponse.getEntity(RegisterResponse.class);
        assertFalse("Recursive definition should fail", response.isRegistered());
        assertEquals("There should be an error message for the recursion", 1,
                response.getErrors().size());
        assertTrue(
                "There error message should specify the point of recursion",
                response.getErrors().get(0)
                        .contains("already contains " + desc.getId()));

    }

    @Test
    public void testUpdatePrivateComponent() throws Exception {

        System.out.println("testUpdatePrivateComponent");

        fillUpPrivateItems();

        List<ComponentDescription> components = getUserComponents();
        assertEquals("user registered components", 1, components.size());
        assertEquals("public registered components", 0, getPublicComponents()
                .size());

        // first, post a component to update
        FormDataMultiPart form = createFormData(RegistryTestHelper
                .getComponentTestContent());
        ClientResponse cResponse = getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components")).type(
                MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                cResponse.getStatus());
        RegisterResponse response = cResponse.getEntity(RegisterResponse.class);
        assertTrue(response.isRegistered());
        assertFalse(response.isProfile());
        assertTrue(response.isPrivate());
        ComponentDescription desc = (ComponentDescription) response
                .getDescription();
        assertNotNull(desc);
        assertEquals("Access", desc.getName());
        assertEquals("Access description", desc.getDescription());
        Date firstDate = desc.getRegistrationDate();
        ComponentSpec spec = getUserComponent(desc);
        assertNotNull(spec);
        assertEquals("Access", spec.getComponent().getName());
        components = getUserComponents();
        assertEquals(2, components.size());
        assertEquals(0, getPublicComponents().size());
        assertEquals(DEVELOPMENT.toString(), spec.getHeader().getStatus());

        // second, now update
        form = createFormData(
                RegistryTestHelper.getComponentTestContentAsStream("TESTNAME"),
                "UPDATE DESCRIPTION!");
        cResponse = getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/components/" + desc.getId() + "/update")).type(
                MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                cResponse.getStatus());
        response = cResponse.getEntity(RegisterResponse.class);
        assertTrue(response.isRegistered());
        assertFalse(response.isProfile());
        assertTrue(response.isPrivate());
        desc = (ComponentDescription) response.getDescription();
        assertNotNull(desc);
        assertEquals("TESTNAME", desc.getName());
        assertEquals("TESTNAME description", desc.getDescription());
        Date secondDate = desc.getRegistrationDate();
        assertTrue(firstDate.before(secondDate) || firstDate.equals(secondDate));

        spec = getUserComponent(desc);
        assertNotNull(spec);
        assertEquals("TESTNAME", spec.getComponent().getName());
        components = getUserComponents();
        assertEquals(2, components.size());
        assertEquals(0, getPublicComponents().size());
        assertEquals("Component status should not have been altered", DEVELOPMENT.toString(), spec.getHeader().getStatus());
    }

    @Test
    public void testUpdatePrivateProfile() throws Exception {

        System.out.println("testUpdatePrivateProfile");

        fillUpPrivateItems();

        List<ProfileDescription> profiles = getUserProfiles();
        assertEquals(1, profiles.size());
        assertEquals(0, getPublicProfiles().size());

        FormDataMultiPart form = createFormData(RegistryTestHelper
                .getTestProfileContent());
        ClientResponse cResponse = getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/profiles")).type(
                MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                cResponse.getStatus());
        RegisterResponse response = cResponse.getEntity(RegisterResponse.class);
        assertTrue(response.isRegistered());
        assertTrue(response.isProfile());
        assertTrue(response.isPrivate());
        ProfileDescription desc = (ProfileDescription) response
                .getDescription();
        assertNotNull(desc);
        assertEquals("Actor", desc.getName());
        assertEquals("Actor description", desc.getDescription());
        assertEquals("TestGroup", desc.getGroupName());
        Date firstDate = desc.getRegistrationDate();
        ComponentSpec spec = getUserProfile(desc);
        assertNotNull(spec);
        assertEquals("Actor", spec.getComponent().getName());
        profiles = getUserProfiles();
        assertEquals(2, profiles.size());
        assertEquals(0, getPublicComponents().size());
        assertEquals(DEVELOPMENT.toString(), spec.getHeader().getStatus());

        // Now update
        form = createFormData(
                RegistryTestHelper.getTestProfileContent("TESTNAME", DEVELOPMENT.toString()),
                "UPDATE DESCRIPTION!");
        cResponse = getAuthenticatedResource(getResource().path(
                REGISTRY_BASE + "/profiles/" + desc.getId() + "/update")).type(
                MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
        assertEquals(ClientResponse.Status.OK.getStatusCode(),
                cResponse.getStatus());
        response = cResponse.getEntity(RegisterResponse.class);
        assertTrue(response.isRegistered());
        assertTrue(response.isProfile());
        assertTrue(response.isPrivate());
        desc = (ProfileDescription) response.getDescription();
        assertNotNull(desc);
        assertEquals("TESTNAME", desc.getName());
        assertEquals("TESTNAME description", desc.getDescription());
        Date secondDate = desc.getRegistrationDate();
        assertTrue(firstDate.before(secondDate) || firstDate.equals(secondDate));

        spec = getUserProfile(desc);
        assertNotNull(spec);
        assertEquals("TESTNAME", spec.getComponent().getName());
        profiles = getUserProfiles();
        assertEquals(2, profiles.size());
        assertEquals(0, getPublicComponents().size());
        assertEquals("Component status should not have been altered", DEVELOPMENT.toString(), spec.getHeader().getStatus());
    }

    private ComponentSpec getUserComponent(ComponentDescription desc) {
        return getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + desc.getId())
                .queryParam(REGISTRY_SPACE_PARAM, REGISTRY_SPACE_PRIVATE))
                .accept(MediaType.APPLICATION_XML)
                .get(ComponentSpec.class);
    }

    private ComponentSpec getUserProfile(ProfileDescription desc) {
        return getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + desc.getId())
                .queryParam(REGISTRY_SPACE_PARAM, REGISTRY_SPACE_PRIVATE))
                .accept(MediaType.APPLICATION_XML)
                .get(ComponentSpec.class);
    }

    private List<ComponentDescription> getPublicComponents() {
        return getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components")
                .queryParam(STATUS_FILTER_PARAM, "*"))
                .accept(MediaType.APPLICATION_XML)
                .get(COMPONENT_LIST_GENERICTYPE);
    }

    private List<ComponentDescription> getUserComponents() {
        return getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components")
                .queryParam(STATUS_FILTER_PARAM, "*")
                .queryParam(REGISTRY_SPACE_PARAM, REGISTRY_SPACE_PRIVATE))
                .accept(MediaType.APPLICATION_XML)
                .get(COMPONENT_LIST_GENERICTYPE);
    }

    @Test
    public void testRegisterComponent() throws Exception {

        System.out.println("testRegisterComponent");

        fillUpPrivateItems();

        FormDataMultiPart form = new FormDataMultiPart();
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD,
                RegistryTestHelper.getComponentTestContent(),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field(ComponentRegistryRestService.NAME_FORM_FIELD,
                "ComponentTest1");
        form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD,
                "My Test Component");
        form.field(ComponentRegistryRestService.DOMAIN_FORM_FIELD,
                "TestDomain");
        form.field(ComponentRegistryRestService.GROUP_FORM_FIELD, "TestGroup");
        RegisterResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/components").type(MediaType.MULTIPART_FORM_DATA)
                .post(RegisterResponse.class, form);
        assertTrue(response.isRegistered());
        assertFalse(response.isProfile());
        assertTrue(response.isPrivate());
        ComponentDescription desc = (ComponentDescription) response
                .getDescription();
        assertNotNull(desc);
        assertEquals("Spec header should override", "Access", desc.getName());
        assertEquals("Spec header should override", "Access description", desc.getDescription());
        assertEquals(expectedUserId("JUnit@test.com"), desc.getUserId());
        assertEquals("Database test user", desc.getCreatorName());
        assertEquals("TestGroup", desc.getGroupName());
        assertEquals("TestDomain", desc.getDomainName());
        assertTrue(desc.getId()
                .startsWith(REGISTRY_ID + "c_"));
        assertNotNull(desc.getRegistrationDate());
        String url = getResource().path(REGISTRY_BASE + "/components/" + desc.getId()).getUriBuilder().build().toString();
        assertEquals(url,
                desc.getHref());
    }

    @Test
    public void testRegisterComponentNonCanonical() throws Exception {

        System.out.println("testRegisterComponentNonCanonical");

        fillUpPrivateItems();

        final FormDataMultiPart form = new FormDataMultiPart();
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD,
                RegistryTestHelper.getComponentTestContent(),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field(ComponentRegistryRestService.NAME_FORM_FIELD,
                "ComponentTest1");
        form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD,
                "My Test Component");
        form.field(ComponentRegistryRestService.DOMAIN_FORM_FIELD,
                "TestDomain");
        form.field(ComponentRegistryRestService.GROUP_FORM_FIELD, "TestGroup");

        final ClientResponse cResponse = getAuthenticatedResource(
                NON_CANONICAL_REGISTRY_BASE + "/components").type(MediaType.MULTIPART_FORM_DATA)
                .post(ClientResponse.class, form);
        assertEquals(Status.BAD_REQUEST.getStatusCode(), cResponse.getStatus());
    }

    @Test
    public void testRegisterComment() throws Exception {

        System.out.println("testRegisterComponent");

        fillUpPublicItems();

        FormDataMultiPart form = new FormDataMultiPart();
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD,
                RegistryTestHelper.getCommentTestContent(),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        String id = ProfileDescription.PROFILE_PREFIX + "profile1";
        CommentResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/" + id + "/comments").type(
                        MediaType.MULTIPART_FORM_DATA)
                .post(CommentResponse.class, form);
        assertTrue(response.isRegistered());
        assertFalse(response.isPrivate());
        Comment comment = response.getComment();
        assertNotNull(comment);
        assertEquals("Actual", comment.getComment());
        assertEquals("Database test user", comment.getUserName());
        Assert.notNull(comment.getCommentDate());
        Assert.hasText(comment.getId());

        // User id should not be serialized!
        assertEquals(0, comment.getUserId());
    }

    @Test
    public void testRegisterCommentToNonExistent() throws Exception {
        fillUpPublicItems();

        FormDataMultiPart form = new FormDataMultiPart();
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD,
                RegistryTestHelper.getCommentTestContent(),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        ClientResponse cResponse = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles/clarin.eu:cr1:profile99/comments").type(
                        MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
        assertEquals(404, cResponse.getStatus());
    }

    @Test
    public void testRegisterCommentUnauthenticated() throws Exception {

        System.out.println("testRegisterCommentUnauthentified");

        fillUpPublicItems();

        FormDataMultiPart form = new FormDataMultiPart();
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD,
                RegistryTestHelper.getCommentTestContent(),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        ClientResponse cResponse = getResource()
                .path(REGISTRY_BASE + "/profiles/" + ProfileDescription.PROFILE_PREFIX + "profile1/comments")
                .type(MediaType.MULTIPART_FORM_DATA)
                .post(ClientResponse.class, form);
        assertEquals(401, cResponse.getStatus());
    }

    @Test
    public void testRegisterProfileInvalidData() throws Exception {
        System.out.println("testRegisterProfileInvalidData");
        fillUpPrivateItems();
        FormDataMultiPart form = new FormDataMultiPart();
        String notAValidProfile = "<ComponentSpec> </ComponentSpec>";
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD,
                new ByteArrayInputStream(notAValidProfile.getBytes()),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field(ComponentRegistryRestService.NAME_FORM_FIELD,
                "ProfileTest1");
        form.field(ComponentRegistryRestService.DOMAIN_FORM_FIELD, "Domain");
        form.field(ComponentRegistryRestService.GROUP_FORM_FIELD, "Group");
        form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD,
                "My Test Profile");
        RegisterResponse postResponse = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles").type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);
        assertTrue(postResponse.isProfile());
        assertFalse(postResponse.isRegistered());
        assertEquals(1, postResponse.getErrors().size());
        assertTrue(postResponse.getErrors().get(0).contains("isProfile"));
    }

    /**
     * Two elements on the same level with the same name violates schematron
     * rule, and should fail validation
     *
     * @throws Exception
     */
    @Test
    public void testRegisterInvalidProfile() throws Exception {
        System.out.println("testRegisterInvalidProfile");
        fillUpPrivateItems();
        FormDataMultiPart form = new FormDataMultiPart();
        String profileContent = "";
        profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        profileContent += "<ComponentSpec CMDVersion=\"1.2\" isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        profileContent += "    xsi:noNamespaceSchemaLocation=\"cmd-component.xsd\">\n";
        profileContent += "    <Header />\n";
        profileContent += "    <Component name=\"ProfileTest1\" CardinalityMin=\"0\" CardinalityMax=\"unbounded\">\n";
        profileContent += "        <Element name=\"Age\">\n";
        profileContent += "            <ValueScheme>\n";
        profileContent += "                <pattern>[23][0-9]</pattern>\n";
        profileContent += "            </ValueScheme>\n";
        profileContent += "        </Element>\n";
        profileContent += "        <Element name=\"Age\">\n";
        profileContent += "            <ValueScheme>\n";
        profileContent += "                <pattern>[23][0-9]</pattern>\n";
        profileContent += "            </ValueScheme>\n";
        profileContent += "        </Element>\n";
        profileContent += "    </Component>\n";
        profileContent += "</ComponentSpec>\n";
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD,
                RegistryTestHelper.getComponentContentAsStream(profileContent),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field(ComponentRegistryRestService.NAME_FORM_FIELD,
                "ProfileTest1");
        form.field(ComponentRegistryRestService.DOMAIN_FORM_FIELD,
                "TestDomain");
        form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD,
                "My Test Profile");
        form.field(ComponentRegistryRestService.GROUP_FORM_FIELD,
                "My Test Group");
        RegisterResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles").type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);
        assertFalse(
                "Subsequent elements should not be allowed to have the same name",
                response.isRegistered());
        assertTrue(response.getErrors().get(0)
                .contains(ComponentSpecValidator.VALIDATION_ERROR));
    }

    @Test
    public void testRegisterProfileInvalidDescriptionAndContent()
            throws Exception {
        System.out.println("testRegisterProfileInvalidDescriptionAndContent");
        fillUpPrivateItems();
        FormDataMultiPart form = new FormDataMultiPart();
        String profileContent = "";
        profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        profileContent += "<ComponentSpec> \n"; // No isProfile attribute
        profileContent += "    <Header />\n";
        profileContent += "    <Component name=\"Actor\" CardinalityMin=\"0\" CardinalityMax=\"unbounded\">\n";
        profileContent += "        <AttributeList>\n";
        profileContent += "            <Attribute>\n";
        profileContent += "                <Name>Name</Name>\n";
        profileContent += "                <Type>string</Type>\n";
        profileContent += "            </Attribute>\n";
        profileContent += "        </AttributeList>\n";
        profileContent += "    </Component>\n";
        profileContent += "</ComponentSpec>\n";
        form.field("data", new ByteArrayInputStream(profileContent.getBytes()),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field("name", "");// Empty name so invalid
        form.field("description", "My Test Profile");
        RegisterResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles").type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);
        assertFalse(response.isRegistered());
        assertEquals(2, response.getErrors().size());
        assertNotNull(response.getErrors().get(0));
        assertEquals(ComponentSpecValidator.VALIDATION_ERROR, response.getErrors().get(1)
                .substring(0, ComponentSpecValidator.VALIDATION_ERROR.length()));
    }

    @Test
    public void testRegisterLargeProfile() throws Exception {
        System.out.println("testRegisterLargeProfile");
        //fillUpPrivateItems(); not necessary
        FormDataMultiPart form = new FormDataMultiPart();
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD,
                RegistryTestHelper.getLargeProfileContent(),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field(ComponentRegistryRestService.NAME_FORM_FIELD,
                "ProfileTest1");
        form.field(ComponentRegistryRestService.DOMAIN_FORM_FIELD,
                "TestDomain");
        form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD,
                "My Test Profile");
        form.field(ComponentRegistryRestService.GROUP_FORM_FIELD,
                "My Test Group");
        ClientResponse response = getAuthenticatedResource(REGISTRY_BASE + "/profiles")
                .type(MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class,
                form);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testRegisterComponentAsProfile() throws Exception {
        System.out.println("tesRegisterComponentAsProfile");
        //fillUpPrivateItems();; not necessary
        FormDataMultiPart form = new FormDataMultiPart();
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD,
                RegistryTestHelper.getComponentTestContent(),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field(ComponentRegistryRestService.NAME_FORM_FIELD, "t");
        form.field(ComponentRegistryRestService.DOMAIN_FORM_FIELD, "domain");
        form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD,
                "My Test");
        form.field(ComponentRegistryRestService.GROUP_FORM_FIELD, "My Group");
        RegisterResponse response = getAuthenticatedResource(
                REGISTRY_BASE + "/profiles").type(MediaType.MULTIPART_FORM_DATA).post(
                RegisterResponse.class, form);
        assertFalse(response.isRegistered());
        assertTrue(response.isProfile());
        assertEquals(1, response.getErrors().size());
        assertEquals(ComponentSpecValidator.MISMATCH_ERROR, response.getErrors().get(0));
    }

    @Test
    public void testGetDescription() throws Exception {
        System.out.println("testGetDescription");
        fillUpPublicItems();
        String id = ComponentDescription.COMPONENT_PREFIX + "component1";
        ComponentDescription component = getAuthenticatedResource(getResource().path("/items/" + id)).accept(MediaType.APPLICATION_JSON).get(ComponentDescription.class);
        assertNotNull(component);
        assertEquals(id, component.getId());
        assertEquals("component1", component.getName());
        assertEquals("component1 description", component.getDescription());
    }

    @Test
    public void testGetComponentStatus() throws Exception {
        fillUpPrivateItems();
        fillUpPublicItems();
        final String privateId = getUserComponents().get(0).getId();
        final String publicId = getPublicComponents().get(0).getId();

        //get status for private
        {
            ClientResponse cResponse = getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components/" + privateId + "/status")).type(
                    MediaType.MULTIPART_FORM_DATA).get(ClientResponse.class);
            assertEquals(ClientResponse.Status.OK.getStatusCode(), cResponse.getStatus());
            assertEquals("development", cResponse.getEntity(String.class));
        }

        //get status for public
        {
            ClientResponse cResponse = getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components/" + publicId + "/status")).type(
                    MediaType.MULTIPART_FORM_DATA).get(ClientResponse.class);
            assertEquals(ClientResponse.Status.OK.getStatusCode(), cResponse.getStatus());
            assertEquals("production", cResponse.getEntity(String.class));
        }

        //deprecate via REST service
        {
            getAuthenticatedResource(getResource().path(
                    REGISTRY_BASE + "/components/" + publicId + "/status")).type(
                            MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class,
                            new FormDataMultiPart().field(ComponentRegistryRestService.STATUS_FORM_FIELD, "deprecated")
                    );
        }
        //check status
        {
            ClientResponse cResponse = getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/components/" + publicId + "/status")).type(
                    MediaType.MULTIPART_FORM_DATA).get(ClientResponse.class);
            assertEquals(ClientResponse.Status.OK.getStatusCode(), cResponse.getStatus());
            assertEquals("deprecated", cResponse.getEntity(String.class));
        }
    }

    @Test
    public void testGetProfileStatus() throws Exception {
        fillUpPrivateItems();
        fillUpPublicItems();
        final String privateId = getUserProfiles().get(0).getId();
        final String publicId = getPublicProfiles().get(0).getId();

        //get status for private
        {
            ClientResponse cResponse = getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/profiles/" + privateId + "/status")).type(
                    MediaType.MULTIPART_FORM_DATA).get(ClientResponse.class);
            assertEquals(ClientResponse.Status.OK.getStatusCode(), cResponse.getStatus());
            assertEquals("development", cResponse.getEntity(String.class));
        }

        //get status for public
        {
            ClientResponse cResponse = getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/profiles/" + publicId + "/status")).type(
                    MediaType.MULTIPART_FORM_DATA).get(ClientResponse.class);
            assertEquals(ClientResponse.Status.OK.getStatusCode(), cResponse.getStatus());
            assertEquals("production", cResponse.getEntity(String.class));
        }

        //deprecate via REST service
        {
            getAuthenticatedResource(getResource().path(
                    REGISTRY_BASE + "/profiles/" + publicId + "/status")).type(
                            MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class,
                            new FormDataMultiPart().field(ComponentRegistryRestService.STATUS_FORM_FIELD, "deprecated")
                    );
        }
        //check status
        {
            ClientResponse cResponse = getAuthenticatedResource(getResource().path(REGISTRY_BASE + "/profiles/" + publicId + "/status")).type(
                    MediaType.MULTIPART_FORM_DATA).get(ClientResponse.class);
            assertEquals(ClientResponse.Status.OK.getStatusCode(), cResponse.getStatus());
            assertEquals("deprecated", cResponse.getEntity(String.class));
        }
    }

    @Test
    public void testUpdateStatusOfPrivateComponent() throws Exception {
        fillUpPrivateItems();

        List<ComponentDescription> privateComponents = getUserComponents();
        assertEquals("private registered components", 1, privateComponents
                .size());
        assertEquals(ComponentStatus.DEVELOPMENT, privateComponents.get(0).getStatus());

        // Now update development to production (should fail)
        {
            FormDataMultiPart form = new FormDataMultiPart();
            form.field(ComponentRegistryRestService.STATUS_FORM_FIELD, "production");

            ClientResponse cResponse = getAuthenticatedResource(getResource().path(
                    REGISTRY_BASE + "/components/" + privateComponents.get(0).getId() + "/status")).type(
                    MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
            assertEquals(ClientResponse.Status.BAD_REQUEST.getStatusCode(), cResponse.getStatus());

            privateComponents = getUserComponents();
            assertEquals("Status should not have changed", ComponentStatus.DEVELOPMENT, privateComponents.get(0).getStatus());
        }

        // Now update development to deprecated (should work)
        {
            FormDataMultiPart form = new FormDataMultiPart();
            form.field(ComponentRegistryRestService.STATUS_FORM_FIELD, "deprecated");

            ClientResponse cResponse = getAuthenticatedResource(getResource().path(
                    REGISTRY_BASE + "/components/" + privateComponents.get(0).getId() + "/status")).type(
                    MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
            assertEquals(ClientResponse.Status.OK.getStatusCode(), cResponse.getStatus());

            privateComponents = getUserComponents();
            assertEquals("Status should have been updated", ComponentStatus.DEPRECATED, privateComponents.get(0).getStatus());
        }
    }

    @Test
    public void testUpdateStatusOfPrivateProfile() throws Exception {
        fillUpPrivateItems();

        List<ProfileDescription> privateProfiles = getUserProfiles();
        assertEquals("private registered profiles", 1, privateProfiles
                .size());
        assertEquals(ComponentStatus.DEVELOPMENT, privateProfiles.get(0).getStatus());

        // Now update development to production (should fail)
        {
            FormDataMultiPart form = new FormDataMultiPart();
            form.field(ComponentRegistryRestService.STATUS_FORM_FIELD, "production");

            ClientResponse cResponse = getAuthenticatedResource(getResource().path(
                    REGISTRY_BASE + "/profiles/" + privateProfiles.get(0).getId() + "/status")).type(
                    MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
            assertEquals(ClientResponse.Status.BAD_REQUEST.getStatusCode(), cResponse.getStatus());

            privateProfiles = getUserProfiles();
            assertEquals("Status should not have changed", ComponentStatus.DEVELOPMENT, privateProfiles.get(0).getStatus());
        }

        // Now update development to deprecated (should work)
        {
            FormDataMultiPart form = new FormDataMultiPart();
            form.field(ComponentRegistryRestService.STATUS_FORM_FIELD, "deprecated");

            ClientResponse cResponse = getAuthenticatedResource(getResource().path(
                    REGISTRY_BASE + "/profiles/" + privateProfiles.get(0).getId() + "/status")).type(
                    MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
            assertEquals(ClientResponse.Status.OK.getStatusCode(), cResponse.getStatus());

            privateProfiles = getUserProfiles();
            assertEquals("Status should have been updated", ComponentStatus.DEPRECATED, privateProfiles.get(0).getStatus());
        }
    }

    @Test
    public void testUpdateStatusOfPublicProfile() throws Exception {
        fillUpPublicItems();

        List<ProfileDescription> publicProfiles = getPublicProfiles();
        assertEquals("public registered profiles", 2, publicProfiles
                .size());
        assertEquals(ComponentStatus.PRODUCTION, publicProfiles.get(0).getStatus());
        assertEquals(ComponentStatus.PRODUCTION, publicProfiles.get(1).getStatus());

        // Now update production to deprecated (should work)
        {
            FormDataMultiPart form = new FormDataMultiPart();
            form.field(ComponentRegistryRestService.STATUS_FORM_FIELD, "deprecated");

            ClientResponse cResponse = getAuthenticatedResource(getResource().path(
                    REGISTRY_BASE + "/profiles/" + publicProfiles.get(0).getId() + "/status")).type(
                    MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
            assertEquals(ClientResponse.Status.OK.getStatusCode(),
                    cResponse.getStatus());

            publicProfiles = getPublicProfiles();
            assertEquals(ComponentStatus.DEPRECATED, publicProfiles.get(0).getStatus());
        }

        // Try to give a deprecated component production status (should fail)
        {
            FormDataMultiPart form = new FormDataMultiPart();
            form.field(ComponentRegistryRestService.STATUS_FORM_FIELD, "production");

            ClientResponse cResponse = getAuthenticatedResource(getResource().path(
                    REGISTRY_BASE + "/profiles/" + publicProfiles.get(0).getId() + "/status")).type(
                    MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
            assertEquals(ClientResponse.Status.BAD_REQUEST.getStatusCode(),
                    cResponse.getStatus());

            publicProfiles = getPublicProfiles();
            assertEquals(ComponentStatus.DEPRECATED, publicProfiles.get(0).getStatus());
        }

        // Try to give a deprecated component development status (should fail)
        {
            FormDataMultiPart form = new FormDataMultiPart();
            form.field(ComponentRegistryRestService.STATUS_FORM_FIELD, "development");

            ClientResponse cResponse = getAuthenticatedResource(getResource().path(
                    REGISTRY_BASE + "/profiles/" + publicProfiles.get(0).getId() + "/status")).type(
                    MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
            assertEquals(ClientResponse.Status.BAD_REQUEST.getStatusCode(),
                    cResponse.getStatus());

            publicProfiles = getPublicProfiles();
            assertEquals(ComponentStatus.DEPRECATED, publicProfiles.get(0).getStatus());
        }

        // Try to give a production component development status (should fail)
        {
            FormDataMultiPart form = new FormDataMultiPart();
            form.field(ComponentRegistryRestService.STATUS_FORM_FIELD, "development");

            ClientResponse cResponse = getAuthenticatedResource(getResource().path(
                    REGISTRY_BASE + "/profiles/" + publicProfiles.get(1).getId() + "/status")).type(
                    MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
            assertEquals(ClientResponse.Status.BAD_REQUEST.getStatusCode(),
                    cResponse.getStatus());

            publicProfiles = getPublicProfiles();
            assertEquals(ComponentStatus.PRODUCTION, publicProfiles.get(1).getStatus());
        }

        // Try to update to invalid status (should fail)
        {
            FormDataMultiPart form = new FormDataMultiPart();
            form.field(ComponentRegistryRestService.STATUS_FORM_FIELD, "invalidstatus");

            ClientResponse cResponse = getAuthenticatedResource(getResource().path(
                    REGISTRY_BASE + "/profiles/" + publicProfiles.get(1).getId() + "/status")).type(
                    MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
            assertEquals(ClientResponse.Status.BAD_REQUEST.getStatusCode(),
                    cResponse.getStatus());

            publicProfiles = getPublicProfiles();
            assertEquals(ComponentStatus.PRODUCTION, publicProfiles.get(1).getStatus());
        }
    }

    @Test
    public void testUpdateStatusOfPublicComponent() throws Exception {
        fillUpPublicItems();

        List<ComponentDescription> publicComponents = getPublicComponents();
        assertEquals("public registered components", 2, publicComponents
                .size());
        assertEquals(ComponentStatus.PRODUCTION, publicComponents.get(0).getStatus());
        assertEquals(ComponentStatus.PRODUCTION, publicComponents.get(1).getStatus());

        // Now update production to deprecated (should work)
        {
            FormDataMultiPart form = new FormDataMultiPart();
            form.field(ComponentRegistryRestService.STATUS_FORM_FIELD, "deprecated");

            ClientResponse cResponse = getAuthenticatedResource(getResource().path(
                    REGISTRY_BASE + "/components/" + publicComponents.get(0).getId() + "/status")).type(
                    MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
            assertEquals(ClientResponse.Status.OK.getStatusCode(),
                    cResponse.getStatus());

            publicComponents = getPublicComponents();
            assertEquals(ComponentStatus.DEPRECATED, publicComponents.get(0).getStatus());
        }

        // Try to give a deprecated component production status (should fail)
        {
            FormDataMultiPart form = new FormDataMultiPart();
            form.field(ComponentRegistryRestService.STATUS_FORM_FIELD, "production");

            ClientResponse cResponse = getAuthenticatedResource(getResource().path(
                    REGISTRY_BASE + "/components/" + publicComponents.get(0).getId() + "/status")).type(
                    MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
            assertEquals(ClientResponse.Status.BAD_REQUEST.getStatusCode(),
                    cResponse.getStatus());

            publicComponents = getPublicComponents();
            assertEquals(ComponentStatus.DEPRECATED, publicComponents.get(0).getStatus());
        }

        // Try to give a deprecated component development status (should fail)
        {
            FormDataMultiPart form = new FormDataMultiPart();
            form.field(ComponentRegistryRestService.STATUS_FORM_FIELD, "development");

            ClientResponse cResponse = getAuthenticatedResource(getResource().path(
                    REGISTRY_BASE + "/components/" + publicComponents.get(0).getId() + "/status")).type(
                    MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
            assertEquals(ClientResponse.Status.BAD_REQUEST.getStatusCode(),
                    cResponse.getStatus());

            publicComponents = getPublicComponents();
            assertEquals(ComponentStatus.DEPRECATED, publicComponents.get(0).getStatus());
        }

        // Try to give a production component development status (should fail)
        {
            FormDataMultiPart form = new FormDataMultiPart();
            form.field(ComponentRegistryRestService.STATUS_FORM_FIELD, "development");

            ClientResponse cResponse = getAuthenticatedResource(getResource().path(
                    REGISTRY_BASE + "/components/" + publicComponents.get(1).getId() + "/status")).type(
                    MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
            assertEquals(ClientResponse.Status.BAD_REQUEST.getStatusCode(),
                    cResponse.getStatus());

            publicComponents = getPublicComponents();
            assertEquals(ComponentStatus.PRODUCTION, publicComponents.get(1).getStatus());
        }

        // Try to update to invalid status (should fail)
        {
            FormDataMultiPart form = new FormDataMultiPart();
            form.field(ComponentRegistryRestService.STATUS_FORM_FIELD, "invalidstatus");

            ClientResponse cResponse = getAuthenticatedResource(getResource().path(
                    REGISTRY_BASE + "/components/" + publicComponents.get(1).getId() + "/status")).type(
                    MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class, form);
            assertEquals(ClientResponse.Status.BAD_REQUEST.getStatusCode(),
                    cResponse.getStatus());

            publicComponents = getPublicComponents();
            assertEquals(ComponentStatus.PRODUCTION, publicComponents.get(1).getStatus());
        }
    }

    @Test
    public void testGetComponentSuccessor() throws Exception {
        fillUpPublicItems();

        List<ComponentDescription> publicComponents = getPublicComponents();
        assertEquals("public registered components", 2, publicComponents
                .size());
        assertEquals(ComponentStatus.PRODUCTION, publicComponents.get(0).getStatus());
        assertEquals(ComponentStatus.PRODUCTION, publicComponents.get(1).getStatus());

        final String componentId = publicComponents.get(0).getId();
        final String successorId = publicComponents.get(1).getId();

        //get for published public - should be none
        {
            FormDataMultiPart form = new FormDataMultiPart();
            form.field(ComponentRegistryRestService.STATUS_FORM_FIELD, "deprecated");

            ClientResponse cResponse = getAuthenticatedResource(getResource()
                    .path(REGISTRY_BASE + "/components/" + componentId + "/successor"))
                    .get(ClientResponse.class);
            assertEquals(ClientResponse.Status.NOT_FOUND.getStatusCode(),
                    cResponse.getStatus());
        }
        //deprecate and assign successor via REST service
        {
            getAuthenticatedResource(getResource().path(
                    REGISTRY_BASE + "/components/" + componentId + "/status")).type(
                            MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class,
                            new FormDataMultiPart().field(ComponentRegistryRestService.STATUS_FORM_FIELD, "deprecated")
                    );
            getAuthenticatedResource(getResource().path(
                    REGISTRY_BASE + "/components/" + componentId + "/successor")).type(
                            MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class,
                            new FormDataMultiPart().field(ComponentRegistryRestService.SUCCESSOR_ID_FORM_FIELD, successorId)
                    );
        }

        //should be as set now
        publicComponents = getPublicComponents();
        assertEquals(successorId, publicComponents.get(0).getSuccessor());

        //get status via REST, should match
        {
            ClientResponse cResponse = getAuthenticatedResource(getResource()
                    .path(REGISTRY_BASE + "/components/" + componentId + "/successor"))
                    .get(ClientResponse.class);
            assertEquals(ClientResponse.Status.OK.getStatusCode(),
                    cResponse.getStatus());
            assertEquals(successorId, cResponse.getEntity(String.class));
        }
    }

    @Test
    public void testSetComponentSuccessor() throws Exception {
        fillUpPublicItems();
        fillUpPrivateItems();

        List<ComponentDescription> publicComponents = getPublicComponents();
        assertEquals(ComponentStatus.PRODUCTION, publicComponents.get(0).getStatus());
        assertEquals(ComponentStatus.PRODUCTION, publicComponents.get(1).getStatus());
        assertNull(publicComponents.get(0).getSuccessor());
        assertNull(publicComponents.get(1).getSuccessor());
        List<ComponentDescription> privateComponents = getUserComponents();
        assertEquals(ComponentStatus.DEVELOPMENT, privateComponents.get(0).getStatus());
        assertNull(privateComponents.get(0).getSuccessor());

        final String publicId = publicComponents.get(0).getId();
        final String privateId = privateComponents.get(0).getId();
        final String successorId = publicComponents.get(1).getId();

        //try to set on development private - fail
        {
            ClientResponse cResponse = getAuthenticatedResource(getResource()
                    .path(REGISTRY_BASE + "/components/" + privateId + "/successor"))
                    .type(MediaType.MULTIPART_FORM_DATA)
                    .post(ClientResponse.class,
                            new FormDataMultiPart()
                                    .field(ComponentRegistryRestService.SUCCESSOR_ID_FORM_FIELD, successorId));
            assertEquals(ClientResponse.Status.BAD_REQUEST.getStatusCode(), cResponse.getStatus());
            assertNull("No successor should have been set", getUserComponents().get(0).getSuccessor());
        }

        //try to set on published public - fail
        {
            ClientResponse cResponse = getAuthenticatedResource(getResource()
                    .path(REGISTRY_BASE + "/components/" + publicId + "/successor"))
                    .type(MediaType.MULTIPART_FORM_DATA)
                    .post(ClientResponse.class,
                            new FormDataMultiPart()
                                    .field(ComponentRegistryRestService.SUCCESSOR_ID_FORM_FIELD, successorId));
            assertEquals(ClientResponse.Status.BAD_REQUEST.getStatusCode(), cResponse.getStatus());
            assertNull("No successor should have been set", getPublicComponents().get(0).getSuccessor());
        }
        //deprecate one public component
        getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/components/" + publicId + "/status"))
                .type(MediaType.MULTIPART_FORM_DATA)
                .post(ClientResponse.class, new FormDataMultiPart().field(ComponentRegistryRestService.STATUS_FORM_FIELD, "deprecated"));
        //try to set non-existing successor on deprecated public - fail
        {
            ClientResponse cResponse = getAuthenticatedResource(getResource()
                    .path(REGISTRY_BASE + "/components/" + publicId + "/successor"))
                    .type(MediaType.MULTIPART_FORM_DATA)
                    .post(ClientResponse.class,
                            new FormDataMultiPart()
                                    .field(ComponentRegistryRestService.SUCCESSOR_ID_FORM_FIELD, "NON-EXISTING-ID"));
            assertEquals(ClientResponse.Status.BAD_REQUEST.getStatusCode(), cResponse.getStatus());
            assertNull("No successor should have been set", getPublicComponents().get(0).getSuccessor());
        }
        //try to set profile as successor to component
        {
            ClientResponse cResponse = getAuthenticatedResource(getResource()
                    .path(REGISTRY_BASE + "/components/" + publicId + "/successor"))
                    .type(MediaType.MULTIPART_FORM_DATA)
                    .post(ClientResponse.class,
                            new FormDataMultiPart()
                                    .field(ComponentRegistryRestService.SUCCESSOR_ID_FORM_FIELD, getPublicProfiles().get(0).getId()));
            assertEquals(ClientResponse.Status.BAD_REQUEST.getStatusCode(), cResponse.getStatus());
            assertNull("No successor should have been set", getPublicComponents().get(0).getSuccessor());
        }
        //try to set existing on deprecated public - success
        {
            ClientResponse cResponse = getAuthenticatedResource(getResource()
                    .path(REGISTRY_BASE + "/components/" + publicId + "/successor"))
                    .type(MediaType.MULTIPART_FORM_DATA)
                    .post(ClientResponse.class,
                            new FormDataMultiPart()
                                    .field(ComponentRegistryRestService.SUCCESSOR_ID_FORM_FIELD, successorId));
            assertEquals(ClientResponse.Status.OK.getStatusCode(), cResponse.getStatus());
            assertEquals("Successor should have been set", successorId, getPublicComponents().get(0).getSuccessor());
        }
        //try to set on same again - fail
        {
            ClientResponse cResponse = getAuthenticatedResource(getResource()
                    .path(REGISTRY_BASE + "/components/" + publicId + "/successor"))
                    .type(MediaType.MULTIPART_FORM_DATA)
                    .post(ClientResponse.class,
                            new FormDataMultiPart()
                                    .field(ComponentRegistryRestService.SUCCESSOR_ID_FORM_FIELD, successorId));
            assertEquals(ClientResponse.Status.BAD_REQUEST.getStatusCode(), cResponse.getStatus());
            assertEquals("Successor should have been kept", successorId, getPublicComponents().get(0).getSuccessor());
        }
    }

    @Test
    public void testGetProfileSuccessor() throws Exception {
        fillUpPublicItems();

        List<ProfileDescription> publicProfiles = getPublicProfiles();
        assertEquals("public registered components", 2, publicProfiles
                .size());
        assertEquals(ComponentStatus.PRODUCTION, publicProfiles.get(0).getStatus());
        assertEquals(ComponentStatus.PRODUCTION, publicProfiles.get(1).getStatus());

        final String profileId = publicProfiles.get(0).getId();
        final String successorId = publicProfiles.get(1).getId();

        //get for published public - should be none
        {
            FormDataMultiPart form = new FormDataMultiPart();
            form.field(ComponentRegistryRestService.STATUS_FORM_FIELD, "deprecated");

            ClientResponse cResponse = getAuthenticatedResource(getResource()
                    .path(REGISTRY_BASE + "/profiles/" + profileId + "/successor"))
                    .get(ClientResponse.class);
            assertEquals(ClientResponse.Status.NOT_FOUND.getStatusCode(),
                    cResponse.getStatus());
        }
        //deprecate and assign successor via REST service
        {
            getAuthenticatedResource(getResource().path(
                    REGISTRY_BASE + "/profiles/" + profileId + "/status")).type(
                            MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class,
                            new FormDataMultiPart().field(ComponentRegistryRestService.STATUS_FORM_FIELD, "deprecated")
                    );
            getAuthenticatedResource(getResource().path(
                    REGISTRY_BASE + "/profiles/" + profileId + "/successor")).type(
                            MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class,
                            new FormDataMultiPart().field(ComponentRegistryRestService.SUCCESSOR_ID_FORM_FIELD, successorId)
                    );
        }

        //should be as set now
        assertEquals(successorId, getPublicProfiles().get(0).getSuccessor());

        //get status via REST, should match
        {
            ClientResponse cResponse = getAuthenticatedResource(getResource()
                    .path(REGISTRY_BASE + "/profiles/" + profileId + "/successor"))
                    .get(ClientResponse.class);
            assertEquals(ClientResponse.Status.OK.getStatusCode(),
                    cResponse.getStatus());
            assertEquals(successorId, cResponse.getEntity(String.class));
        }
    }

    @Test
    public void testSetProfileSuccessor() throws Exception {
        fillUpPublicItems();
        fillUpPrivateItems();

        List<ProfileDescription> publicComponents = getPublicProfiles();
        assertEquals(ComponentStatus.PRODUCTION, publicComponents.get(0).getStatus());
        assertEquals(ComponentStatus.PRODUCTION, publicComponents.get(1).getStatus());
        assertNull(publicComponents.get(0).getSuccessor());
        assertNull(publicComponents.get(1).getSuccessor());
        List<ProfileDescription> privateComponents = getUserProfiles();
        assertEquals(ComponentStatus.DEVELOPMENT, privateComponents.get(0).getStatus());
        assertNull(privateComponents.get(0).getSuccessor());

        final String publicId = publicComponents.get(0).getId();
        final String privateId = privateComponents.get(0).getId();
        final String successorId = publicComponents.get(1).getId();

        //try to set on development private - fail
        {
            ClientResponse cResponse = getAuthenticatedResource(getResource()
                    .path(REGISTRY_BASE + "/profiles/" + privateId + "/successor"))
                    .type(MediaType.MULTIPART_FORM_DATA)
                    .post(ClientResponse.class,
                            new FormDataMultiPart()
                                    .field(ComponentRegistryRestService.SUCCESSOR_ID_FORM_FIELD, successorId));
            assertEquals(ClientResponse.Status.BAD_REQUEST.getStatusCode(), cResponse.getStatus());
            assertNull("No successor should have been set", getUserProfiles().get(0).getSuccessor());
        }

        //try to set on published public - fail
        {
            ClientResponse cResponse = getAuthenticatedResource(getResource()
                    .path(REGISTRY_BASE + "/profiles/" + publicId + "/successor"))
                    .type(MediaType.MULTIPART_FORM_DATA)
                    .post(ClientResponse.class,
                            new FormDataMultiPart()
                                    .field(ComponentRegistryRestService.SUCCESSOR_ID_FORM_FIELD, successorId));
            assertEquals(ClientResponse.Status.BAD_REQUEST.getStatusCode(), cResponse.getStatus());
            assertNull("No successor should have been set", getPublicProfiles().get(0).getSuccessor());
        }
        //deprecate one public component
        getAuthenticatedResource(getResource()
                .path(REGISTRY_BASE + "/profiles/" + publicId + "/status"))
                .type(MediaType.MULTIPART_FORM_DATA)
                .post(ClientResponse.class, new FormDataMultiPart().field(ComponentRegistryRestService.STATUS_FORM_FIELD, "deprecated"));
        //try to set non-existing successor on deprecated public - fail
        {
            ClientResponse cResponse = getAuthenticatedResource(getResource()
                    .path(REGISTRY_BASE + "/profiles/" + publicId + "/successor"))
                    .type(MediaType.MULTIPART_FORM_DATA)
                    .post(ClientResponse.class,
                            new FormDataMultiPart()
                                    .field(ComponentRegistryRestService.SUCCESSOR_ID_FORM_FIELD, "NON-EXISTING-ID"));
            assertEquals(ClientResponse.Status.BAD_REQUEST.getStatusCode(), cResponse.getStatus());
            assertNull("No successor should have been set", getPublicProfiles().get(0).getSuccessor());
        }
        //try to set profile as successor to component
        {
            ClientResponse cResponse = getAuthenticatedResource(getResource()
                    .path(REGISTRY_BASE + "/profiles/" + publicId + "/successor"))
                    .type(MediaType.MULTIPART_FORM_DATA)
                    .post(ClientResponse.class,
                            new FormDataMultiPart()
                                    .field(ComponentRegistryRestService.SUCCESSOR_ID_FORM_FIELD, getPublicProfiles().get(0).getId()));
            assertEquals(ClientResponse.Status.BAD_REQUEST.getStatusCode(), cResponse.getStatus());
            assertNull("No successor should have been set", getPublicProfiles().get(0).getSuccessor());
        }
        //try to set existing on deprecated public - success
        {
            ClientResponse cResponse = getAuthenticatedResource(getResource()
                    .path(REGISTRY_BASE + "/profiles/" + publicId + "/successor"))
                    .type(MediaType.MULTIPART_FORM_DATA)
                    .post(ClientResponse.class,
                            new FormDataMultiPart()
                                    .field(ComponentRegistryRestService.SUCCESSOR_ID_FORM_FIELD, successorId));
            assertEquals(ClientResponse.Status.OK.getStatusCode(), cResponse.getStatus());
            assertEquals("Successor should have been set", successorId, getPublicProfiles().get(0).getSuccessor());
        }
        //try to set on same again - fail
        {
            ClientResponse cResponse = getAuthenticatedResource(getResource()
                    .path(REGISTRY_BASE + "/profiles/" + publicId + "/successor"))
                    .type(MediaType.MULTIPART_FORM_DATA)
                    .post(ClientResponse.class,
                            new FormDataMultiPart()
                                    .field(ComponentRegistryRestService.SUCCESSOR_ID_FORM_FIELD, successorId));
            assertEquals(ClientResponse.Status.BAD_REQUEST.getStatusCode(), cResponse.getStatus());
            assertEquals("Successor should have been kept", successorId, getPublicProfiles().get(0).getSuccessor());
        }
    }

    @Test
    public void testGetProfilesObject() throws Exception {
        System.out.println("testGetProfilesObject");

        fillUpPublicItems();

        RegistryTestHelper.addProfile(baseRegistry, "PROFILE2", true);
        ProfileDescriptionsObject response = getResource()
                .path(REGISTRY_BASE + "/profilesList").accept(MediaType.APPLICATION_XML)
                .get(ProfileDescriptionsObject.class);
        assertEquals(3, response.getProfileDescriptions().size());
        response = getResource()
                .path(REGISTRY_BASE + "/profilesList")
                .accept(MediaType.APPLICATION_JSON)
                .get(ProfileDescriptionsObject.class);
        assertEquals(3, response.getProfileDescriptions().size());
        assertEquals("profile1", response.getProfileDescriptions().get(0).getName());
        assertEquals("PROFILE2", response.getProfileDescriptions().get(1).getName());
        assertEquals("profile2", response.getProfileDescriptions().get(2).getName());
    }
}
