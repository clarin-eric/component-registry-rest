package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.AuthenticationRequiredException;
import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.ComponentSpec;
import clarin.cmdi.componentregistry.BaseDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;

public interface RegisterAction {

    int execute(BaseDescription desc, ComponentSpec spec, RegisterResponse response, ComponentRegistry registry) throws UserUnauthorizedException, AuthenticationRequiredException, ItemNotFoundException;

    boolean isPreRegistration();
    
}
