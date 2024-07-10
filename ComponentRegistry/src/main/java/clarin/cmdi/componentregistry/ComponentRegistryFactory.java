package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.model.ComponentRegistry;
import clarin.cmdi.componentregistry.model.RegistryUser;
import java.security.Principal;
import java.util.List;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface ComponentRegistryFactory {

    public static final String ANONYMOUS_USER = "anonymous"; //Default shibboleth fallback.

    List<ComponentRegistry> getAllUserRegistries();

    /**
     * Gets the specified registry
     *
     * @param status status of the registry
     * @param owner owner of the registry. Passing null will assume the authenticated user as a {@link OwnerUser}
     * @param credentials credentials that authenticate user, can be left null if requested registry has a public status
     * @return
     */
    ComponentRegistry getComponentRegistry(RegistrySpace space, Owner owner, ShhaaUserCredentials credentials, Number groupId) throws UserUnauthorizedException;

    ComponentRegistry getPublicRegistry();
    
    ComponentRegistry getOtherUserComponentRegistry(Principal adminPrincipal, Owner owner);
    
    ComponentRegistry getBaseRegistry(ShhaaUserCredentials credentials);

    RegistryUser getOrCreateUser(ShhaaUserCredentials credentials);
}
