package clarin.cmdi.componentregistry.rest;

import java.security.Principal;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.BaseComponent;
import clarin.cmdi.componentregistry.model.RegisterResponse;

public class UpdateAction implements RegisterAction {
    
    private final Principal principal;

    public UpdateAction(Principal principal) {
        this.principal = principal;
    }
    
    @Override
    public int execute(BaseComponent desc, CMDComponentSpec spec, RegisterResponse response, ComponentRegistry registry) {
        return registry.update(desc, spec, principal, false);
    }

}
