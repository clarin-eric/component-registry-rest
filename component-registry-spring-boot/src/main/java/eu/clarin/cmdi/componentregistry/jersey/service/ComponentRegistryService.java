package eu.clarin.cmdi.componentregistry.jersey.service;

import eu.clarin.cmdi.componentregistry.jersey.model.BaseDescription;
import java.util.List;
import org.springframework.data.domain.Sort;

/**
 *
 * @author twagoo
 */
public interface ComponentRegistryService {

    BaseDescription getItemDescription(String componentId);

    BaseDescription getItemSpecification(String componentId);

    List<BaseDescription> getPublishedDescriptions();

    List<BaseDescription> getPublishedDescriptions(String sortBy, Sort.Direction sortDirection);

}
