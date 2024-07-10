package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.AuthenticationRequiredException;
import clarin.cmdi.componentregistry.model.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ItemIsLockedException;
import clarin.cmdi.componentregistry.ItemLockService;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ComponentStatus;
import clarin.cmdi.componentregistry.model.Group;
import clarin.cmdi.componentregistry.model.ItemLock;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.GROUPID_PARAM;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.spi.resource.Singleton;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import clarin.cmdi.componentregistry.GroupService;
import clarin.cmdi.componentregistry.model.ItemRights;
import com.google.common.base.Strings;
import java.util.Optional;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
@Path("/items")
@Service
@Singleton
@Api(value = "/items", produces = MediaType.APPLICATION_XML)
public class ItemsService extends AbstractComponentRegistryRestService {

    private final static Logger logger = LoggerFactory.getLogger(ItemsService.class);

    @Context
    private HttpServletResponse servletResponse;

    @InjectParam(value = "GroupService")
    private GroupService groupService;

    @InjectParam(value = "ItemLockService")
    private ItemLockService itemLockService;

    @GET
    @Path("/{itemId}")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    @ApiOperation(value = "The description (metadata) of a single component or profile item")
    @ApiResponses(value = {
        @ApiResponse(code = 401, message = "Item requires authorisation and user is not authenticated"),
        @ApiResponse(code = 403, message = "Non-public item is not owned by current user and user is no administrator"),
        @ApiResponse(code = 404, message = "Item does not exist")
    })
    public BaseDescription getBaseDescription(@PathParam("itemId") String itemId) throws ComponentRegistryException, IOException {
        logger.debug("Item with id: {} is requested.", itemId);
        try {
            final BaseDescription description = getBaseDescriptionOrSendError(itemId, servletResponse);
            if (description == null) {
                servletResponse.sendError(Response.Status.NOT_FOUND.getStatusCode(), "No such item: " + itemId);
            } else {
                return description;
            }
        } catch (ComponentRegistryException ex) {
            logger.warn("Failed to get description (error response set): {}", ex.getMessage());
            logger.debug("Failed to get description", ex);
        }
        return new BaseDescription();
    }

    @GET
    @Path("/{itemId}/groups")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns a listing of groups to which an item belongs")
    public List<Group> getGroupsTheItemIsAMemberOf(@PathParam("itemId") String itemId) {
        return groupService.getGroupsTheItemIsAMemberOf(itemId);
    }

    @GET
    @Path("/{itemId}/rights")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns information about the rights to an item for the current user")
    public ItemRights getItemRights(@PathParam("itemId") String itemId) throws ComponentRegistryException, IOException {
        final Principal principal = security.getUserPrincipal();
        try {
            final BaseDescription item = getItemDescriptionAccesControled(itemId);
            return itemRightsForPrincipal(principal, item);
        } catch (UserUnauthorizedException | AuthenticationRequiredException ex) {
            return new ItemRights(itemId, principal == null ? "" : principal.getName(), false, false);
        } catch (ItemNotFoundException ex) {
            servletResponse.sendError(Response.Status.NOT_FOUND.getStatusCode(), "No such item");
            return null;
        } catch (ComponentRegistryException ex) {
            logger.warn("Failed to get description on access information request: {}", ex.getMessage());
            logger.debug("Failed to get description", ex);
            throw ex;
        }
    }

    private ItemRights itemRightsForPrincipal(final Principal principal, final BaseDescription item) {
        final String principalName = Optional.ofNullable(principal).map(Principal::getName).orElse("");

        boolean canRead;
        boolean canWrite;
        if (isOwnerOrInOwningGroup(principal, item)) {
            //(group) owner can always read
            canRead = true;
            //(group) owner can edit while in development status
            canWrite = item.getStatus() == ComponentStatus.DEVELOPMENT;
        } else {
            canRead = item.isPublic();
            canWrite = false;
        }
        return new ItemRights(item.getId(), principalName, canRead, canWrite);
    }

    private boolean isOwnerOrInOwningGroup(Principal principal, BaseDescription item) {
        if (principal == null) {
            //no principal -> no ownership
            return false;
        } else {
            return groupService.isUserOwnerEitherOnHisOwnOrThroughGroupMembership(principal.getName(), item);
        }
    }

