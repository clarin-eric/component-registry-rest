package clarin.cmdi.componentregistry.impl.database;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import clarin.cmdi.componentregistry.BaseUnitTest;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.Group;
import clarin.cmdi.componentregistry.model.Ownership;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import clarin.cmdi.componentregistry.persistence.jpa.GroupDao;
import clarin.cmdi.componentregistry.persistence.jpa.OwnershipDao;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;
import clarin.cmdi.componentregistry.rest.DummyPrincipal;
import static org.junit.Assert.*;
import clarin.cmdi.componentregistry.GroupService;

public class GroupServiceImplTest extends BaseUnitTest {

    @Autowired
    GroupService groupService;
    @Autowired
    UserDao userDao;

    @Autowired
    GroupDao groupDao;

    @Autowired
    OwnershipDao ownershipDao;

    @Autowired
    ComponentDao componentDescriptionDao;

    RegistryUser user;
    RegistryUser user2;
    RegistryUser user3;

    final static String USER_NAME_1 = DummyPrincipal.DUMMY_CREDENTIALS.getPrincipalName() + "1";
    final static String USER_NAME_2 = DummyPrincipal.DUMMY_CREDENTIALS.getPrincipalName() + "2";
    final static String USER_NAME_3 = DummyPrincipal.DUMMY_CREDENTIALS.getPrincipalName() + "3";

    protected BaseDescription makeTestProfile(String sid, boolean isPublic, long ownerId) {
        ProfileDescription profile = new ProfileDescription();
        profile.setDescription("some description");
        profile.setId(ProfileDescription.PROFILE_PREFIX + sid);
        profile.setName("profilename");
        profile.setCreatorName(DummyPrincipal.DUMMY_PRINCIPAL.getName());
        Number id = componentDescriptionDao.insertDescription(profile, "someContent",
                isPublic, ownerId);
        return componentDescriptionDao.getById(id);
    }

    protected BaseDescription makeTestComponent(boolean isPublic,
            long ownerId) {
        BaseDescription componentDescription = new BaseDescription();
        componentDescription.setDescription("some description");
        componentDescription.setId(ComponentDescription.COMPONENT_PREFIX + "4567");
        componentDescription.setName("componentname");
        componentDescription.setCreatorName(DummyPrincipal.DUMMY_PRINCIPAL.getName());
        Number id = componentDescriptionDao.insertDescription(
                componentDescription, "someContent", isPublic, ownerId);
        return componentDescriptionDao.getById(id);
    }

    @Before
    public void setupThisTest() {
        user = new RegistryUser();
        user.setName("Test User");
        user.setPrincipalName(USER_NAME_1);
        user = userDao.saveAndFlush(user);
        user = userDao.getByPrincipalName(user.getPrincipalName());

        user2 = new RegistryUser();
        user2.setName("Test User 2");
        user2.setPrincipalName(USER_NAME_2);
        user2 = userDao.saveAndFlush(user2);
        user2 = userDao.getByPrincipalName(user2.getPrincipalName());

        user3 = new RegistryUser();
        user3.setName("Test User 3");
        user3.setPrincipalName(USER_NAME_3);
        user3 = userDao.saveAndFlush(user3);
        user3 = userDao.getByPrincipalName(user3.getPrincipalName());
    }

    
    @Test
    public void testAddMember() throws ItemNotFoundException {
        final String groupName = "group";
        final Group group = groupDao.findById(groupService.createNewGroup(groupName, user.getPrincipalName())).orElseThrow();

        //add user
        groupService.makeMember(USER_NAME_1, groupName);
        assertTrue(groupService.userGroupMember(USER_NAME_1, group.getId()));
        
        //add another user
        groupService.makeMember(USER_NAME_2, groupName);
        assertTrue(groupService.userGroupMember(USER_NAME_1, group.getId()));
        assertTrue(groupService.userGroupMember(USER_NAME_2, group.getId()));
        
        //add yet another user
        groupService.makeMember(USER_NAME_3, groupName);
        assertTrue(groupService.userGroupMember(USER_NAME_1, group.getId()));
        assertTrue(groupService.userGroupMember(USER_NAME_2, group.getId()));
        assertTrue(groupService.userGroupMember(USER_NAME_3, group.getId()));
    }
    
