package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.AuthenticationRequiredException;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.UserCredentials;
import clarin.cmdi.componentregistry.model.AuthenticationInfo;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;
import com.google.common.base.Strings;
import java.net.URI;
import java.security.Principal;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Authentication resource to be used by the client to retrieve the current
 * authentication status and/or to force an authentication request if the user
 * is not authenticated.
 *
 * <p>
 * A 'GET' on this resource will return a JSON or XML structure with the
 * following information:</p>
 * <ul>
 * <li>authentication (true/false)</li>
 * <li>username (string)</li>
 * <li>displayName</li>
 * (string)
 * </ul>
 *
 * <p>
 * A 'POST' to this resource will trigger an authentication request (by means of
 * a 401) response code if the user is not yet authenticated. In case of a
 * successful authentication, it will respond with a redirect (303) to this same
 * resource.</p>
 *
 * <p>
 * A query parameter 'redirect' is accepted on the GET. If it is present, the
 * service will respond with a redirect to the provided URI. This way, the
 * client can make sure that the user is lead back to the front end in the
 * desired state. Passing the 'redirect' query parameter in the POST response
 * will cause it to be preserved in the redirect to the GET. To execute a
 * 'login' action, a front end application will therefore typically send a POST
 * to {@code <SERVICE_BASE_URI>/authentication?redirect=<FRONT_END_URI>}.</p>
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
@Service
public class AuthenticationRestService implements IAuthenticationRestService {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationRestService.class);

    @Autowired(required = true)
    private Configuration configuration;
    @Autowired(required = true)
    private UserDao userDao;

    @Override
    public Response getAuthenticationInformation(String redirectUri, UriInfo uriInfo, SecurityContext security) throws JSONException, AuthenticationRequiredException {
        logger.trace("Authentication information requested. Security context {}. Redirect URI: '{}'", security, redirectUri);

        final Principal userPrincipal = security.getUserPrincipal();
        logger.trace("User principal: {}", userPrincipal);

        final AuthenticationInfo authInfo;
        if (userPrincipal == null) {
            logger.trace("Unauthenticated (userPrincipal == null)");
            authInfo = new AuthenticationInfo(false);
        } else if (userPrincipal.getName() == null || userPrincipal.getName().isEmpty() || ComponentRegistryFactory.ANONYMOUS_USER.equals(userPrincipal.getName())) {
            logger.trace("User principal set but no user name ({}): {}", userPrincipal.getName(), userPrincipal);
            authInfo = new AuthenticationInfo(false);
        } else {
            final UserCredentials credentials = new UserCredentials(userPrincipal);
            final RegistryUser user = userDao.getByPrincipalName(userPrincipal.getName());

            final Long id;
            if (user == null) {
                logger.trace("Unregistered user {}", userPrincipal.getName());
                id = null;
            } else {
                id = user.getId();
            }
            authInfo = new AuthenticationInfo(credentials, id, configuration.isAdminUser(userPrincipal));
        }

        logger.trace("Authentication info: {}", authInfo);

        if (Strings.isNullOrEmpty(redirectUri)) {
            return Response.ok(authInfo).build();
        } else {
            return Response.seeOther(URI.create(redirectUri)).entity(authInfo).build();
        }
    }

    @Override
    public Response triggerAuthenticationRequest(UriInfo uriInfo, SecurityContext security) {
        logger.debug("Client has triggered authentication request {} -> {}", security.getUserPrincipal(), uriInfo.getRequestUri());

        //done - redirect to GET
        return Response.seeOther(uriInfo.getRequestUri()).build();
    }
}
