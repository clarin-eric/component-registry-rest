package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.model.Group;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.spi.resource.Singleton;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.stereotype.Service;
import clarin.cmdi.componentregistry.GroupService;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
@Path("/groups")
@Service
@Singleton
@Api(value = "/groups", produces = MediaType.APPLICATION_XML)
public class GroupsService extends AbstractComponentRegistryRestService {

    @InjectParam(value = "GroupService")
    private GroupService groupService;

    @GET
    @Path("/usermembership")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Returns a listing of groups the current user is a member of (empty list when unauthenticated)")
    public List<Group> getGroupsTheCurrentUserIsAMemberOf() {
        Principal principal = security.getUserPrincipal();
        if (principal == null) {
            return new ArrayList<>();
        }
        List<Group> groups = groupService.getGroupsOfWhichUserIsAMember(principal.getName());
        return groups;
    }
}
