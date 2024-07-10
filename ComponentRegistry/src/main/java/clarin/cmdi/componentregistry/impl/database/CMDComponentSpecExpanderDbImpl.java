package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.BaseCMDComponentSpecExpander;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.components.ComponentSpec;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class CMDComponentSpecExpanderDbImpl extends BaseCMDComponentSpecExpander {

    private ComponentRegistryDbImpl dbImplRegistry;
    
    public CMDComponentSpecExpanderDbImpl(ComponentRegistryDbImpl registry) {
        super(registry);
        dbImplRegistry = registry;
    }

    public static ComponentSpec expandComponent(String componentId, ComponentRegistryDbImpl registry) throws ComponentRegistryException {
        CMDComponentSpecExpanderDbImpl expander = new CMDComponentSpecExpanderDbImpl(registry);
        return expander.expandComponent(componentId);
    }

    public static ComponentSpec expandProfile(String profileId, ComponentRegistryDbImpl registry) throws ComponentRegistryException {
        CMDComponentSpecExpanderDbImpl expander = new CMDComponentSpecExpanderDbImpl(registry);
        return expander.expandProfile(profileId);
    }

    @Override
    protected ComponentSpec getUncachedComponent(String componentId) throws ComponentRegistryException {
        return dbImplRegistry.getUncachedMDComponent(componentId);
    }

    @Override
    protected ComponentSpec getUncachedProfile(String profileId) throws ComponentRegistryException {
        return dbImplRegistry.getUncachedMDProfile(profileId);
    }
}
