package clarin.cmdi.componentregistry.persistence.jpa;

import clarin.cmdi.componentregistry.BaseUnitTest;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryTestDatabase;
import clarin.cmdi.componentregistry.impl.database.UserDaoTest;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ItemLock;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;
import java.util.Date;
import static junit.framework.TestCase.assertNotNull;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

/**
 *
 * @author twagoo
 */
public class ItemLockDaoTest extends BaseUnitTest {

    @Autowired
    private ItemLockDao itemLockDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    ComponentDao componentDao;

    public ItemLockDaoTest() {
    }

    @Before
    public void init() {
        ComponentRegistryTestDatabase.resetDatabase(jdbcTemplate);
        // user and component (description) tables are required in addition to the item lock table
        ComponentRegistryTestDatabase.createTableRegistryUser(jdbcTemplate);
        ComponentRegistryTestDatabase.createTablePersistentComponents(jdbcTemplate);
        ComponentRegistryTestDatabase.createTableItemLock(jdbcTemplate);
    }

    @Test
    public void testInjection() {
        assertNotNull(jdbcTemplate);
        assertNotNull(itemLockDao);
    }

    @Test
    public void testInsertLock() {
        long userId = createUser().longValue();
        int itemId = createItem().intValue();

        final ItemLock lock = new ItemLock();
        lock.setUserId(userId);
        lock.setItemId(itemId);

        //save lock
        final ItemLock savedLock = itemLockDao.save(lock);
        assertNotNull(savedLock);

        //retrieve and check
        final ItemLock retrievedItem = itemLockDao.getLockForItem(savedLock.getId());
        assertEquals(lock.getUserId(), retrievedItem.getUserId());
        assertEquals(lock.getItemId(), retrievedItem.getItemId());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testInsertLockTwice() {
        long userId = createUser().longValue();
        int itemId = createItem().intValue();

        final ItemLock lock = new ItemLock();
        lock.setUserId(userId);
        lock.setItemId(itemId);

        //save lock
        itemLockDao.save(lock);

        //second lock
        final ItemLock lock2 = new ItemLock();
        lock2.setUserId(userId);
        lock2.setItemId(itemId);
        itemLockDao.save(lock2);
        
        itemLockDao.flush();
    }

    /**
     * Test of getLockForItem method, of class ItemLockDao.
     */
    @Test
    @Ignore
    public void testGetLockForItem() {
        //TODO
//        System.out.println("getLockForItem");
//        Integer itemId = null;
//        ItemLock expResult = null;
//        ItemLock result = itemLockDao.getLockForItem(itemId);
//        assertEquals(expResult, result);
    }

    private Number createUser() {
        return userDao.save(UserDaoTest.createTestUser()).getId();
    }

    private Number createItem() {
        Date regDate = new Date();

        BaseDescription description = ComponentDescription.createNewDescription();
        description.setName("MyComponent");
        description.setDescription("MyDescription");
        description.setCreatorName("Aap");
        description.setGroupName("MyGroup");
        description.setDomainName("MyDomain \u00CA");

        description.setRegistrationDate(regDate);

        String testComponent = RegistryTestHelper.getComponentTestContentString();
        return componentDao.insertDescription(description, testComponent,
                true, null);
    }

}
