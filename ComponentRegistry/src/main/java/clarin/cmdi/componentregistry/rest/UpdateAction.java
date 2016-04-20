package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.AuthenticationRequiredException;
import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.ComponentSpec;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;

public class UpdateAction implements RegisterAction {
    
    @Override
    public int execute(BaseDescription desc, ComponentSpec spec, RegisterResponse response, ComponentRegistry registry)  throws UserUnauthorizedException, ItemNotFoundException, AuthenticationRequiredException{
        return registry.update(desc, spec, false);
    }

    @Override
    public boolean isPreRegistration() {
        return false;
    }

}
