package clarin.cmdi.componentregistry.persistence.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import clarin.cmdi.componentregistry.impl.ComponentUtils;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import clarin.cmdi.componentregistry.persistence.jpa.CommentsDao;
import clarin.cmdi.componentregistry.persistence.jpa.JpaComponentDao;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;

/**
 * Base DAO which can be extended to serve {@link ComponentDescription}s and
 * {@link ProfileDescription}s
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author George.Georgovassilis@mpi.nl
 */

@Repository
public class ComponentDaoImpl implements ComponentDao {

    @Autowired
    private JpaComponentDao jpaComponentDao;

    @Autowired
    private CommentsDao commentsDao;

    @Autowired
    private UserDao userDao;

    private final static Logger LOG = LoggerFactory
	    .getLogger(ComponentDaoImpl.class);

    /**
     * Copy a set of properties which are allowed to change from "from" to "to"
     * thus making sure that only those properties are updated and not other
     * ones.
     * 
     * @param from
     * @param to
     */
    protected void copyPersistentProperties(BaseDescription from, BaseDescription to) {
	to.setName(from.getName());
	to.setDescription(from.getDescription());
	to.setRegistrationDate(from.getRegistrationDate());
	to.setCreatorName(from.getCreatorName());
	to.setDomainName(from.getDomainName());
	to.setGroupName(from.getGroupName());
	to.setHref(from.getHref());
    }

    protected String getTableName() {
	return "persistentcomponents";
    };

    private boolean compare(String s, Number n) {
	return ("" + s).equals("" + n);
    }

    private String toString(Number n) {
	if (n == null)
	    return null;
	return n.toString();
    }

    private BaseDescription castDown(BaseDescription c) {
	// JPA will save only BaseComponent instances, not derived classes
	if (c instanceof ComponentDescription
		|| c instanceof ProfileDescription) {
	    BaseDescription copy = new BaseDescription();
	    ComponentUtils.copyPropertiesFrom(c, copy);
	    return copy;
	}
	return c;
    }

    /**
     * Update {@link BaseDescription#getCommentsCount()} (which is a transient
     * property) with the number of comments for that component and load
     * {@link BaseDescription#getContent()}
     * 
     * @param baseDescription
     */
    private void augment(BaseDescription baseDescription) {
	if (baseDescription == null)
	    return;
	int count = (int) commentsDao.findCommentCountForComponent(baseDescription
		.getId());
	baseDescription.setCommentsCount(count);
	baseDescription.setContent(jpaComponentDao.findContentByComponentId(baseDescription
		.getId()));
    }

/**
     * Update {@link BaseDescription#getCommentsCount()
     * @param baseDescription
     */
    private List<BaseDescription> augment(List<BaseDescription> baseDescription) {
	if (baseDescription.isEmpty())
	    return baseDescription;
	Map<String, BaseDescription> map = new HashMap<String, BaseDescription>();
	List<String> idlist = new ArrayList<String>();
	for (BaseDescription c:baseDescription) {
	    idlist.add(c.getId());
	    map.put(c.getId(), c);
	}
	List<Object[]> tuples = commentsDao
		.findCommentCountForComponents(idlist);
	for (Object[] tuple : tuples) {
	    String id = tuple[0].toString();
	    int count = Integer.parseInt(tuple[1].toString());
	    map.get(id).setCommentsCount(count);
	}
	return baseDescription;
    }

    /**
     * 
     * @param cmdId
     *            CMD id
     * @return Whether the specified item is in the public space
     */
    @Override
    public boolean isPublic(String cmdId) {
	BaseDescription baseDescription = jpaComponentDao.findByComponentId(cmdId);
	return baseDescription != null && baseDescription.isPublic();
    }

    /**
     * 
     * @param cmdId
     *            CMD id
     * @param userId
     *            User db id of workspace owner
     * @return Whether the specified item is in the specified user's workspace
     */
    @Override
    public boolean isInUserSpace(String cmdId, Number userId) {
	BaseDescription baseDescription = jpaComponentDao.findByComponentId(cmdId);
	boolean b = baseDescription != null && !baseDescription.isPublic()
		&& compare(baseDescription.getUserId(), userId);
	return b;
    }

    /**
     * 
     * @param cmdId
     *            CMD id
     * @param userId
     *            User db id of workspace owner, null for public registry
     * @return Whether the specified item is in the specified workspace (user or
     *         public)
     */
    @Override
    public boolean isInRegistry(String cmdId, Number userId) {
	if (userId == null) {
	    return isPublic(cmdId);
	} else {
	    return isInUserSpace(cmdId, userId);
	}
    }

    /**
     * 
     * @param cmdId
     *            Profile or component Id (not primary key)
     * @return String value of XML content for profile or component
     */
    @Override
    public String getContent(boolean isDeleted, String cmdId) {
	return jpaComponentDao.findContentByComponentId(cmdId, isDeleted);
    }

    /**
     * @param description
     *            Description to insert
     * @param content
     *            Content to insert and refer to from description
     * @return Id of newly inserted description
     */
    @Override
    public Number insertDescription(BaseDescription description, String content,
	    boolean isPublic, Number userId) {

	if (description.getId() != null
		&& jpaComponentDao.findByComponentId(description.getId()) != null) {
	    throw new IllegalArgumentException("Component "
		    + description.getId() + " already in DB");
	}
	if (content == null)
	    content = "";
	BaseDescription copy = castDown(description);
	copy.setContent(content);
	copy.setPublic(isPublic);
	copy.setUserId(toString(userId));
	copy = jpaComponentDao.saveAndFlush(copy);
	jpaComponentDao.updateContent(copy.getId(), content);
	return copy.getDbId();
    }

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
    @Override
    public void updateDescription(Number id, BaseDescription description,
	    String content) {
	if (content == null)
	    content = "";
	if (description != null) {
	    // Update description
	    BaseDescription copy = jpaComponentDao.findByComponentId(description
		    .getId());
	    copyPersistentProperties(description, copy);
	    jpaComponentDao.saveAndFlush(copy);
	    jpaComponentDao.updateContent(copy.getId(), content);
	}

	if (content != null) {
	    BaseDescription copy = jpaComponentDao.findOne(id.longValue());
	    copy.setContent(content);
	    jpaComponentDao.saveAndFlush(copy);
	    jpaComponentDao.updateContent(copy.getId(), content);
	}
    }

