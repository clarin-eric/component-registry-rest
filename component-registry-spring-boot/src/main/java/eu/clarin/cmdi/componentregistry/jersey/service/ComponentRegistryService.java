package eu.clarin.cmdi.componentregistry.jersey.service;

import eu.clarin.cmdi.componentregistry.jersey.model.BaseDescription;
import java.util.List;
import org.springframework.data.domain.Sort;

/**
 *
 * @author twagoo
 */
public interface ComponentRegistryService {

    BaseDescription getTestComponent();

    List<BaseDescription> getPublishedComponents();

    List<BaseDescription> getPublishedComponents(String sortBy, Sort.Direction sortDirection);

}