    @Test
    public void testRemoveMember() throws ItemNotFoundException {
        final String groupName = "group";
        final Group group = groupDao.findById(groupService.createNewGroup(groupName, user.getPrincipalName())).orElseThrow();

        //add three users
        groupService.makeMember(USER_NAME_1, groupName);       
        groupService.makeMember(USER_NAME_2, groupName);
        groupService.makeMember(USER_NAME_3, groupName);

        //remove user 1
        groupService.removeMember(USER_NAME_1, groupName);
        assertFalse(groupService.userGroupMember(USER_NAME_1, group.getId()));
        assertTrue(groupService.userGroupMember(USER_NAME_2, group.getId()));
        assertTrue(groupService.userGroupMember(USER_NAME_3, group.getId()));
        
        //alse remove user 2
        groupService.removeMember(USER_NAME_2, groupName);
        assertFalse(groupService.userGroupMember(USER_NAME_1, group.getId()));
        assertFalse(groupService.userGroupMember(USER_NAME_2, group.getId()));
        assertTrue(groupService.userGroupMember(USER_NAME_3, group.getId()));
    }

    @Test
    public void testCreateNewGroup_getGroupsOwnedByUser() {
        // Expect no groups for the current user
        List<Group> groups = groupService.getGroupsOwnedByUser(user
                .getPrincipalName());
        assertTrue(groups.isEmpty());

        // Make a group
        Group group1 = groupDao.findById(groupService.createNewGroup("Group 1",
                user.getPrincipalName())).orElseThrow();
        // and another one
        Group group2 = groupDao.findById(groupService.createNewGroup("Group 2",
                user.getPrincipalName())).orElseThrow();
        groups = groupService.getGroupsOwnedByUser(user.getPrincipalName());
        assertEquals(2, groups.size());

        assertTrue(groups.contains(group1));
        assertTrue(groups.contains(group2));
    }

    @Test(expected = ValidationException.class)
    public void testCreateDuplicateGroup() {
        // Make a group
        groupService.createNewGroup("Group X", user.getPrincipalName());
        // Make same group, again
        groupService.createNewGroup("Group X", user.getPrincipalName());
    }

