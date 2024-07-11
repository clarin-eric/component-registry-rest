package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.AuthenticationRequiredException;
import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.model.ProfileDescription;

public class ProfileClosure implements RegistryClosure<ProfileDescription> {

    @Override
    public ProfileDescription getDescription(ComponentRegistry registry, String id) throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException, AuthenticationRequiredException {
        return registry.getProfileDescriptionAccessControlled(id);
    }

}
