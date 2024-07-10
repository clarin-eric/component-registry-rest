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
package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.BaseUnitTest;
import clarin.cmdi.componentregistry.ItemIsLockedException;
import clarin.cmdi.componentregistry.ItemLockService;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ItemLock;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import clarin.cmdi.componentregistry.persistence.jpa.ItemLockDao;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class ItemLockServiceImplTest extends BaseUnitTest {

    @Autowired
    private ItemLockService itemLockService;

    @Autowired
    private ItemLockDao itemLockDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ComponentDao componentDao;

    private RegistryUser existingLockUser;
    private BaseDescription existingLockItem;
    private ItemLock existingLock;

    @Before
    public void setup() {
        ComponentRegistryTestDatabase.resetDatabase(jdbcTemplate);
        // user and component (description) tables are required in addition to the item lock table
        ComponentRegistryTestDatabase.createTableRegistryUser(jdbcTemplate);
        ComponentRegistryTestDatabase.createTablePersistentComponents(jdbcTemplate);
        ComponentRegistryTestDatabase.createTableComments(jdbcTemplate);
        ComponentRegistryTestDatabase.createTableItemLock(jdbcTemplate);

        existingLockUser = createUser();
        existingLockItem = createItem();

        existingLock = new ItemLock();
        existingLock.setUserId(existingLockUser.getId());
        existingLock.setItemId(existingLockItem.getDbId().intValue());

        itemLockDao.save(existingLock);
    }

    /**
     * Test of getLock method, of class ItemLockServiceImpl.
     */
    @Test
    public void testGetLock() {
        final ItemLock result = itemLockService.getLock(existingLockItem.getId());
        assertNotNull(result);
        assertEquals(existingLock.getId(), result.getId());
        assertEquals((long) existingLockItem.getDbId(), result.getItemId().longValue());
        assertEquals(existingLockUser.getId(), result.getUserId());
    }

    @Test
    public void testGetNonExistentLock() {
        assertNull("No lock should exist for component", itemLockService.getLock("non-existent-component"));
    }

    /**
     * Test of setLock method, of class ItemLockServiceImpl.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testSetLock() throws Exception {
        final BaseDescription newItem = createItem();
        final ItemLock result = itemLockService.setLock(newItem.getId(), existingLockUser.getPrincipalName());
        assertNotNull("Lock should have been created", result);
        assertEquals((long) newItem.getDbId(), result.getItemId().longValue());
        assertEquals(existingLockUser.getId(), result.getUserId());

        assertNotNull("Lock should be in database", itemLockDao.getLockForItem(existingLockItem.getDbId().intValue()));

        //test retrieve newly created lock
        final ItemLock retrievedLock = itemLockService.getLock(newItem.getId());
        assertNotNull("Created lock should have been retrieved", retrievedLock);
        assertEquals(result.getId(), retrievedLock.getId());
        assertEquals((long) newItem.getDbId(), retrievedLock.getItemId().longValue());
        assertEquals(existingLockUser.getId(), retrievedLock.getUserId());
    }

    /**
     * Test of setLock method, of class ItemLockServiceImpl.
     *
     * @throws java.lang.Exception
     */
    @Test(expected = ItemIsLockedException.class)
    public void testSetLockAgain() throws Exception {
        // this should throw an exception as the identified item is already locked
        itemLockService.setLock(existingLockItem.getId(), existingLockUser.getPrincipalName());
    }

    /**
     * Test of deleteLock method, of class ItemLockServiceImpl.
     */
    @Test
    public void testDeleteLock() {
        itemLockService.deleteLock(existingLockItem.getId());
        assertNull("Deleted lock should not be retrievable", itemLockService.getLock(existingLockItem.getId()));
        assertNull("Deleted lock should not be in datbase", itemLockDao.getLockForItem(existingLockItem.getDbId().intValue()));
    }

    private RegistryUser createUser() {
        return userDao.save(UserDaoTest.createTestUser());
    }

    private BaseDescription createItem() {
        final Date regDate = new Date();

        final BaseDescription description = ComponentDescription.createNewDescription();
        description.setName("MyComponent");
        description.setDescription("MyDescription");
        description.setCreatorName("Aap");
        description.setGroupName("MyGroup");
        description.setDomainName("MyDomain \u00CA");
        description.setRegistrationDate(regDate);

        final String testComponent = RegistryTestHelper.getComponentTestContentString();

        final Number id = componentDao.insertDescription(description, testComponent,
                true, null);
        return componentDao.getById(id);
    }

}