    /**
     * Retrieves description by it's primary key Id
     * 
     * @param id
     *            Description key
     * @return The description, if it exists; null otherwise
     */
    @Override
    public BaseDescription getById(Number id) {
	BaseDescription baseDescription = jpaComponentDao.findOne(id.longValue());
	if (baseDescription != null && !baseDescription.isDeleted()) {
	    augment(baseDescription);
	    return baseDescription;
	}
	return null;
    }

    /**
     * Get by ComponentId / ProfileId, whether in userspace or public
     * 
     * @param id
     *            Full component id
     * @return The description, if it exists; null otherwise
     */
    @Override
    public BaseDescription getByCmdId(String id) {
	BaseDescription baseDescription = jpaComponentDao.findByComponentId(id);
	if (baseDescription != null && !baseDescription.isDeleted()) {
	    augment(baseDescription);
	    return baseDescription;
	}
	return null;
    }

    /**
     * Get by ComponentId / ProfileId
     * 
     * @param id
     *            Full component id
     * @param userId
     *            Db id of user for workspace; null for public space
     * @return The description, if it exists; null otherwise
     */
    @Override
    public BaseDescription getByCmdId(String id, Number userId) {
	BaseDescription baseDescription = getByCmdId(id);
	if (baseDescription != null) {
	    augment(baseDescription);
	    if (userId == null) {
		if (!baseDescription.isPublic())
		    baseDescription = null;
	    } else {
		if (baseDescription.isPublic()
			|| !compare(baseDescription.getUserId(), userId))
		    baseDescription = null;
	    }
	}
	return baseDescription;
    }

    /**
     * 
     * @param cmdId
     *            CMD Id of description
     * @return Database id for description record
     */
    @Override
    public Number getDbId(String cmdId) {
	Number id = null;
	BaseDescription c = jpaComponentDao.findByComponentId(cmdId);
	if (c != null)
	    id = c.getDbId();
	return id;
    }

    /**
     * 
     * @return All descriptions in the public space. Won't include xml content
     *         and comments count.
     */
    @Override
    public List<BaseDescription> getPublicDescriptions() {
	return augment(jpaComponentDao.findPublicItems());
    }

    /**
     * @return List of deleted descriptions in user space or in public when
     *         userId=null. Won't include xml content and comments count.
     * @param userId
     */
    @Override
    public List<BaseDescription> getDeletedDescriptions(Number userId) {
	List<BaseDescription> list = null;
	if (userId != null) {
	    list = jpaComponentDao.findDeletedItemsForUser(userId.longValue());
	} else {
	    list = jpaComponentDao.findPublicDeletedItems();
	}
	list = augment(list);
	return list;
    }

    /**
     * 
     * @return All the user's descriptions not in the public space .Won't
     *         include xml content and comments count.
     */
    @Override
    public List<BaseDescription> getUserspaceComponents(Number userId) {
	return augment(jpaComponentDao
		.findItemsForUserThatAreNotInGroups(userId.longValue(),
			ComponentDescription.COMPONENT_PREFIX + "%"));
    }

    @Override
    public List<BaseDescription> getUserspaceProfiles(Number userId) {
	return augment(jpaComponentDao.findItemsForUserThatAreNotInGroups(
		userId.longValue(), ProfileDescription.PROFILE_PREFIX + "%"));
    }

    @Override
    public void setDeleted(BaseDescription desc, boolean isDeleted) {
	BaseDescription copy = jpaComponentDao.findByComponentId(desc.getId());
	copy.setDeleted(isDeleted);
	jpaComponentDao.saveAndFlush(copy);
    }

    @Override
    public void setPublished(Number id, boolean published) {
	BaseDescription copy = jpaComponentDao.findOne(id.longValue());
	copy.setPublic(published);
	jpaComponentDao.saveAndFlush(copy);
    }

    /**
     * 
     * @param id
     *            Id of description record
     * @return Principal name of description's owner, if any. Otherwise, null.
     */
    @Override
    public String getOwnerPrincipalName(Number id) {
	BaseDescription baseDescription = getById(id);
	if (baseDescription == null)
	    return null;

	long userId = baseDescription.getDbUserId();
	RegistryUser user = userDao.findOne(userId);
	if (user == null)
	    return null;

	return user.getPrincipalName();
    }

    @Override
    public List<BaseDescription> getPublicComponentDescriptions() {
	return augment(jpaComponentDao
		.findPublicItems(ComponentDescription.COMPONENT_PREFIX + "%"));
    }

    @Override
    public List<BaseDescription> getPublicProfileDescriptions() {
	return augment(jpaComponentDao
		.findPublicItems(ProfileDescription.PROFILE_PREFIX + "%"));
    }

    @Override
    public List<String> getAllNonDeletedProfileIds() {
	return jpaComponentDao.findNonDeletedItemIds(ProfileDescription.PROFILE_PREFIX+"%");
    }

    @Override
    public List<String> getAllNonDeletedComponentIds() {
	return jpaComponentDao.findNonDeletedItemIds(ComponentDescription.COMPONENT_PREFIX+"%");
    }

}
