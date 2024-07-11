package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.AuthenticationRequiredException;
import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.ShhaaUserCredentials;
import com.sun.jersey.api.core.InjectParam;
import java.security.Principal;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class AbstractComponentRegistryRestService {

    @Context
    protected SecurityContext security;

    @InjectParam(value = "componentRegistryFactory")
    protected ComponentRegistryFactory componentRegistryFactory;

    protected ComponentRegistry getBaseRegistry() throws AuthenticationRequiredException {
        Principal userPrincipal = security.getUserPrincipal();
        if (userPrincipal == null) {
            return componentRegistryFactory.getBaseRegistry(null);
        } else {
            ShhaaUserCredentials userCredentials = this.getUserCredentials(userPrincipal);
            return componentRegistryFactory.getBaseRegistry(userCredentials);
        }
    }

    protected ShhaaUserCredentials getUserCredentials(Principal userPrincipal) {
        ShhaaUserCredentials userCredentials = null;
        if (userPrincipal != null) {
            userCredentials = new ShhaaUserCredentials(userPrincipal);
        }
        return userCredentials;
    }

    /**
     * @param componentRegistryFactory the componentRegistryFactory to set
     */
    public void setComponentRegistryFactory(
            ComponentRegistryFactory componentRegistryFactory) {
        this.componentRegistryFactory = componentRegistryFactory;
    }

    /**
     * Helper method to cause trigger an immediate error response through a web
     * application exception
     *
     * @param status status for response
     * @param message message for response
     * @throws WebApplicationException with specified status and response
     * message
     */
    protected WebApplicationException serviceException(Response.Status status, String message) {
        return new WebApplicationException(Response
                .serverError()
                .status(status)
                .entity(message)
                .build());
    }

    /**
     *
     * @return Principal of current request
     * @throws AuthenticationRequiredException If no user principal found
     */
    protected final Principal checkAndGetUserPrincipal() throws AuthenticationRequiredException {
        Principal principal = security.getUserPrincipal();
        if (principal == null) {
            throw new AuthenticationRequiredException("No user principal found.");
        }
        return principal;
    }
}
