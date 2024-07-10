package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.AuthenticationRequiredException;
import clarin.cmdi.componentregistry.CmdVersion;
import clarin.cmdi.componentregistry.model.ComponentRegistry;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.ComponentSpec;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;

public class UpdateAction implements RegisterAction {
    
    @Override
    public int execute(BaseDescription desc, ComponentSpec spec, RegisterResponse response, ComponentRegistry registry)  throws UserUnauthorizedException, ItemNotFoundException, AuthenticationRequiredException{
        spec.setCMDOriginalVersion(CmdVersion.CANONICAL_CMD_VERSION.getVersionNumber());
        spec.setCMDVersion(CmdVersion.CANONICAL_CMD_VERSION.getVersionNumber());
        return registry.update(desc, spec, false);
    }

    @Override
    public boolean isPreRegistration() {
        return false;
    }

}
