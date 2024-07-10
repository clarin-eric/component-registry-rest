package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.AuthenticationRequiredException;
import clarin.cmdi.componentregistry.model.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.model.BaseDescription;

public interface RegistryClosure<T extends BaseDescription> {
    
    T getDescription(ComponentRegistry registry, String id) throws ComponentRegistryException,  UserUnauthorizedException, AuthenticationRequiredException, ItemNotFoundException;


}
