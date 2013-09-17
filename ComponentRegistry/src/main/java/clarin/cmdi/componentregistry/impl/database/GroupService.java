package clarin.cmdi.componentregistry.impl.database;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import clarin.cmdi.componentregistry.UserCredentials;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.Group;
import clarin.cmdi.componentregistry.model.GroupMembership;
import clarin.cmdi.componentregistry.model.Ownership;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;

/**
 * Service for handling groups and component/profile ownership
 * @author george.georgovassilis@mpi.nl
 *
 */
@Transactional
public interface GroupService {

    /**
     * Creates a new group. Will fail with an exception if the (normalised) group name already exists
     * @param name
     * @param owner
     * @return ID of group created
     * @throws ValidationException
     */
    long createNewGroup(String name, String ownerPrincipalName);
    
    /**
     * Gets groups directly owned by a user
     * @param ownerPrincipalName
     * @return
     */
    List<Group> getGroupsOwnedByUser(String ownerPrincipalName);
    
    /**
     * Get list of groups of which the provided user is a member of. Does not include
     * groups he owns but is not a member of.
     * @param principal
     * @return
     */
    List<Group> getGroupsOfWhichUserIsAMember(String principal);
    
    /**
     * Lists all group names
     * @return
     */
    List<String> listGroupNames();

    /**
     * Determines whether a user is the direct owner of a group
     * @param groupId
     * @param user
     * @return
     */
    boolean isUserOwnerOfGroup(long groupId, RegistryUser user);
    
    /**
     * Add an ownership of a user or group to a profile or component. Will check ownership for plausibility and will fail if that ownership already exists.
     * Will not fail if user/group/component/profile IDs don't correspond to an existing entry.
     * @param ownership
     */
    void addOwnership(Ownership ownership);
    
    /**
     * Removes an existing ownership. Won't complain if the ownership doesn't exist
     * @param ownership
     */
    void removeOwnership(Ownership ownership);
    
    /**
     * Determines whether a user has read access to a profile. Factors that allow access are:
     * 1. The profile is public
     * 2. The user is the creator
     * 3. The user has an ownership (see {@link #addOwnership(Ownership)})
     * 4. The user belongs to a group that has ownership
     * @param user
     * @param profile
     * @return
     */
    boolean canUserAccessProfileEitherOnHisOwnOrThroughGroupMembership(RegistryUser user, ProfileDescription profile);
    /**
     * Determines whether a user has read access to a component. Factors that allow access are:
     * 1. The component is public
     * 2. The user is the creator
     * 3. The user has an ownership (see {@link #addOwnership(Ownership)})
     * 4. The user belongs to a group that has ownership
     * @param user
     * @param component
     * @return
     */
    boolean canUserAccessComponentEitherOnHisOwnOrThroughGroupMembership(RegistryUser user, ComponentDescription component);

    /**
     * Make a user a mamber of a group
     * @param userName
     * @param groupName
     * @return database ID of group membership row
     */
    long makeMember(String userName, String groupName);
    
    /**
     * Move ownership of a component from a user to a group
     * @param principal
     * @param groupName
     * @param componentId
     */
    void transferComponentOwnershipFromUserToGroup(String principal, String groupName, String componentId);

    /**
     * Move ownership of a profile from a user to a group
     * @param principal
     * @param groupName
     * @param profileId
     */
    void transferProfileOwnershipFromUserToGroup(String principal, String groupName, String profileId);
}