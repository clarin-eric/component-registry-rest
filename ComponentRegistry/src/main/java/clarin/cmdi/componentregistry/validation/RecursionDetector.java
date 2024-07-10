package clarin.cmdi.componentregistry.validation;

import clarin.cmdi.componentregistry.model.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.ComponentSpec;
import clarin.cmdi.componentregistry.model.BaseDescription;
import com.google.common.collect.Lists;

/**
 * Expands a spec to check for recursion.
 *
 * @author twagoo
 */
public class RecursionDetector extends BaseValidator {

    private final ComponentSpecCopyFactory specCopyFactory;
    private final ComponentRegistry registry;
    private final BaseDescription desc;
    private ComponentSpec expandedSpecCopy;

    public RecursionDetector(ComponentSpecCopyFactory specCopyFactory, ComponentRegistry registry, BaseDescription desc) {
        this.specCopyFactory = specCopyFactory;
        this.registry = registry;
        this.desc = desc;
    }

    @Override
    public boolean validate() throws UserUnauthorizedException {
        try {
            //Operate on copy so that origina does not get expanded.
            expandedSpecCopy = specCopyFactory.newSpecCopy();

            // In case of recursion, the following will throw a ComponentRegistryException
            registry.getExpander().expandNestedComponent(
                    Lists.newArrayList(expandedSpecCopy.getComponent()), desc.getId());

            //expansion succeeded
            return true;
        } catch (ComponentRegistryException ex) {
            //expansion failed (assuming because of recursion)
            addErrorMessage(ex.getMessage());
            return false;
        }
    }

    public ComponentSpec getExpandedSpecCopy() {
        return expandedSpecCopy;
    }

    public static interface ComponentSpecCopyFactory {

        ComponentSpec newSpecCopy() throws ComponentRegistryException;
    }

    @Override
    public boolean runIfInvalid() {
        return false;
    }

}
