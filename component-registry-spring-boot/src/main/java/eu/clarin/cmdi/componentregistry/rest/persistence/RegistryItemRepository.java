/*
 * Copyright (C) 2024 CLARIN ERIC
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
package eu.clarin.cmdi.componentregistry.rest.persistence;

import eu.clarin.cmdi.componentregistry.rest.model.BaseDescription;
import eu.clarin.cmdi.componentregistry.rest.model.ComponentStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author twagoo
 */
@Repository
public interface RegistryItemRepository extends JpaRepository<BaseDescription, Long> {
//
//        @Query("SELECT c FROM BaseDescription c WHERE c.ispublic = true and c.deleted = false and c.componentId like ?1 ORDER BY c.recommended desc, upper(c.name) asc")
//    List<BaseDescription> findPublishedItems(String idPrefix);
//
//    @Query("SELECT c FROM BaseDescription c WHERE c.ispublic = true and c.deleted = false and c.componentId like ?1 and status in ?2 ORDER BY c.recommended desc, upper(c.name) asc")
//    List<BaseDescription> findPublishedItems(String idPrefix, Collection<ComponentStatus> statusFilter);

    @Query("SELECT c FROM BaseDescription c"
            + " WHERE c.componentId like ?1"
            + " AND c.ispublic = ?2"
            + " AND status in ?3"
            + " AND c.deleted = false")
    List<BaseDescription> findItems(
            String idPrefix,
            boolean isPublic,
            Collection<ComponentStatus> status,
            Sort sort);

    @Query("SELECT c FROM BaseDescription c WHERE c.ispublic = true AND c.deleted = false") //+ "ORDER BY c.recommended desc, upper(c.name), c.id")
    List<BaseDescription> findPublicItems(Sort sort);

    @Query("SELECT c FROM BaseDescription c WHERE c.deleted = false AND c.componentId = ?1")
    public BaseDescription findByComponentId(String componentId);
}
