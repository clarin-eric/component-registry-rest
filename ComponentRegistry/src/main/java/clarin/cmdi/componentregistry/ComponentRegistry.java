package clarin.cmdi.componentregistry;

import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.util.List;

import clarin.cmdi.componentregistry.components.CMDComponentSpec;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

public interface ComponentRegistry {

    public static final String REGISTRY_ID = "clarin.eu:cr1:";

    List<ComponentDescription> getComponentDescriptions() throws ComponentRegistryException;

    ComponentDescription getComponentDescription(String id) throws ComponentRegistryException;

    List<ProfileDescription> getProfileDescriptions() throws ComponentRegistryException;

    ProfileDescription getProfileDescription(String id) throws ComponentRegistryException;

    CMDComponentSpec getMDProfile(String id) throws ComponentRegistryException;

    CMDComponentSpec getMDComponent(String id) throws ComponentRegistryException;

    /**
     * 
     * @return -1 if profile could not be registered
     */
    int register(AbstractDescription desc, CMDComponentSpec spec);

    /**
     * 
     * @return -1 if component could not be updated
     */
    int update(AbstractDescription description, CMDComponentSpec spec);

    /**
     * 
     * @return -1 if component could not be published. Published means move from
     *         current (private) workspace to public workspace.
     */
    int publish(AbstractDescription desc, CMDComponentSpec spec, Principal principal);

    void getMDProfileAsXml(String profileId, OutputStream output) throws ComponentRegistryException;

    void getMDProfileAsXsd(String profileId, OutputStream outputStream) throws ComponentRegistryException;

    void getMDComponentAsXml(String componentId, OutputStream output) throws ComponentRegistryException;

    void getMDComponentAsXsd(String componentId, OutputStream outputStream) throws ComponentRegistryException;

    /**
     * 
     * @param profileId
     * @param principal
     * @throws IOException
     * @throws UserUnauthorizedException
     *             thrown when principal does not match creator of profile
     * @throws DeleteFailedException
     */
    void deleteMDProfile(String profileId, Principal principal) throws IOException, UserUnauthorizedException, ComponentRegistryException, DeleteFailedException;

    /**
     * 
     * @param componentId
     * @param principal
     * @param forceDelete
     *            ignores the fact that the component is still in use by other
     *            components and just deletes.
     * @throws IOException
     * @throws UserUnauthorizedException
     *             thrown when principal does not match creator of component
     * @throws DeleteFailedException
     */
    void deleteMDComponent(String componentId, Principal principal, boolean forceDelete) throws IOException,  ComponentRegistryException, UserUnauthorizedException,
	    DeleteFailedException;

    /**
     * 
     * @param componentId
     * @return List of ComponentDescriptions of Components that use the given
     *         Component.
     */
    List<ComponentDescription> getUsageInComponents(String componentId) throws ComponentRegistryException;

    /**
     * 
     * @param componentId
     * @return List of ProfileDescriptions of Profiles that use the given
     *         Component.
     */
    List<ProfileDescription> getUsageInProfiles(String componentId) throws ComponentRegistryException;

    /**
     * Return true if this registry is the public registry as opposed to a
     * registry used for the user privately.
     **/
    boolean isPublic();

    String getName();

    List<ProfileDescription> getDeletedProfileDescriptions();

    List<ComponentDescription> getDeletedComponentDescriptions();

}
