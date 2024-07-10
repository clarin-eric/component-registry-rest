package clarin.cmdi.componentregistry.persistence;

import java.util.List;

import org.springframework.dao.DataAccessException;

import clarin.cmdi.componentregistry.BaseDescription;
import clarin.cmdi.componentregistry.model.ComponentStatus;
import clarin.cmdi.componentregistry.model.RegistryUser;
import java.util.Collection;

/**
 * Interface for a persistence operation on components and profiles
 * 
 * @author george.georgovassilis@mpi.nl
 * 
 * @param <T>
 */
public interface ComponentDao {

    
    /**
     * Get all public profiles
     * 
     * @return
     */
    List<BaseDescription> getPublicBaseDescriptions(String prefix, Collection<ComponentStatus> statusFilter);

    /**
     * 
     * @param cmdId
     *            CMD id
     * @return Whether the specified item is in the public space
     */
    boolean isPublic(String cmdId);

    /**
     * 
     * @param cmdId
     *            CMD id
     * @param userId
     *            User db id of workspace owner
     * @return Whether the specified item is in the specified user's workspace
     */
    boolean isInUserSpace(String cmdId, Number userId);

    /**
     * 
     * @param cmdId
     *            CMD id
     * @param userId
     *            User db id of workspace owner, null for public registry
     * @return Whether the specified item is in the specified workspace (user or
     *         public)
     */
    //boolean isAccessible(String cmdId, Number userId);

    /**
     * 
     * @param cmdId
     *            Profile or component Id (not primary key)
     * @return String value of XML content for profile or component
     */
    String getContent(boolean isDeleted, String cmdId)
	    throws DataAccessException;

    /**
     * @param description
     *            Description to insert
     * @param content
     *            Content to insert and refer to from description
     * @return Id of newly inserted description
     */
    Number insertDescription(BaseDescription description, String content,
	    boolean isPublic, Number userId) throws DataAccessException;

    /**
     * Updates a description by database id
     * 
     * @param id
     *            Id (key) of description record
     * @param description
     *            New values for description (leave null to not change)
     * @param content
     *            New content for description (leave null to not change)
     */
    void updateDescription(Number id, BaseDescription description, String content);

    /**
     * Retrieves description by it's primary key Id
     * 
     * @param id
     *            Description key
     * @return The description, if it exists; null otherwise
     */
    BaseDescription getById(Number id) throws DataAccessException;

    BaseDescription getDeletedById(Number id) throws DataAccessException;

    /**
     * Get by ComponentId / ProfileId, whether in userspace or public
     * 
     * @param id
     *            Full component id
     * @return The description, if it exists; null otherwise
     */
    BaseDescription getByCmdId(String id) throws DataAccessException;

    /**
     * Get by ComponentId / ProfileId
     * 
     * @param id
     *            Full component id
     * @param userId
     *            Db id of user for workspace; null for public space
     * @return The description, if it exists; null otherwise
     */
//    BaseDescription getByCmdId(String id, Number userId)
//	    throws DataAccessException;
 

    /**
     * 
     * @param cmdId
     *            CMD Id of description
     * @return Database id for description record
     */
    Number getDbId(String cmdId);

    /**
     * 
     * @return All descriptions in the public space
     */
    List<BaseDescription> getPublicDescriptions() throws DataAccessException;

    /**
     * @return List of deleted descriptions in user space or in public when
     *         userId=null
     * @param userId
     */
    List<BaseDescription> getDeletedDescriptions(Number userId);
    List<BaseDescription> getDeletedPublicDescriptions();
    List<BaseDescription> getDeletedTeamDescriptions(Number teamId);

    /**
     * 
     * @return All the user's components not in the public space and are also
     *         not part of any group
     */
    List<BaseDescription> getPrivateBaseDescriptions(Number userId, String prefix, Collection<ComponentStatus> statusFilter)
	    throws DataAccessException;
  
    void setDeleted(BaseDescription desc, boolean isDeleted)
	    throws DataAccessException;

    void setPublished(Number id, boolean published);

    /**
     * 
     * @param id
     *            Id of description record
     * @return Principal name of description's owner, if any. Otherwise, null.
     */
    String getOwnerPrincipalName(Number id);
    
    /**
     * Get a list of ids ({@link BaseDescription#getId()}) of all non-deleted profiles
     * @param contentFilter filter on profiles that contain this string in their XML content (can be null for no filtering)
     * @return
     */
    List<String> getAllNonDeletedProfileIds(String contentFilter, Collection<ComponentStatus> statusFilter);

    /**
     * Get a list of ids ({@link BaseDescription#getId()}) of all non-deleted components
     * @param contentFilter filter on components that contain this string in their XML content (can be null for no filtering)
     * @return
     */
    List<String> getAllNonDeletedComponentIds(String contentFilter, Collection<ComponentStatus> statusFilter);

    public List<BaseDescription> getAllNonDeletedDescriptions();
            
    // Olha was here
    public List<String> getAllItemIdsInGroup(String prefix, Long groupId, Collection<ComponentStatus> statusFilter);

    public void setOwner(Long itemId, Long ownerId);

}