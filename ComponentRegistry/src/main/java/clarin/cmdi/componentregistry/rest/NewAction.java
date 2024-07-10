package clarin.cmdi.componentregistry.rest;

import static clarin.cmdi.componentregistry.CmdVersion.CANONICAL_CMD_VERSION;
import clarin.cmdi.componentregistry.model.ComponentRegistry;
import clarin.cmdi.componentregistry.components.ComponentSpec;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.ComponentStatus;
import clarin.cmdi.componentregistry.model.RegisterResponse;

public class NewAction implements RegisterAction {

    @Override
    public int execute(BaseDescription desc, ComponentSpec spec, RegisterResponse response, ComponentRegistry registry) {
        //new component always has development status
        spec.getHeader().setStatus(ComponentStatus.DEVELOPMENT.toString());
        //new component always has the canonical CMD version
        spec.setCMDVersion(CANONICAL_CMD_VERSION.getVersionNumber());
        spec.setCMDOriginalVersion(CANONICAL_CMD_VERSION.getVersionNumber());
        return registry.register(desc, spec);
    }

    @Override
    public boolean isPreRegistration() {
        return true;
    }
}
