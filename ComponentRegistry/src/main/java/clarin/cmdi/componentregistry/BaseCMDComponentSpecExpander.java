package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.components.ComponentSpec;
import clarin.cmdi.componentregistry.components.ComponentType;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class BaseCMDComponentSpecExpander implements CMDComponentSpecExpander {

    private final static Logger LOG = LoggerFactory.getLogger(BaseCMDComponentSpecExpander.class);
    protected final ComponentRegistry registry;

    public BaseCMDComponentSpecExpander(ComponentRegistry registry) {
        this.registry = registry;
    }

    /**
     *
     * @param cmdComponents a *mutable* list of components
     * @param id
     * @throws ComponentRegistryException
     */
    @Override
    public void expandNestedComponent(List<ComponentType> cmdComponents, String id) throws ComponentRegistryException {
        expandNestedComponent(cmdComponents, new HashSet<String>(Collections.singleton(id)));
    }

    private void expandNestedComponent(List<ComponentType> cmdComponents, Collection<String> path) throws ComponentRegistryException {
        List<ComponentType> expanded = new ArrayList<ComponentType>();
        for (ComponentType cmdComponentType : cmdComponents) {
            String componentId = cmdComponentType.getComponentRef();
            if (componentId != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("[Level {}] Expanding {}", path.size(), componentId);
                }
                if (path.contains(componentId)) {
                    throw new ComponentRegistryException("Detected recursion in component specification: " + path.toString() + " already contains " + componentId);
                } else {
                    Collection<String> newPath = new HashSet<String>(path);
                    newPath.add(componentId);
                    // Use uncached components and profiles, because we expand and thus change them this change should not be in the cache.
                    ComponentSpec spec = getUncachedComponent(componentId);
                    if (spec != null) {
                        ComponentType nested = spec.getComponent();
                        expandNestedComponent(nested.getComponent(), newPath);
                        overwriteAttributes(cmdComponentType, nested);
                        expanded.add(nested);
                    } else {
                        // Spec could not be resolved
                        LOG.warn("Could not resolve referenced component with id " + componentId);
                        // Add spec itself, without futher expanding
                        expanded.add(cmdComponentType);
                    }
                }
            } else {
                // No id = embedded component
                expandNestedComponent(cmdComponentType.getComponent(), path);
                expanded.add(cmdComponentType);//no attributes overwritten
            }
        }
        cmdComponents.clear();
        cmdComponents.addAll(expanded);
    }

    /**
     * Copying the cardinality specified in the referenceDeclaration over the
     * values in the actual component.
     */
    private void overwriteAttributes(ComponentType referenceDeclaration, ComponentType nested) {
        if (!referenceDeclaration.getCardinalityMax().isEmpty()) {
            List<String> cardinalityMax = nested.getCardinalityMax();
            cardinalityMax.clear();
            cardinalityMax.addAll(referenceDeclaration.getCardinalityMax());
        }
        if (!referenceDeclaration.getCardinalityMin().isEmpty()) {
            List<String> cardinalityMin = nested.getCardinalityMin();
            cardinalityMin.clear();
            cardinalityMin.addAll(referenceDeclaration.getCardinalityMin());
        }
        nested.setComponentRef(referenceDeclaration.getComponentRef()); // Setting componentId for better xsd generation.
    }

    protected ComponentSpec expandComponent(String componentId) throws ComponentRegistryException {
        // Use uncached components and profiles, because we expand and thus change them this change should not be in the cache.
        ComponentSpec result = getUncachedComponent(componentId);//registry.getUncachedComponent(componentId);
        ComponentType cmdComponentType = result.getComponent();
        expandNestedComponent(cmdComponentType.getComponent(), componentId);
        return result;
    }

    protected ComponentSpec expandProfile(String profileId) throws ComponentRegistryException {
        // Use uncached components and profiles, because we expand and thus change them this change should not be in the cache.
        ComponentSpec profileSpec = getUncachedProfile(profileId);
        expandProfileSpec(profileSpec, profileId);
        return profileSpec;
    }

    private void expandProfileSpec(ComponentSpec profileSpec, String profileId) throws ComponentRegistryException {
        // make a temporary list of the root component (singleton)
        final ComponentType rootComponent = profileSpec.getComponent();
        final ArrayList<ComponentType> rootComponentList = Lists.newArrayList(rootComponent);
        // expand 'all' components
        expandNestedComponent(rootComponentList, profileId);
        // put the expanded root back in the profile spec
        profileSpec.setComponent(rootComponentList.get(0));
    }

//    protected ComponentSpec expandComment(String commentId) throws ComponentRegistryException {
//        ComponentSpec result = getUncachedComment(commentId);
//        List<ComponentType> cmdComponents = result.getComponent();
//        expandNestedComponent(cmdComponents);
//        return result;
//    }
    /**
     * Get uncached component from "this" registry and possibly from public
     * registry. Note: "this" registry can be a user registry.
     *
     * @param componentId
     */
    protected abstract ComponentSpec getUncachedComponent(String componentId) throws ComponentRegistryException;

    protected abstract ComponentSpec getUncachedProfile(String profileId) throws ComponentRegistryException;
//    protected abstract ComponentSpec getUncachedComment(String commentId) throws ComponentRegistryException;
}