    @POST
    @Path("/{itemId}/transferownership")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Transfers an item to the specified group (either from the private space or another group)")
    @ApiResponses(value = {
        @ApiResponse(code = 403, message = "Current user has no access")
    })
    public Response transferItemOwnershipToGroup(@PathParam("itemId") String itemId,
            @QueryParam(GROUPID_PARAM) long groupId) throws IOException {
        Principal principal = security.getUserPrincipal();
        try {
            groupService.transferItemOwnershipFromUserToGroupId(principal.getName(), groupId, itemId);
            return Response.ok("Ownership transferred").build();
        } catch (UserUnauthorizedException e) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @GET
    @Path("/{itemId}/lock")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getItemLock(@PathParam("itemId") String itemId) {
        final ItemLock lock = itemLockService.getLock(itemId);
        if (lock == null) {
            return Response.noContent().status(Status.NOT_FOUND).build();
        } else {
            return Response.ok(lock).build();
        }
    }

    @PUT
    @Path("/{itemId}/lock")
    public Response putItemLock(@PathParam("itemId") String itemId) throws ComponentRegistryException, IOException {
        //test item accessibility
        try {
            //first check whether authenticated
            checkAndGetUserPrincipal();

            final BaseDescription itemDescription = getBaseDescriptionOrSendError(itemId, servletResponse);
            if (itemDescription == null) {
                throw serviceException(Status.CONFLICT, "Request to put lock on item that does not exist: " + itemId);
            } else if (itemDescription.getStatus() != ComponentStatus.DEVELOPMENT) {
                throw serviceException(Status.FORBIDDEN, "Only development items can be (un)locked");
            }
        } catch (AuthenticationRequiredException ex) {
            throw serviceException(Status.UNAUTHORIZED, "Removing a lock requires authentication");
        } catch (ComponentRegistryException ex) {
            logger.warn("Failed to get item for which locking was requested (error response set): {}", ex.getMessage());
            logger.debug("Failed to get item for which locking was requested", ex);
            return Response.noContent().build();
        }

        //create lock
        try {
            final ItemLock createdLock = itemLockService.setLock(itemId, security.getUserPrincipal().getName());
            if (createdLock != null) {
                logger.debug("Item lock set: {} by {}", itemId, security.getUserPrincipal().getName());
                return Response.ok(createdLock).build();
            } else {
                throw new ComponentRegistryException("Lock was not created for an unkown reason");
            }
        } catch (ItemIsLockedException ex) {
            logger.warn("Attempt to lock item that is already locked: {}", itemId);
            servletResponse.sendError(Status.CONFLICT.getStatusCode(), "The item is already locked: " + itemId);
            return Response.noContent().build();
        }
    }

    @DELETE
    @Path("/{itemId}/lock")
    public Response removeItemLock(@PathParam("itemId") String itemId) throws ComponentRegistryException, IOException {
        //test item accessibility
        try {
            //first check whether authenticated
            checkAndGetUserPrincipal();

            final BaseDescription itemDescription = getBaseDescriptionOrSendError(itemId, servletResponse);
            if (itemDescription == null) {
                throw serviceException(Status.NOT_FOUND, "Request to remove lock of item that does not exist: " + itemId);
            } else if (itemDescription.getStatus() != ComponentStatus.DEVELOPMENT) {
                throw serviceException(Status.FORBIDDEN, "Only development items can be (un)locked");
            }
        } catch (AuthenticationRequiredException ex) {
            throw serviceException(Status.UNAUTHORIZED, "Removing a lock requires authentication");
        } catch (ComponentRegistryException ex) {
            logger.warn("Failed to get item for which lock removal was requested (error response set): {}", ex.getMessage());
            logger.debug("Failed to get item for which lock removal was requested", ex);
            return Response.noContent().build();
        }

        //delete lock
        itemLockService.deleteLock(itemId);
        logger.debug("Item lock removed: {} by {}", itemId, security.getUserPrincipal().getName());
        return Response.ok().build();
    }

    /**
     * Tries to get the description of the identified item, checking for
     * authorisation. If the retrieval fails for one of a number of reasons, an
     * error is set on the response and a ComponentRegistryException is thrown.
     * If the item is not found, null is returned.
     *
     * @param itemId item to try to retrieve
     * @param response servlet response that errors can be sent to
     * @return base description only if it could be retrieved, null if it was
     * not found
     * @throws ComponentRegistryException
     */
    private BaseDescription getBaseDescriptionOrSendError(String itemId, HttpServletResponse response) throws ComponentRegistryException, IOException {
        try {
            BaseDescription description = getItemDescriptionAccesControled(itemId);
            if (description != null) {
                return description;
            } else {
                // null signifies a not well-formed identifier
                response.sendError(Response.Status.BAD_REQUEST.getStatusCode(), "Bad ID");
                throw new ComponentRegistryException("No item:" + itemId);
            }
        } catch (ItemNotFoundException e) {
            return null;
        } catch (UserUnauthorizedException ex2) {
            response.sendError(Response.Status.FORBIDDEN.getStatusCode(), ex2.getMessage());
            throw new ComponentRegistryException("Forbidden:" + itemId);
        } catch (AuthenticationRequiredException e) {
            response.sendError(Response.Status.UNAUTHORIZED.getStatusCode(), e.toString());
            throw new ComponentRegistryException("Unauthorized:" + itemId);
        }
    }

    /**
     *
     * @param itemId
     * @return description, if the description ID is well-formed
     * @throws UserUnauthorizedException if the user does not have access
     * @throws AuthenticationRequiredException if authorization is required
     * @throws ItemNotFoundException if the description ID is well-formed but
     * the item could not be found
     * @throws ComponentRegistryException an error occurred while getting the
     * description from the registry
     */
    private BaseDescription getItemDescriptionAccesControled(String itemId) throws UserUnauthorizedException, AuthenticationRequiredException, ItemNotFoundException, ComponentRegistryException {
        final ComponentRegistry cr = this.getBaseRegistry();
        if (itemId.startsWith(ComponentDescription.COMPONENT_PREFIX)) {
            return cr.getComponentDescriptionAccessControlled(itemId);
        } else if (itemId.startsWith(ProfileDescription.PROFILE_PREFIX)) {
            return cr.getProfileDescriptionAccessControlled(itemId);
        } else {
            return null;
        }
    }
}