    @Test
    public void testAccessToNonOwnedPrivateProfile() {
        // Make a private profile that belongs to someone else
        BaseDescription profile = makeTestProfile("4567", false, 9999);
        // Expect that user can't access profile
        boolean result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user, profile);
        assertFalse(result);
    }

    @Test
    public void testAccessToNonOwnedPublicProfile() {
        // Make a public profile that belongs to someone else
        BaseDescription profile = makeTestProfile("4567", true, 9999);

        // Expect that user can access the profile
        boolean result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user, profile);
        assertTrue(result);
    }

    @Test
    public void testAccessToOwnedPrivateProfile() {
        // Make a profile that belongs to someone else
        BaseDescription profile = makeTestProfile("4567", false, user.getId()
                .longValue());
        // Add an ownership to that profile to the current user
        Ownership ownership = new Ownership();
        ownership.setComponentRef(profile.getId());
        ownership.setUserId(user.getId().longValue());
        groupService.addOwnership(ownership);

        // Expect that user can access the profile
        boolean result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user, profile);
        assertTrue(result);
    }

    @Test
    public void testAccessViaGroupToPrivateProfile() throws ItemNotFoundException {
        // Make a profile that belongs to someone else
        BaseDescription profile = makeTestProfile("4567", false, 9999);

        // Make a group that belongs to someone else
        Group group = groupDao.findById(groupService.createNewGroup("Group 1",
                user2.getPrincipalName())).orElseThrow();

        groupService.makeMember(user.getPrincipalName(),
                group.getName());
        // Make me a member of that group

        // Add an ownership to that profile to the group
        Ownership ownership = new Ownership();
        ownership.setComponentRef(profile.getId());
        ownership.setGroupId(group.getId());
        groupService.addOwnership(ownership);

        // Expect that user can access the profile
        boolean result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user, profile);
        assertTrue(result);
    }

    @Test
    public void testAccessToNonOwnedPrivateComponent() {
        // Make a private profile that belongs to someone else
        BaseDescription componentDescription = makeTestComponent(false,
                9999);

        // Expect that user can't access profile
        boolean result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user, componentDescription);
        assertFalse(result);
    }

    @Test
    public void testAccessToNonOwnedPublicComponent() {
        // Make a public profile that belongs to someone else
        BaseDescription componentDescription = makeTestComponent(true,
                9999);

        // Expect that user can access the profile
        boolean result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user, componentDescription);
        assertTrue(result);
    }

    @Test
    public void testAccessToOwnedPrivateComponent() {
        // Make a profile that belongs to someone else
        BaseDescription componentDescription = makeTestComponent(false,
                9999);

        // Add an ownership to that profile to the current user
        Ownership ownership = new Ownership();
        ownership.setComponentRef(componentDescription.getId());
        ownership.setUserId(user.getId().longValue());
        groupService.addOwnership(ownership);

        // Expect that user can access the profile
        boolean result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user, componentDescription);
        assertTrue(result);
    }

    @Test
    public void testAccessViaGroupToPrivateComponent() throws ItemNotFoundException {
        // Make a profile that belongs to someone else
        BaseDescription componentDescription = makeTestComponent(false,
                user3.getId().longValue());

        // Make a group that belongs to someone else
        Group group = groupDao.findById(groupService.createNewGroup("Group 1",
                user2.getPrincipalName())).orElseThrow();

        // Make me a member of that group
        groupService.makeMember(user.getPrincipalName(),
                group.getName());

        // Add an ownership to that component to the group
        Ownership ownership = new Ownership();
        ownership.setComponentRef(componentDescription.getId());
        ownership.setGroupId(group.getId());
        groupService.addOwnership(ownership);

        // Expect that user can access the profile
        boolean result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user, componentDescription);
        assertTrue(result);
    }

    @Test
    public void testTransferComponentOwnershipFromUserToGroup() throws UserUnauthorizedException, ItemNotFoundException {

        // Make a group
        Group group = groupDao.findById(groupService.createNewGroup("Group 1",
                user.getPrincipalName())).orElseThrow();

        // Make user and user2 members of the same group
        groupService.makeMember(user.getPrincipalName(), group.getName());

        assertTrue(groupService.getGroupsOfWhichUserIsAMember(user.getPrincipalName()).contains(group));
        groupService.makeMember(user2.getPrincipalName(), group.getName());
        assertTrue(groupService.getGroupsOfWhichUserIsAMember(user2.getPrincipalName()).contains(group));

        // Make a component that belongs to user
        BaseDescription componentDescription = makeTestComponent(false,
                user.getId().longValue());

        // Just for the fun of it: check that user can access the component
        // while user2 can't
        boolean result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user, componentDescription);
        assertTrue(result);
        result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user2, componentDescription);
        assertFalse(result);

        // user transfers ownership of the component to his group
        groupService.transferItemOwnershipToGroup(
                user.getPrincipalName(), group.getName(), componentDescription.getId());

        List<Ownership> ownerships = (List<Ownership>) ownershipDao.findAll();

        // Check that user and user2 have access to the component...
        result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user, componentDescription);
        assertTrue(result);
        result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user2, componentDescription);
        assertTrue(result);

        // ... but someone unreleated not
        result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user3, componentDescription);
        assertFalse(result);
    }

    @Test
    public void testtransferComponentOwnershipFromUserToGroup() throws UserUnauthorizedException, ItemNotFoundException {

        // Make a group
        Group group = groupDao.findById(groupService.createNewGroup("Group 1",
                user.getPrincipalName())).orElseThrow();

        // Make user and user2 members of the same group
        groupService.makeMember(user.getPrincipalName(), group.getName());

        assertTrue(groupService.getGroupsOfWhichUserIsAMember(user.getPrincipalName()).contains(group));
        groupService.makeMember(user2.getPrincipalName(), group.getName());
        assertTrue(groupService.getGroupsOfWhichUserIsAMember(user2.getPrincipalName()).contains(group));

        // Make a profile that belongs to user
        BaseDescription profileDescription = makeTestProfile("4567", false,
                user.getId().longValue());

        // Just for the fun of it: check that user can access the profile
        // while user2 can't
        boolean result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user, profileDescription);
        assertTrue(result);
        result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user2, profileDescription);
        assertFalse(result);

        // user transfers ownership of the component to his group
        groupService.transferItemOwnershipToGroup(
                user.getPrincipalName(), group.getName(),
                profileDescription.getId());

        List<Ownership> ownerships = (List<Ownership>) ownershipDao.findAll();

        // Check that user and user2 have access to the component...
        result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user, profileDescription);
        assertTrue(result);
        result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user2, profileDescription);
        assertTrue(result);

        // ... but someone unreleated not
        result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user3, profileDescription);
        assertFalse(result);
    }

    @Test
    public void testTransferItemOwnershipFromUserToGroup() throws UserUnauthorizedException, ItemNotFoundException {
        //TODO: improve test by mixing in components and exclusing components/profiles
        // Make a group
        Group group = groupDao.findById(groupService.createNewGroup("Group 1",
                user.getPrincipalName())).orElseThrow();

        // Make user and user2 members of the same group
        groupService.makeMember(user.getPrincipalName(), group.getName());

        assertTrue(groupService.getGroupsOfWhichUserIsAMember(user.getPrincipalName()).contains(group));
        groupService.makeMember(user2.getPrincipalName(), group.getName());
        assertTrue(groupService.getGroupsOfWhichUserIsAMember(user2.getPrincipalName()).contains(group));

        // Make a profile that belongs to user
        BaseDescription profileDescription = makeTestProfile("4567", false,
                user.getId().longValue());

        // Just for the fun of it: check that user can access the profile
        // while user2 can't
        boolean result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user, profileDescription);
        assertTrue(result);
        result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user2, profileDescription);
        assertFalse(result);

        // user transfers ownership of the component to his group
        groupService.transferItemOwnershipToGroup(
                user.getPrincipalName(), group.getName(),
                profileDescription.getId());

        List<Ownership> ownerships = (List<Ownership>) ownershipDao.findAll();

        // Check that user and user2 have access to the component...
        result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user, profileDescription);
        assertTrue(result);
        result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user2, profileDescription);
        assertTrue(result);

        // ... but someone unreleated not
        result = groupService
                .canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(
                        user3, profileDescription);
        assertFalse(result);
    }

    @Test
    public void testGetGroupsTheItemIsAMemberOf() throws UserUnauthorizedException, ItemNotFoundException {
        // Make a group
        Group group1 = groupDao.findById(groupService.createNewGroup("Group 1",
                user.getPrincipalName())).orElseThrow();

        // And another one which the item is not assigned to. will use to test exclusion
        Group group2 = groupDao.findById(groupService.createNewGroup("Group 2",
                user.getPrincipalName())).orElseThrow();

        // Make user members of the same group
        groupService.makeMember(user.getPrincipalName(), group1.getName());
        // Make user members of the same group
        groupService.makeMember(user.getPrincipalName(), group2.getName());

        // Make a profile that belongs to user
        BaseDescription profileDescription = makeTestProfile("4567", false,
                user.getId().longValue());

        // Make a profile that belongs to user, but that one will belong to group2
        BaseDescription profileDescription2 = makeTestProfile("666", false,
                user.getId().longValue());

        // user transfers ownership of the profile to his group
        groupService.transferItemOwnershipToGroup(
                user.getPrincipalName(), group1.getName(),
                profileDescription.getId());

        // user transfers ownership of the profile to his group
        groupService.transferItemOwnershipToGroup(
                user.getPrincipalName(), group2.getName(),
                profileDescription2.getId());

        List<Group> groups = groupService.getGroupsTheItemIsAMemberOf(profileDescription.getId());
        assertEquals(1, groups.size());
        Group g = groups.get(0);
        assertEquals(group1.getId(), g.getId());
    }
}
