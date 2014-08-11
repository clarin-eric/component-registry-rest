package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.model.ComponentDescription;

public class ComponentClosure implements RegistryClosure<ComponentDescription> {

    @Override
    public ComponentDescription getDescription(ComponentRegistry registry, String id) throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException {
        return registry.getComponentDescriptionAccessControlled(id);
    }

}
