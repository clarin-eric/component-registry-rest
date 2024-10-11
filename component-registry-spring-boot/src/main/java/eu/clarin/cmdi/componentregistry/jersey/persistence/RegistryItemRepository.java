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
package eu.clarin.cmdi.componentregistry.jersey.persistence;

import eu.clarin.cmdi.componentregistry.jersey.model.BaseDescription;
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

//    @Query("SELECT c FROM BaseDescription c WHERE c.componentId = ?1")
//    BaseDescription findByComponentId(String componentId);
    @Query("SELECT c FROM BaseDescription c WHERE c.ispublic = true and c.deleted = false")
    //+ "ORDER BY c.recommended desc, upper(c.name), c.id")
    List<BaseDescription> findPublicItems(Sort sort);
}
