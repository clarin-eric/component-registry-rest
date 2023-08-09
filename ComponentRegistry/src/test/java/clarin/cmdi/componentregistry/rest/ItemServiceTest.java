/*
 * Copyright (C) 2017 CLARIN ERIC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.ItemLockService;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryTestDatabase;
import clarin.cmdi.componentregistry.impl.database.ValidationException;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ComponentStatus;
import clarin.cmdi.componentregistry.model.Group;
import clarin.cmdi.componentregistry.model.ItemLock;
import clarin.cmdi.componentregistry.model.Ownership;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.GenericType;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import clarin.cmdi.componentregistry.GroupService;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class ItemServiceTest extends ComponentRegistryRestServiceTestCase {

    private static final GenericType<List<Group>> GENERIC_GROUP_LIST_TYPE = new GenericType<List<Group>>() {
    };

    @Autowired
    private ComponentRegistryFactory componentRegistryFactory;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private GroupService groupService;
    @Autowired
    private ItemLockService itemLockService;

    private RegistryUser registryUser;
    private ComponentRegistry baseRegistry;

    @Before
    public void init() {
        ComponentRegistryTestDatabase.resetAndCreateAllTables(jdbcTemplate);
        registryUser = createUserRecord();
        baseRegistry = componentRegistryFactory.getBaseRegistry(DummyPrincipal.DUMMY_CREDENTIALS);
    }

    private long createTeam(String teamName) throws ItemNotFoundException, ValidationException {
        long groupId = groupService.createNewGroup(teamName, registryUser.getPrincipalName());
        groupService.makeMember(registryUser.getPrincipalName(), teamName);
        return groupId;
    }

    private void addToTeam(final String componentId, final long groupId) {
        final Ownership ownership = new Ownership();
        ownership.setComponentRef(componentId);
        ownership.setGroupId(groupId);
        groupService.addOwnership(ownership);
    }

    @Test
    public void testGetPublicProfileDescription() throws Exception {
        final String profileId = RegistryTestHelper.addProfile(baseRegistry, "profile1", true).getId();

        BaseDescription result = getResource().path("/items/" + profileId)
                .accept(MediaType.APPLICATION_XML)
                .get(BaseDescription.class);
        assertNotNull(result);
        assertThat(result, instanceOf(ProfileDescription.class));
        assertEquals(profileId, result.getId());
    }

    @Test
    public void testGetPublicComponentDescription() throws Exception {
        final String componentId = RegistryTestHelper.addComponent(baseRegistry, "component1", true).getId();

        BaseDescription result = getResource().path("/items/" + componentId)
                .accept(MediaType.APPLICATION_XML)
                .get(BaseDescription.class);
        assertNotNull(result);
        assertThat(result, instanceOf(ComponentDescription.class));
        assertEquals(componentId, result.getId());
    }

    @Test
    public void testGetPrivateProfileDescription() throws Exception {
        final String profileId = RegistryTestHelper.addProfile(baseRegistry, "profile3", false).getId();

        BaseDescription result = getAuthenticatedResource("/items/" + profileId)
                .accept(MediaType.APPLICATION_XML)
                .get(BaseDescription.class);
        assertNotNull(result);
        assertThat(result, instanceOf(ProfileDescription.class));
        assertEquals(profileId, result.getId());
    }

    @Test
    public void testGetPrivateComponentDescription() throws Exception {
        final String componentId = RegistryTestHelper.addComponent(baseRegistry, "component3", false).getId();

        BaseDescription result = getAuthenticatedResource("/items/" + componentId)
                .accept(MediaType.APPLICATION_XML)
                .get(BaseDescription.class);
        assertNotNull(result);
        assertThat(result, instanceOf(ComponentDescription.class));
        assertEquals(componentId, result.getId());
    }

    @Test
    public void testGetPrivateItemUnAuthenticated() throws Exception {
        final String profileId = RegistryTestHelper.addProfile(baseRegistry, "profile3", false).getId();

        ClientResponse result = getResource().path("/items/" + profileId)
                .accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);
        assertEquals(Status.UNAUTHORIZED.getStatusCode(), result.getStatus());
    }

    @Test
    public void testGetPrivateItemForbidden() throws Exception {
        final String componentId = RegistryTestHelper.addComponentAnotherPrincipal(baseRegistry, "component3", false).getId();

        ClientResponse result = getAuthenticatedResource("/items/" + componentId)
                .accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);
        assertEquals(Status.FORBIDDEN.getStatusCode(), result.getStatus());
    }

    @Test
    public void testGetItemGroups() throws Exception {
        final String componentId = RegistryTestHelper.addComponent(baseRegistry, "component3", false).getId();
        addToTeam(componentId, createTeam("testGroup1"));
        addToTeam(componentId, createTeam("testGroup2"));

        final List<Group> result = getAuthenticatedResource("/items/" + componentId + "/groups")
                .accept(MediaType.APPLICATION_XML)
                .get(GENERIC_GROUP_LIST_TYPE);
        assertEquals(2, result.size());
        assertEquals("testGroup1", result.get(0).getName());
        assertEquals("testGroup2", result.get(1).getName());
    }

    @Test
    public void testTransferItemToGroup() throws Exception {
        final String componentId = RegistryTestHelper.addComponent(baseRegistry, "component3", false).getId();
        addToTeam(componentId, createTeam("testGroup1"));
        //target team
        long targetTeamId = createTeam("testGroup2");

        final ClientResponse result = getAuthenticatedResource(
                getResource().path("/items/" + componentId + "/transferownership").queryParam("groupId", String.valueOf(targetTeamId)))
                .accept(MediaType.APPLICATION_XML)
                .post(ClientResponse.class);
        assertEquals(Status.OK.getStatusCode(), result.getStatus());
    }

    @Test
    public void testGetItemLock() throws Exception {
        final String componentId = RegistryTestHelper.addComponent(baseRegistry, "component3", false).getId();
        itemLockService.setLock(componentId, registryUser.getPrincipalName());

        final ItemLock result = getAuthenticatedResource("/items/" + componentId + "/lock")
                .accept(MediaType.APPLICATION_XML)
                .get(ItemLock.class);
        assertNotNull("Item lock should have been retrieved", result);
        assertEquals("Item id should match", (long) baseRegistry.getComponentDescription(componentId).getDbId(), result.getItemId().longValue());
        assertEquals("User id should match", registryUser.getId(), result.getUserId());
    }

    @Test
    public void testGetItemLockNoLock() throws Exception {
        final String componentId = RegistryTestHelper.addComponent(baseRegistry, "component3", false).getId();

        final ClientResponse result = getAuthenticatedResource("/items/" + componentId + "/lock")
                .accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);
        assertEquals("Not found status expected for non-existent lock", Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }

    @Test
    public void testSetItemLock() throws Exception {
        final String componentId = RegistryTestHelper.addComponent(baseRegistry, "component3", false).getId();
        final ClientResponse result = getAuthenticatedResource("/items/" + componentId + "/lock")
                .accept(MediaType.APPLICATION_XML)
                .put(ClientResponse.class);
        assertEquals(Status.OK.getStatusCode(), result.getStatus());

        final ItemLock lock = itemLockService.getLock(componentId);
        assertNotNull("Lock should have been created", lock);
        assertEquals("Item id should match", (long) baseRegistry.getComponentDescription(componentId).getDbId(), lock.getItemId().longValue());
        assertEquals("User id should match", registryUser.getId(), lock.getUserId());
    }

    @Test
    public void testSetItemLockUnauthenticated() throws Exception {
        final String componentId = RegistryTestHelper.addComponent(baseRegistry, "component3", false).getId();
        final ClientResponse result = getResource().path("/items/" + componentId + "/lock")
                .accept(MediaType.APPLICATION_XML)
                .put(ClientResponse.class);
        assertEquals(Status.UNAUTHORIZED.getStatusCode(), result.getStatus());

        assertNull("Lock should not have been created", itemLockService.getLock(componentId));
    }

    @Test
    public void testSetItemLockExistingLock() throws Exception {
        final String componentId = RegistryTestHelper.addComponent(baseRegistry, "component3", false).getId();
        //put once
        getAuthenticatedResource("/items/" + componentId + "/lock")
                .accept(MediaType.APPLICATION_XML)
                .put();
        //put again
        final ClientResponse result = getAuthenticatedResource("/items/" + componentId + "/lock")
                .accept(MediaType.APPLICATION_XML)
                .put(ClientResponse.class);
        assertEquals(Status.CONFLICT.getStatusCode(), result.getStatus());
    }

    @Test
    public void testRemoveItemLock() throws Exception {
        final String componentId = RegistryTestHelper.addComponent(baseRegistry, "component3", false).getId();
        itemLockService.setLock(componentId, registryUser.getPrincipalName());
        assertNotNull("Lock should be present before deletion", itemLockService.getLock(componentId));
        final ClientResponse result = getAuthenticatedResource("/items/" + componentId + "/lock")
                .accept(MediaType.APPLICATION_XML)
                .delete(ClientResponse.class);
        assertEquals(Status.OK.getStatusCode(), result.getStatus());

        assertNull("Lock should have been removed", itemLockService.getLock(componentId));
    }

    @Test
    public void testRemoveItemLockUnauthenticated() throws Exception {
        final String componentId = RegistryTestHelper.addComponent(baseRegistry, "component3", false).getId();
        itemLockService.setLock(componentId, registryUser.getPrincipalName());
        final ClientResponse result = getResource().path("/items/" + componentId + "/lock")
                .accept(MediaType.APPLICATION_XML)
                .delete(ClientResponse.class);
        assertEquals(Status.UNAUTHORIZED.getStatusCode(), result.getStatus());

        assertNotNull("Lock should not have been removed", itemLockService.getLock(componentId));
    }

    @Test
    public void testGetItemLockNonExistentItem() throws Exception {
        final ClientResponse result = getResource().path("/items/" + ComponentDescription.COMPONENT_PREFIX + "000000/lock")
                .accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);
        assertEquals(Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }

    @Test
    public void testSetItemLockNonExistentItem() throws Exception {
        final ClientResponse result = getAuthenticatedResource("/items/" + ComponentDescription.COMPONENT_PREFIX + "000000/lock")
                .accept(MediaType.APPLICATION_XML)
                .put(ClientResponse.class);
        assertEquals(Status.CONFLICT.getStatusCode(), result.getStatus());
    }

    @Test
    public void testRemoveItemLockNonExistentItem() throws Exception {
        final ClientResponse result = getAuthenticatedResource("/items/" + ComponentDescription.COMPONENT_PREFIX + "000000/lock")
                .accept(MediaType.APPLICATION_XML)
                .delete(ClientResponse.class);
        assertEquals(Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }

    @Test
    public void testSetItemLockProductionItem() throws Exception {
        final String componentId = RegistryTestHelper.addComponent(baseRegistry, "component3", true, ComponentStatus.PRODUCTION).getId();
        final ClientResponse result = getAuthenticatedResource("/items/" + componentId + "/lock")
                .accept(MediaType.APPLICATION_XML)
                .put(ClientResponse.class);
        assertEquals(Status.FORBIDDEN.getStatusCode(), result.getStatus());
        assertNull(itemLockService.getLock(componentId));
    }

    @Test
    public void testSetItemLockDeprecatedItem() throws Exception {
        final String componentId = RegistryTestHelper.addComponent(baseRegistry, "component3", true, ComponentStatus.DEPRECATED).getId();
        final ClientResponse result = getAuthenticatedResource("/items/" + componentId + "/lock")
                .accept(MediaType.APPLICATION_XML)
                .put(ClientResponse.class);
        assertEquals(Status.FORBIDDEN.getStatusCode(), result.getStatus());
    }
}
