package eu.clarin.cmdi.componentregistry.rest.service;

import eu.clarin.cmdi.componentregistry.components.ComponentSpec;
import eu.clarin.cmdi.componentregistry.rest.model.BaseDescription;
import java.util.List;
import org.springframework.data.domain.Sort;

/**
 *
 * @author twagoo
 */
public interface ComponentRegistryService {

    BaseDescription getItemDescription(String componentId);

    ComponentSpec getItemSpecification(String componentId);

    List<BaseDescription> getPublishedDescriptions();

    List<BaseDescription> getPublishedDescriptions(String sortBy, Sort.Direction sortDirection);

}
