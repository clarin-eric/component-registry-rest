package eu.clarin.cmdi.componentregistry.rest.service;

import eu.clarin.cmdi.componentregistry.components.ComponentSpec;
import eu.clarin.cmdi.componentregistry.rest.model.BaseDescription;
import eu.clarin.cmdi.componentregistry.rest.model.ComponentStatus;
import eu.clarin.cmdi.componentregistry.rest.model.ItemType;
import eu.clarin.cmdi.componentregistry.rest.model.RegistrySpace;
import java.util.Collection;
import java.util.List;
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
     * Get all published items with the specified type and status. Checks
     * authorization if needed (when requesting items from a private or team
     * space)
     *
     * @param type
     * @param space
     * @param status
     * @param sortBy field to sort by; provide an empty optional for unsorted
     * result
     * @param sortDirection sorting direction or provide an empty optional for
     * default direction
     * @param groupId user group (aka team) id
     * @return
     */
    List<BaseDescription> getItemDescriptions(ItemType type, RegistrySpace space, Collection<ComponentStatus> status,
            String sortBy, Sort.Direction sortDirection, Long userId, Long groupId);

    public boolean itemIsOfType(BaseDescription item, ItemType type);

}
