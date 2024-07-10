package clarin.cmdi.componentregistry.persistence.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.ComponentStatus;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import java.util.Collection;

/**
 * Basic jpa persistence functions. The more complicated rest is performed by
 * {@link ComponentDao}
 *
 * @author george.georgovassilis@mpi.nl
 * @author twan@clarin.eu
 *
 */
public interface JpaComponentDao extends JpaRepository<BaseDescription, Long> {

    @Query("SELECT c FROM BaseDescription c WHERE c.componentId = ?1")
    BaseDescription findByComponentId(String componentId);

    @Query("SELECT c FROM BaseDescription c WHERE c.ispublic = true and c.deleted = false ORDER BY c.recommended desc, upper(c.name), c.id")
    List<BaseDescription> findPublicItems();

    @Query("SELECT c FROM BaseDescription c WHERE c.ispublic = true and c.deleted = false and c.componentId like ?1 ORDER BY c.recommended desc, upper(c.name) asc")
    List<BaseDescription> findPublishedItems(String idPrefix);

    @Query("SELECT c FROM BaseDescription c WHERE c.ispublic = true and c.deleted = false and c.componentId like ?1 and status in ?2 ORDER BY c.recommended desc, upper(c.name) asc")
    List<BaseDescription> findPublishedItems(String idPrefix, Collection<ComponentStatus> statusFilter);

    @Query("SELECT c FROM BaseDescription c WHERE c.ispublic = false and c.deleted = true and c.dbUserId = ?1 ORDER BY c.recommended desc, upper(c.name), c.id")
    List<BaseDescription> findDeletedItemsForUser(Long userId);

    @Query("SELECT c FROM BaseDescription c"
            + " WHERE c.ispublic = false and c.deleted = true"
            + " AND exists (select o from Ownership o WHERE o.componentId = c.componentId AND o.groupId = ?1)"
            + " ORDER BY c.recommended desc, upper(c.name), c.id")
    List<BaseDescription> findDeletedItemsForTeam(Long teamId);

    @Query("SELECT c FROM BaseDescription c WHERE c.ispublic = true and c.deleted = true ORDER BY c.recommended desc, upper(c.name), c.id")
    List<BaseDescription> findPublicDeletedItems();

    //compare @Query("SELECT c FROM BaseDescription c WHERE c.dbUserId = ?1 AND c.deleted = false AND c.ispublic = false AND c.componentId like ?2 order by upper(c.name), c.id")
    @Query("SELECT c FROM BaseDescription c"
            + " WHERE c.dbUserId = ?1"
            + " AND c.deleted = false"
            + " AND c.ispublic = false"
            + " AND c.componentId like ?2"
            + " AND not exists (select 1 from Ownership o WHERE o.componentId = c.componentId)"
            + " ORDER BY c.recommended desc, upper(c.name), c.id")
    List<BaseDescription> findItemsForUserThatAreNotInGroups(Long userId, String idPrefix);

    @Query("SELECT c FROM BaseDescription c"
            + " WHERE c.dbUserId = ?1"
            + " AND c.deleted = false"
            + " AND c.ispublic = false"
            + " AND c.componentId like ?2"
            + " AND not exists (select 1 from Ownership o WHERE o.componentId = c.componentId)"
            + " AND status in ?3"
            + " ORDER BY c.recommended desc, upper(c.name), c.id")
    List<BaseDescription> findItemsForUserThatAreNotInGroups(Long userId, String idPrefix, Collection<ComponentStatus> statusFilter);

    @Modifying
    @Query(nativeQuery = true, value = "update basedescription set content = ?2 WHERE component_id like ?1")
    void updateContent(String componentId, String content);

    @Query(nativeQuery = true, value = "SELECT content from basedescription WHERE component_id like ?1 and is_deleted = ?2")
    String findContentByComponentId(String componentId, boolean deleted);

    @Query(nativeQuery = true, value = "SELECT content from basedescription WHERE component_id like ?1")
    String findContentByComponentId(String componentId);

    @Query("SELECT c.componentId FROM BaseDescription c WHERE c.deleted = false and c.componentId like ?1 ORDER BY c.recommended desc, c.id asc")
    List<String> findNonDeletedItemIds(String componentIdPrefix);

    @Query("SELECT c.componentId FROM BaseDescription c"
            + " WHERE c.deleted = false"
            + " AND c.componentId like ?1"
            + " AND status in ?2"
            + " ORDER BY c.recommended desc, c.id asc")
    List<String> findNonDeletedItemIds(String componentIdPrefix, Collection<ComponentStatus> statusFilter);

    @Query("SELECT c.componentId FROM BaseDescription c"
            + " WHERE c.deleted = false"
            + " AND c.componentId like ?1"
            + " AND content like CONCAT('%', ?2, '%')"
            + " ORDER BY c.recommended desc, c.id asc")
    List<String> findNonDeletedItemIds(String componentIdPrefix, String contentFilter);

    @Query("SELECT c.componentId FROM BaseDescription c"
            + " WHERE c.deleted = false"
            + " AND c.componentId like ?1"
            + " AND content like CONCAT('%', ?2, '%')"
            + " AND status in ?3"
            + " ORDER BY c.recommended desc, c.id asc")
    List<String> findNonDeletedItemIds(String componentIdPrefix, String contentFilter, Collection<ComponentStatus> statusFilter);

    @Query("SELECT c FROM BaseDescription c"
            + " WHERE c.deleted = false"
            + " ORDER BY c.recommended desc, c.id asc")
    List<BaseDescription> findNonDeletedDescriptions();

    @Query("SELECT c FROM BaseDescription c"
            + " WHERE c.dbUserId = ?1"
            + " AND c.deleted = false"
            + " AND c.ispublic = false"
            + " AND c.componentId like ?2"
            + " ORDER BY c.recommended desc, upper(c.name), c.id")
    List<BaseDescription> findNotPublishedUserItems(Long userId, String idPrefix);

    @Query("SELECT c.componentId FROM BaseDescription c"
            + " WHERE c.ispublic = ?1"
            + " AND c.componentId like ?2"
            + " AND exists (select o from Ownership o WHERE o.componentId = c.componentId AND o.groupId = ?3)"
            + " ORDER BY c.recommended desc, upper(c.name) asc")
    List<String> findAllItemIdsInGroup(boolean isPublished, String idPrefix, long groupId);

    @Query("SELECT c.componentId FROM BaseDescription c"
            + " WHERE c.ispublic = ?1"
            + " AND c.componentId like ?2"
            + " AND exists (select o from Ownership o WHERE o.componentId = c.componentId AND o.groupId = ?3)"
            + " AND status in ?4"
            + " ORDER BY c.recommended desc, upper(c.name) asc")
    List<String> findAllItemIdsInGroup(boolean isPublished, String idPrefix, long groupId, Collection<ComponentStatus> statusFilter);
}
