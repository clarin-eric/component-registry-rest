package eu.clarin.cmdi.componentregistry.jersey.service;

import eu.clarin.cmdi.componentregistry.jersey.model.BaseDescription;
import java.util.List;

/**
 *
 * @author twagoo
 */
public interface ComponentRegistryService {

    BaseDescription getTestComponent();
    
    List<BaseDescription> getPublishedComponents();

}
