package clarin.cmdi.componentregistry.impl.database;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import clarin.cmdi.componentregistry.BaseUnitTest;
import clarin.cmdi.componentregistry.BaseDescription;
import clarin.cmdi.componentregistry.ComponentDescription;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;

/**
 * Base test class for concrete tests to profile and component DAOs
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author George.Georgovassilis@mpi.nl
 */
public abstract class AbstractDescriptionDaoTest extends BaseUnitTest {

    @Autowired
    private UserDao userDao;

    protected abstract ComponentDao getDao();

    @Test
    public void testInjection() {
	assertNotNull(jdbcTemplate);
	assertNotNull(getDao());
    }

    @Test
    public void testInsertComponent() throws Exception {
	Date regDate = new Date();

	BaseDescription description = createNewDescription();
	description.setName("MyComponent");
	description.setDescription("MyDescription");
	description.setCreatorName("Aap");
	description.setGroupName("MyGroup");
	description.setDomainName("MyDomain \u00CA");

	description.setRegistrationDate(regDate);

	String testComponent = getContentString();
	Number newId = getDao().insertDescription(description, testComponent,
		true, null);
	assertNotNull(newId);
	BaseDescription descr = getDao().getById(newId);
	assertNotNull(descr);
	assertEquals("MyComponent", descr.getName());
	assertEquals("MyDescription", descr.getDescription());
	assertEquals("Aap", descr.getCreatorName());
	assertEquals("MyGroup", descr.getGroupName());
	assertEquals("MyDomain \u00CA", descr.getDomainName());
	assertEquals(regDate, descr.getRegistrationDate());
	assertEquals(testComponent,
		getDao().getContent(false, description.getId()));
    }

    @Test
    public void testGetDescriptionsOrdered() throws Exception {
	insert("a", true, null);
	insert("A", true, null);
	insert("B", true, null);
	insert("a", true, null);

	List<BaseDescription> descs = getDao().getPublicDescriptions();
	assertEquals(4, descs.size());
	assertEquals("a", descs.get(0).getName()); // ordered by name case
						   // insensitive then by cmdId
	assertEquals("A", descs.get(1).getName());
	assertEquals("a", descs.get(2).getName());
	assertTrue(descs.get(1).getId().compareTo(descs.get(2).getId()) < 0);
	assertEquals("B", descs.get(3).getName());

    }

    @Test
    public void testGetPublicComponents() throws Exception {
	List<BaseDescription> descriptions = getDao().getPublicDescriptions();
	assertNotNull(descriptions);
    }

    @Test
    public void testGetUserspaceDescriptions() throws Exception {
	// TODO: test queries empty database and is happy that no results are
	// returned. Should also test the case where there are userspace
	// descriptions
	List<BaseDescription> descriptions = getDao().getPrivateBaseDescriptions(-1, ComponentDescription.COMPONENT_PREFIX, null);
	assertEquals(0, descriptions.size());
    }

    @Test
    public void testDeleteDescription() throws Exception {
	BaseDescription description = createNewDescription();
	description.setName("Aap");
	description.setDescription("MyDescription");
	String testComponent = getContentString();

	int count = getDao().getPublicDescriptions().size();
	// insert
	getDao().insertDescription(description, testComponent, true, null);
	assertEquals(count + 1, getDao().getPublicDescriptions().size());

	List<BaseDescription> deletedDescriptions = getDao().getDeletedDescriptions(null);
	assertEquals(0, deletedDescriptions.size());

	// delete
	getDao().setDeleted(description, true);
	assertEquals(count, getDao().getPublicDescriptions().size());

	deletedDescriptions = getDao().getDeletedDescriptions(null);
	assertEquals(1, deletedDescriptions.size());
    }

    @Test
    public void testUpdateDescription() {
	BaseDescription description = createNewDescription();
	description.setName("Aap");
	description.setDescription("MyDescription");
	description.setCreatorName("Aap");
	description.setGroupName("MyGroup");
	description.setDomainName("MyDomain");

	String testComponent = getContentString();
	Number newId = getDao().insertDescription(description, testComponent,
		true, null);

	// Change values
	description.setName("Noot");
	description.setDescription("AnotherDescription");
	description.setCreatorName("AnotherAap");
	description.setGroupName("AnotherGroup");
	description.setDomainName("AnotherDomain\u00CA");
	// Update in db
	getDao().updateDescription(newId, description, null);
	description = getDao().getById(newId);
	// Test if new values are there
	assertNotNull(description);
	assertEquals("Noot", description.getName());
	assertEquals("AnotherDescription", description.getDescription());
	assertEquals("AnotherAap", description.getCreatorName());
	assertEquals("AnotherGroup", description.getGroupName());
	assertEquals("AnotherDomain\u00CA", description.getDomainName());

	// Update content
	String testContent2 = "<test>Test content</test>";
	getDao().updateDescription(newId, null, testContent2);
	// Test if new content is there
	assertEquals(testContent2,
		getDao().getContent(false, description.getId()));

	// Update both
	description.setName("Mies");
	description.setDescription("YetAnotherDescription");
	String testContent3 = "<test>More test \u00CA content</test>";

	// Update in db
	getDao().updateDescription(newId, description, testContent3);
	description = getDao().getById(newId);
	// Test if new values are there
	assertNotNull(description);
	assertEquals("Mies", description.getName());
	assertEquals("YetAnotherDescription", description.getDescription());
	// Test if new content is there
	assertEquals(testContent3,
		getDao().getContent(false, description.getId()));
    }

    @Test
    public void testIsPublic() {
	Number userId = userDao.save(UserDaoTest.createTestUser()).getId();
	BaseDescription publicDesc = insert(true, null);
	assertTrue(getDao().isPublic(publicDesc.getId()));
	assertFalse(getDao().isInUserSpace(publicDesc.getId(), userId));

	BaseDescription privateDesc = insert(false, userId);
	assertFalse(getDao().isPublic(privateDesc.getId()));
	assertTrue(getDao().isInUserSpace(privateDesc.getId(), userId));

	getDao().setDeleted(publicDesc, true);
	assertTrue(getDao().isPublic(publicDesc.getId()));
	assertFalse(getDao().isInUserSpace(publicDesc.getId(), userId));

	getDao().setDeleted(privateDesc, true);
	assertFalse(getDao().isPublic(privateDesc.getId()));
	assertTrue(getDao().isInUserSpace(privateDesc.getId(), userId));
    }

    private BaseDescription insert(boolean isPublic, Number userId) {
	return insert("Aap", isPublic, userId);
    }

    private BaseDescription insert(String name, boolean isPublic, Number userId) {
	BaseDescription desc = createNewDescription();
	desc.setName(name);
	desc.setDescription("MyDescription");
	getDao().insertDescription(desc, getContentString(), isPublic, userId);
	return desc;
    }

    protected abstract String getContentString();

    protected abstract BaseDescription createNewDescription();
}
