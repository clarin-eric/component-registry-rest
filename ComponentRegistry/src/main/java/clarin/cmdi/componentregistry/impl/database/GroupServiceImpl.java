package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.GroupService;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import clarin.cmdi.componentregistry.impl.ComponentUtils;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.Group;
import clarin.cmdi.componentregistry.model.GroupMembership;
import clarin.cmdi.componentregistry.model.Ownership;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import clarin.cmdi.componentregistry.persistence.jpa.GroupDao;
import clarin.cmdi.componentregistry.persistence.jpa.GroupMembershipDao;
import clarin.cmdi.componentregistry.persistence.jpa.OwnershipDao;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.dao.DataAccessException;

/**
 * Service that manages groups, memberships and ownerships. It exposes some
 * functions over JMX, that's why some methods use human-friendly names (user
 * principal names, group names) rather than ID arguments.
 *
 * @author george.georgovassilis@mpi.nl
 *
 */
@ManagedResource(objectName = "componentregistry:name=GroupService", description = "Operations for managing groups")
@Service("GroupService")
@Transactional
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupDao groupDao;
    @Autowired
    private GroupMembershipDao groupMembershipDao;
    @Autowired
    private OwnershipDao ownershipDao;
    @Autowired
    private ComponentDao componentDao;
    @Autowired
    private UserDao userDao;

    public void setGroupDao(GroupDao groupDao) {
        this.groupDao = groupDao;
    }

    public void setGroupMembershipDao(GroupMembershipDao groupMembershipDao) {
        this.groupMembershipDao = groupMembershipDao;
    }

    public void setOwnershipDao(OwnershipDao ownershipDao) {
        this.ownershipDao = ownershipDao;
    }

    @Override
    public List<Group> getGroupsOwnedByUser(String ownerPrincipalName) {
        RegistryUser owner = userDao.getByPrincipalName(ownerPrincipalName);
        return groupDao.findGroupOwnedByUser(owner.getId().longValue());
    }

    @Override
    public boolean isUserOwnerOfGroup(String groupName, String ownerPrincipalName) {
        List<Group> groups = getGroupsOwnedByUser(ownerPrincipalName);
        for (Group group : groups) {
            if (group.getName().equals(groupName)) {
                return true;
            }
        }

        return false;
    }

    private void checkOwnership(Ownership ownership) {
        if (ownership.getComponentRef() == null) {
            throw new RuntimeException("Ownership needs a componentId");
        }
        if (ownership.getUserId() == 0 && ownership.getGroupId() == 0) {
            throw new RuntimeException("Ownership needs a groupId or userId");
        }
        if (ownership.getUserId() != 0 && ownership.getGroupId() != 0) {
            throw new RuntimeException("Ownership has both a groupId and a userId ");
        }
    }

    private void assertOwnershipDoesNotExist(Ownership ownership) {
        Ownership o = ownershipDao.findOwnershipByGroupAndComponent(ownership.getGroupId(), ownership.getComponentRef());
        if (o != null) {
            throw new ValidationException("Ownership exists");
        }
    }

    @Override
    public void addOwnership(Ownership ownership) {
        checkOwnership(ownership);
        assertOwnershipDoesNotExist(ownership);
        ownershipDao.save(ownership);
    }

    @Override
    public void removeOwnership(Ownership ownership) {
        throw new RuntimeException("not implemented");
    }

    protected boolean canUserAccessAbstractDescriptionEitherOnHisOwnOrThroughGroupMembership(RegistryUser user,
            BaseDescription description) {
        // TODO make some joins and multi-id queries to speed the entire method
        // up
        boolean isProfile = (description instanceof ProfileDescription);
        long userId = user.getId().longValue();
        // anyone can access public profile
        if (componentDao.isPublic(description.getId())) {
            return true;
        }
        // the creator can also access any profile
        if (description.getUserId().equals(user.getId() + "")) {
            return true;
        }

        // a co-ownership on the profile also allows access
        Ownership ownership = ownershipDao.findOwnershipByUserAndComponent(userId, description.getId());
        if (ownership != null) {
            return true;
        }

        // get a list of groups the user owns and is a member of
        List<Group> groups = groupDao.findGroupOwnedByUser(userId);
        Set<Long> groupIds = new HashSet<Long>();
        for (Group group : groups) {
            groupIds.add(group.getId());
        }

        List<GroupMembership> memberships = groupMembershipDao.findGroupsTheUserIsAmemberOf(userId);
        for (GroupMembership gm : memberships) {
            groupIds.add(gm.getGroupId());
        }

        for (Long groupId : groupIds) {
            ownership = ownershipDao.findOwnershipByGroupAndComponent(groupId, description.getId());
            if (ownership != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(RegistryUser user,
            BaseDescription baseDescription) {
        return canUserAccessAbstractDescriptionEitherOnHisOwnOrThroughGroupMembership(user, baseDescription);
    }

    @Override
    @ManagedOperation(description = "Make a user member of a group")
    @ManagedOperationParameters({
        @ManagedOperationParameter(name = "principalName", description = "Principal name of the user to make a member"),
        @ManagedOperationParameter(name = "groupName", description = "Name of the group")})
    public long makeMember(String userPrincipalName, String groupName) throws ItemNotFoundException {

        RegistryUser user = userDao.getByPrincipalName(userPrincipalName);

        if (user == null) {
            throw new ItemNotFoundException("User with the principal name " + userPrincipalName + " is not found.");
        }
        Group group = groupDao.findGroupByName(groupName);

        if (group == null) {
            throw new ItemNotFoundException("Group with the  name " + groupName + " is not found.");
        }

        GroupMembership gm = groupMembershipDao.findMembership(user.getId().longValue(), group.getId());
        if (gm != null) {
            return gm.getId();
        }
        gm = new GroupMembership();
        gm.setGroupId(group.getId());
        gm.setUserId(user.getId().longValue());
        return groupMembershipDao.save(gm).getId();
    }

//    @Override
//    @ManagedOperation(description = "Remove user member from  a group")
//    @ManagedOperationParameters({
//        @ManagedOperationParameter(name = "principalName", description = "Principal name of the user to make a member"),
//        @ManagedOperationParameter(name = "groupName", description = "Name of the group")})
//    public long removeMember(String userPrincipalName, String groupName) throws ItemNotFoundException{
//        
//        RegistryUser user = userDao.getByPrincipalName(userPrincipalName);
//        
//        if (user == null) {
//            throw new ItemNotFoundException("User the the principal name "+userPrincipalName+" is not found.");
//        }
//        Group group = groupDao.findGroupByName(groupName);
//        
//        if (group == null) {
//            throw new ItemNotFoundException("Group with the  name "+groupName+" is not found.");
//        }
//        
//       
//        return groupMembershipDao.deleteMembership(user.getId(), group.getId());
//    }
    @ManagedOperation(description = "Create a new group")
    @ManagedOperationParameters({
        @ManagedOperationParameter(name = "name", description = "Name of the group, must be unique"),
        @ManagedOperationParameter(name = "ownerPrincipalName", description = "Principal name of the user")})
    @Override
    public long createNewGroup(String name, String ownerPrincipalName) {
        RegistryUser owner = userDao.getByPrincipalName(ownerPrincipalName);
        if (owner == null) {
            throw new ValidationException("No principal '" + ownerPrincipalName + "' found");
        }
        Group group = groupDao.findGroupByName(name);
        if (group != null) {
            throw new ValidationException("Group '" + name + "' already exists");
        }
        group = new Group();
        group.setName(name);
        group.setOwnerId(owner.getId().longValue());
        group = groupDao.save(group);
        return group.getId();
    }

    @ManagedOperation(description = "List available groups")
    @Override
    public List<String> listGroupNames() {
        List<String> groupNames = new ArrayList<String>();
        for (Group group : groupDao.findAll()) {
            groupNames.add(group.getName());
        }
        return groupNames;
    }

    @Override
    public List<Group> getGroupsOfWhichUserIsAMember(String principal) {
        RegistryUser user = userDao.getByPrincipalName(principal);
        if (user == null || user.getId() == null) {
            return new ArrayList<Group>();
        }
        List<GroupMembership> memberships = groupMembershipDao.findGroupsTheUserIsAmemberOf(user.getId().longValue());
        List<Group> groups = new ArrayList<Group>();
        for (GroupMembership m : memberships) {
            groups.add(groupDao.findOne(m.getGroupId()));
        }
        return groups;
    }

    @Override
    public List<String> getComponentIdsInGroup(long groupId) {
        List<Ownership> ownerships = ownershipDao.findOwnershipByGroup(groupId);
        Set<String> componentIds = new HashSet<String>();
        for (Ownership o : ownerships) {
            if (ComponentUtils.isComponentId(o.getComponentRef())) {
                componentIds.add(o.getComponentRef());
            }
        }
        List<String> idsList = new ArrayList<String>(componentIds);
        Collections.sort(idsList);
        return idsList;
    }

    @Override
    public List<String> getProfileIdsInGroup(long groupId) {
        List<Ownership> ownerships = ownershipDao.findOwnershipByGroup(groupId);
        Set<String> profileIds = new HashSet<String>();
        for (Ownership o : ownerships) {
            if (ComponentUtils.isProfileId(o.getComponentRef())) {
                profileIds.add(o.getComponentRef());
            }
        }
        List<String> idsList = new ArrayList<String>(profileIds);
        Collections.sort(idsList);
        return idsList;
    }

    @Override
    public List<Group> getGroupsTheItemIsAMemberOf(String itemId) {
        Set<Ownership> ownerships = new HashSet<Ownership>();
        ownerships.addAll(ownershipDao.findOwnershipByComponentId(itemId));
        Set<Group> groups = new HashSet<Group>();
        for (Ownership ownership : ownerships) {
            groups.add(groupDao.findOne(ownership.getGroupId()));
        }
        List<Group> groupList = new ArrayList<Group>(groups);
        Collections.sort(groupList, new Comparator<Group>() {
            @Override
            public int compare(Group g1, Group g2) {
                return (int) (g1.getId() - g2.getId());
            }
        });
        return groupList;
    }

    @ManagedOperation(description = "Make a component owned by a group instead of a user")
    @ManagedOperationParameters({
        @ManagedOperationParameter(name = "principal", description = "Name of the principal who owns the component"),
        @ManagedOperationParameter(name = "groupName", description = "Name of the group to move the component to"),
        @ManagedOperationParameter(name = "componentId", description = "Id of component")})
    @Override
    public void transferItemOwnershipToGroup(String principal, String groupName, String itemId) throws UserUnauthorizedException {
        final BaseDescription item = componentDao.getByCmdId(itemId);
        if (item == null) {
            throw new ValidationException("No profile or component found with ID " + itemId);
        }
        final Group target = groupDao.findGroupByName(groupName);
        if (target == null) {
            throw new ValidationException("No group found with name " + groupName);
        }

        if (!this.userGroupMember(principal, target.getId())) {
            throw new UserUnauthorizedException("User " + principal + " is not a member of group " + groupName);
        }

        final List<Ownership> currentOwnerships = ownershipDao.findOwnershipByComponentId(itemId);
        if (!userHasRightToMove(item, principal, currentOwnerships)) {
            throw new UserUnauthorizedException("User " + principal + " does not have the rights to move item  " + item.getName());
        }

        ownershipDao.delete(currentOwnerships);
        final Ownership ownership = new Ownership();
        ownership.setComponentRef(itemId);
        ownership.setGroupId(target.getId());
        addOwnership(ownership);
    }

    private boolean userHasRightToMove(BaseDescription item, String principal, List<Ownership> currentOwnerships) throws DataAccessException {
        long itemOwnerUser = item.getDbUserId();
        long principalId = userDao.getByPrincipalName(principal).getId();
        if (itemOwnerUser == principalId) {
            return true;
        } else {
            // check if item and user are in the same group
            for (Ownership ownership : currentOwnerships) {
                if (userGroupMember(principal, ownership.getGroupId())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void transferItemOwnershipFromUserToGroupId(String principal, long groupId, String componentId) throws UserUnauthorizedException {
        Group group = groupDao.findOne(groupId);
        if (group == null) {
            throw new ValidationException("No group found with id " + groupId);
        }
        this.transferItemOwnershipToGroup(principal, group.getName(), componentId);
    }

    @Override
    public boolean userGroupMember(String principalName, long groupId) {
        RegistryUser user = userDao.getByPrincipalName(principalName);
        GroupMembership gm = groupMembershipDao.findMembership(user.getId(), groupId);
        return gm != null;
    }

    @Override
    public Number getGroupIdByName(String groupName) throws ItemNotFoundException {
        Group group = groupDao.findGroupByName(groupName);
        if (group != null) {
            return group.getId();
        } else {
            throw new ItemNotFoundException("No group with the name " + groupName);
        }
    }

    @Override
    public String getGroupNameById(long groupId) throws ItemNotFoundException {
        Group group = groupDao.findOne(groupId);
        if (group != null) {
            return group.getName();
        } else {
            throw new ItemNotFoundException("No group with the id " + groupId);
        }
    }

    @Override
    public List<RegistryUser> getUsersInGroup(long groupId) {
        final List<GroupMembership> groupMemberships = groupMembershipDao.findForGroup(groupId);
        return Lists.transform(groupMemberships, new Function<GroupMembership, RegistryUser>() {

            @Override
            public RegistryUser apply(GroupMembership f) {
                return userDao.getPrincipalNameById(f.getUserId());
            }
        });
    }
}
