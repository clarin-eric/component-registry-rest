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

import clarin.cmdi.componentregistry.ItemIsLockedException;
import clarin.cmdi.componentregistry.ItemLockService;
import clarin.cmdi.componentregistry.model.ItemLock;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import clarin.cmdi.componentregistry.persistence.jpa.ItemLockDao;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;
import java.util.Calendar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Service("ItemLockService")
public class ItemLockServiceImpl implements ItemLockService {

    @Autowired
    private ComponentDao componentDao;

    @Autowired
    private ItemLockDao itemLockDao;

    @Autowired
    private UserDao userDao;

    @Override
    public ItemLock getLock(String componentId) {
        final Number itemId = componentDao.getDbId(componentId);
        if (itemId == null) {
            return null;
        } else {
            return itemLockDao.getLockForItem(itemId.intValue());
        }
    }

    @Override
    public ItemLock setLock(String componentId, String ownerPrincipalName) throws ItemIsLockedException {
        final ItemLock existingLock = getLock(componentId);
        if (existingLock == null) {
            final ItemLock newLock = new ItemLock();
            newLock.setItemId(componentDao.getDbId(componentId).intValue());
            newLock.setUserId(userDao.getByPrincipalName(ownerPrincipalName).getId());
            newLock.setTimestamp(Calendar.getInstance().getTime());
            return itemLockDao.save(newLock);
        } else {
            final RegistryUser currentOwner = userDao.getPrincipalNameById(existingLock.getUserId());
            throw new ItemIsLockedException("Item " + componentId + " is already locked by user " + currentOwner);
        }
    }

    @Override
    public void deleteLock(String componentId) {
        final ItemLock existingLock = getLock(componentId);
        if (existingLock != null) {
            itemLockDao.delete(existingLock);
        }
    }

}
