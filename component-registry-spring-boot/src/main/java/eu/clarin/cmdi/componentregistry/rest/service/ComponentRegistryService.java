package eu.clarin.cmdi.componentregistry.rest.service;

import eu.clarin.cmdi.componentregistry.components.ComponentSpec;
import eu.clarin.cmdi.componentregistry.rest.model.BaseDescription;
import eu.clarin.cmdi.componentregistry.rest.model.ComponentStatus;
import eu.clarin.cmdi.componentregistry.rest.model.ItemType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;

/**
 *
 * @author twagoo
 */
public interface ComponentRegistryService {

    BaseDescription getItemDescription(String componentId);

    ComponentSpec getItemSpecification(String componentId);

    String getItemSpecificationXml(String componentId);

    /**
     * *
     * @return an unsorted list of all published items
     */
    List<BaseDescription> getPublishedDescriptions();

    /**
     * *
     * @param status
     * @param sortBy field to sort by
     * @param sortDirection direction to sort in
     * @return a sorted list of all published items
     */
    List<BaseDescription> getItemDescriptions(Collection<ComponentStatus> status, Optional<String> sortBy, Optional<Sort.Direction> sortDirection);

    /**
     * *
     * Get all published items with the specified type and status
     *
     * @param type
     * @param status
     * @param sortBy field to sort by; provide an empty optional for unsorted
     * result
     * @param sortDirection sorting direction or provide an empty optional for
     * default direction
     * @return
     */
    List<BaseDescription> getItemDescriptions(ItemType type, Collection<ComponentStatus> status,
            Optional<String> sortBy, Optional<Sort.Direction> sortDirection);

    public boolean itemIsOfType(BaseDescription item, ItemType type);

}
