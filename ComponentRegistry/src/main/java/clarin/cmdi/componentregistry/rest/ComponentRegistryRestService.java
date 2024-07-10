package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.validation.ComponentSpecValidator;
import clarin.cmdi.componentregistry.validation.DescriptionValidator;
import clarin.cmdi.componentregistry.validation.Validator;
import clarin.cmdi.componentregistry.CmdVersion;
import clarin.cmdi.componentregistry.AllowedAttributetypesXML;
import clarin.cmdi.componentregistry.AuthenticationRequiredException;
import static clarin.cmdi.componentregistry.CmdVersion.CANONICAL_CMD_VERSION;
import clarin.cmdi.componentregistry.model.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.ComponentSpecConverter;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.RegistrySpace;
import clarin.cmdi.componentregistry.ShhaaUserCredentials;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.ComponentSpec;
import clarin.cmdi.componentregistry.ComponentUtils;
import clarin.cmdi.componentregistry.impl.database.ValidationException;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.CommentResponse;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ComponentRegistryResponse;
import clarin.cmdi.componentregistry.model.ComponentStatus;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.APPLICATION_URL_BASE_PARAM;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.APPLICATION_URL_HOST_HEADER_PARAM;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.APPLICATION_URL_PATH_PARAM;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.APPLICATION_URL_PROTOCOL_HEADER_PARAM;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.DATA_FORM_FIELD;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.DESCRIPTION_FORM_FIELD;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.DOMAIN_FORM_FIELD;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.GROUPID_PARAM;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.GROUP_FORM_FIELD;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.METADATA_EDITOR_PARAM;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.NAME_FORM_FIELD;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.NUMBER_OF_RSSITEMS;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.REGISTRY_SPACE_GROUP;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.REGISTRY_SPACE_PARAM;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.REGISTRY_SPACE_PRIVATE;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.REGISTRY_SPACE_PUBLISHED;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.USER_SPACE_PARAM;
import clarin.cmdi.componentregistry.rss.Rss;
import clarin.cmdi.componentregistry.rss.RssCreatorComments;
import clarin.cmdi.componentregistry.rss.RssCreatorDescriptions;
import clarin.cmdi.componentregistry.validation.CommentValidator;
import clarin.cmdi.componentregistry.validation.ExpandedSpecValidator;
import clarin.cmdi.componentregistry.validation.RecursionDetector;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.resource.Singleton;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.security.Principal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles CRUD operations on
 * {@link ComponentDescription}, {@link ProfileDescription} and {@link Comment}s
 *
 * @author twago@mpi.nl
 * @author olsha@mpi.nl
 * @author george.georgovassilis@mpi.nl
 *
 */
@Path("/")
@Api(value = "/", description = "Rest API for the CMDI Component Registry", produces = MediaType.APPLICATION_XML)
@Service
@Singleton
public class ComponentRegistryRestService {

    public static final String APPLICATION_URL_BASE_PARAM = "eu.clarin.cmdi.componentregistry.serviceUrlBase";
    public static final String APPLICATION_URL_PATH_PARAM = "eu.clarin.cmdi.componentregistry.serviceUrlPath";
    public static final String APPLICATION_URL_PROTOCOL_HEADER_PARAM = "eu.clarin.cmdi.componentregistry.serviceUrlProtocolHeader";
    public static final String APPLICATION_URL_HOST_HEADER_PARAM = "eu.clarin.cmdi.componentregistry.serviceUrlHostHeader";
    public static final String DATA_FORM_FIELD = "data";
    public static final String NAME_FORM_FIELD = "name";
    public static final String DESCRIPTION_FORM_FIELD = "description";
    public static final String GROUP_FORM_FIELD = "group";
    public static final String DOMAIN_FORM_FIELD = "domainName";
    public static final String STATUS_FORM_FIELD = "status";
    public static final String SUCCESSOR_ID_FORM_FIELD = "successor";
    public static final String REGISTRY_SPACE_PARAM = "registrySpace";
    public static final String USER_SPACE_PARAM = "userspace";
    public static final String GROUPID_PARAM = "groupId";
    public static final String METADATA_EDITOR_PARAM = "mdEditor";
    public static final String STATUS_FILTER_PARAM = "status";
    public static final String NUMBER_OF_RSSITEMS = "limit";

    public static final String REGISTRY_SPACE_PUBLISHED = "published";
    public static final String REGISTRY_SPACE_PRIVATE = "private";
    public static final String REGISTRY_SPACE_GROUP = "group";

    private final static Logger LOG = LoggerFactory
            .getLogger(ComponentRegistryRestService.class);

    /**
     * Requests a registry service for a specific version of CMDI
     *
     * Allowed versions in path: 1.1, 1.2, 1.x
     *
     * @return RegistryService resource with the requested service
     */
    @Path("/registry/{cmdVersion: 1\\.[12x]}")
    @ApiOperation(value = "Registry for a CMDI version")
    public Class<RegistryService> versionService() {
        return RegistryService.class;
    }

    /**
     * Requests a registry service for the default version of CMDI
     *
     * @return RegistryService resource for default version
     */
    @Path("/registry")
    @ApiOperation(value = "Registry for the default CMDI version")
    public Class<RegistryService> defaultVersionService() {
        return RegistryService.class;
    }

    @GET
    @Path("/allowedTypes")
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    @ApiOperation(
            value = "A listing of values that are allowed as element or attribute type by the CMDI schema",
            response = AllowedAttributetypesXML.class)
    public AllowedAttributetypesXML getAllowedAttributeTypes()
            throws ComponentRegistryException, IOException, JAXBException,
            ParseException {
        return (new AllowedAttributetypesXML());
    }

    /**
     * Subresource for a registry service
     */
    @Transactional(rollbackFor = {Exception.class, ValidationException.class})
    @Api(value = "/registry", description = "Profiles and component registry", produces = MediaType.APPLICATION_XML)
    public static class RegistryService extends AbstractComponentRegistryRestService {

        @PathParam("cmdVersion")
        @DefaultValue("1.1")
        private String cmdVersion;

        @Context
        private UriInfo uriInfo;
        @Context
        private HttpServletRequest servletRequest;
        @Context
        private HttpServletResponse servletResponse;
        @Context
        private ServletContext servletContext;
        @InjectParam(value = "mdMarshaller")
        private MDMarshaller marshaller;
        @InjectParam(value = "ComponentSpecConverter")
        private ComponentSpecConverter componentSpecConverter;

        private CmdVersion getCmdVersion() {
            final CmdVersion version = getCmdVersion(cmdVersion);
            if (version != null) {
                return version;
            } else {
                return CmdVersion.CMD_1_1;
            }
        }

        private static CmdVersion getCmdVersion(String versionString) {
            if (versionString != null) {
                switch (versionString) {
                    case "1.1":
                        return CmdVersion.CMD_1_1;
                    case "1.2":
                        return CmdVersion.CMD_1_2;
                    case "1.x":
                        //latest version
                        return CmdVersion.CMD_1_2;
                }
                // default: fall through, same as null case
            }
            // also in case of null: 'default' registry
            return null;
        }

        private ComponentRegistry getRegistry(RegistrySpace space, Number groupId) {
            Principal userPrincipal = security.getUserPrincipal();
            ShhaaUserCredentials userCredentials = this.getUserCredentials(userPrincipal);
            try {
                return componentRegistryFactory.getComponentRegistry(space, null, userCredentials, groupId);
            } catch (UserUnauthorizedException uuEx) {
                LOG.warn("Unauthorized access to {} registry by user {}", space,
                        userCredentials);
                LOG.debug("Details for unauthorized access", uuEx);
                throw new WebApplicationException(uuEx, Status.FORBIDDEN);
            }
        }
        /**
         *
         * @return Principal of current request
         * @throws AuthenticationRequiredException If no user principal found
         */

        private ComponentRegistry initialiseRegistry(String space, String groupId) throws AuthenticationRequiredException {
            //checking credentials 
            RegistrySpace regSpace = RegistrySpace.valueOf(space.toUpperCase());
            if (regSpace != RegistrySpace.PUBLISHED) {
                // ensure that user is authenticated
                this.getUserCredentials(this.checkAndGetUserPrincipal());
            }
            // initializing the registry
            Number groupIdNumber = null;
            if (groupId != null && !groupId.isEmpty()) {
                groupIdNumber = Integer.parseInt(groupId);
            }

            return this.getRegistry(regSpace, groupIdNumber);
        }

        private boolean checkRegistrySpaceString(String registrySpace) {
            return (registrySpace.equalsIgnoreCase(REGISTRY_SPACE_GROUP) || registrySpace.equalsIgnoreCase(REGISTRY_SPACE_PRIVATE) || registrySpace.equalsIgnoreCase(REGISTRY_SPACE_PUBLISHED));
        }

        @GET
        @Path("/components")
        @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON})
        @ApiOperation(value = "A listing of the descriptions of components in the specified registry space")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Registry requires authorisation and user is not authenticated"),
            @ApiResponse(code = 403, message = "Non-public registry is not owned by current user"),
            @ApiResponse(code = 404, message = "Registry space does not exist")
        })
        public List<ComponentDescription> getRegisteredComponents(
                @QueryParam(REGISTRY_SPACE_PARAM) @DefaultValue(REGISTRY_SPACE_PUBLISHED) String registrySpace,
                @QueryParam(GROUPID_PARAM) String groupId,
                @QueryParam(STATUS_FILTER_PARAM) List<String> status,
                @Deprecated @QueryParam(USER_SPACE_PARAM) @DefaultValue("") String userSpace)
                throws ComponentRegistryException, IOException {
            long start = System.currentTimeMillis();

            // deprecated parameter, processed here for backwards compatibility
            if (!Strings.isNullOrEmpty(userSpace)) {
                LOG.warn("Usage of deprecated {} parameter", USER_SPACE_PARAM);
                if (Boolean.valueOf(userSpace)) {
                    registrySpace = REGISTRY_SPACE_PRIVATE;
                }
            }
            final Set<ComponentStatus> statusFilter = getStatusFilter(status, registrySpace);

            if (!checkRegistrySpaceString(registrySpace)) {
                throw serviceException(Status.NOT_FOUND, "illegal registry space");
            }

            try {
                final ComponentRegistry cr = this.initialiseRegistry(registrySpace, groupId);
                final List<ComponentDescription> result = cr.getComponentDescriptions(statusFilter);
                //insert hrefs in descriptions
                fillHrefs(result);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                            "Releasing {} registered components into the world ({} millisecs)",
                            result.size(), (System.currentTimeMillis() - start));
                }

                return result;
            } catch (AuthenticationRequiredException e) {
                throw serviceException(Status.UNAUTHORIZED, e.toString());

            } catch (UserUnauthorizedException e) {
                throw serviceException(Status.FORBIDDEN, e.toString());

            } catch (ItemNotFoundException e) {
                throw serviceException(Status.NOT_FOUND, e.toString());
            }

        }

        @GET
        @Path("/profiles")
        @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON})
        @ApiOperation(value = "A listing of the descriptions of profiles in the specified registry space")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Registry requires authorisation and user is not authenticated"),
            @ApiResponse(code = 403, message = "Non-public registry is not owned by current user"),
            @ApiResponse(code = 404, message = "Registry space does not exist")
        })
        public List<ProfileDescription> getRegisteredProfiles(
                @QueryParam(REGISTRY_SPACE_PARAM) @DefaultValue(REGISTRY_SPACE_PUBLISHED) String registrySpace,
                @QueryParam(METADATA_EDITOR_PARAM) @DefaultValue("false") boolean metadataEditor,
                @QueryParam(GROUPID_PARAM) String groupId,
                @QueryParam(STATUS_FILTER_PARAM) List<String> status,
                @Deprecated @QueryParam(USER_SPACE_PARAM) @DefaultValue("") String userSpace
        )
                throws ComponentRegistryException, IOException {

            long start = System.currentTimeMillis();

            // deprecated parameter, processed here for backwards compatibility
            if (!Strings.isNullOrEmpty(userSpace)) {
                LOG.warn("Usage of deprecated {} parameter", USER_SPACE_PARAM);
                if (Boolean.valueOf(userSpace)) {
                    registrySpace = REGISTRY_SPACE_PRIVATE;
                }
            }
            final Set<ComponentStatus> statusFilter = getStatusFilter(status, registrySpace);

            if (!checkRegistrySpaceString(registrySpace)) {
                throw serviceException(Status.NOT_FOUND, "illegal registry space");
            }
            try {
                final ComponentRegistry cr = this.initialiseRegistry(registrySpace, groupId);
                final List<ProfileDescription> result
                        = (metadataEditor) ? cr.getProfileDescriptionsForMetadaEditor(statusFilter)
                                : cr.getProfileDescriptions(statusFilter);
                //insert hrefs in descriptions
                fillHrefs(result);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                            "Releasing {} registered components into the world ({} millisecs)",
                            result.size(), (System.currentTimeMillis() - start));
                }

                return result;
            } catch (AuthenticationRequiredException e) {
                throw serviceException(Status.UNAUTHORIZED, e.toString());
            } catch (UserUnauthorizedException e) {
                throw serviceException(Status.FORBIDDEN, e.toString());
            } catch (ItemNotFoundException e) {
                throw serviceException(Status.NOT_FOUND, e.toString());
            }
        }

        @GET
        @Path("/components/{componentId}")
        @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON})
        @ApiOperation(value = "The component specification of a single component")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Item requires authorisation and user is not authenticated"),
            @ApiResponse(code = 403, message = "Non-public item is not owned by current user"),
            @ApiResponse(code = 404, message = "Item does not exist")
        })
        public Response getRegisteredComponent(
                @PathParam("componentId") String componentId) throws IOException {
            LOG.debug("Component with id: {} is requested.", componentId);
            try {
                ComponentSpec mdComponent = this.getBaseRegistry().getMDComponentAccessControlled(componentId);
                return createComponentSpecResponse(mdComponent);
            } catch (ItemNotFoundException e) {
                return Response.status(Status.NOT_FOUND).build();
            } catch (ComponentRegistryException e1) {
                return Response.serverError().status(Status.CONFLICT).build();
            } catch (AuthenticationRequiredException e) {
                return Response.serverError().status(Status.UNAUTHORIZED).build();
            } catch (UserUnauthorizedException e) {
                return Response.serverError().status(Status.FORBIDDEN).build();
            }
        }

        private Response createComponentSpecResponse(ComponentSpec mdProfile) throws IOException {
            final CmdVersion registryVersion = getCmdVersion();
            if (CANONICAL_CMD_VERSION != registryVersion) {
                //TODO: only if accepting XML - check!

                // get XML representation of original to serve as input for conversion
                byte[] originalByes = null;
                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    marshaller.marshal(mdProfile, os);
                    originalByes = os.toByteArray();
                } catch (JAXBException ex) {
                    throw new RuntimeException("Failed to marshal component before conversion", ex);
                }

                // perform conversion
                final StringWriter resultWriter = new StringWriter();
                try (InputStream is = new ByteArrayInputStream(originalByes)) {
                    componentSpecConverter.convertComponentSpec(CANONICAL_CMD_VERSION, registryVersion, is, resultWriter);
                }

                // handle result
                final String result = resultWriter.toString();
                if (result == null || result.isEmpty()) {
                    return Response
                            .status(Status.BAD_REQUEST)
                            .entity("Cannot convert spec from " + CANONICAL_CMD_VERSION + " to " + registryVersion)
                            .type(MediaType.TEXT_PLAIN)
                            .build();
                } else {
                    return Response.ok(result).build();
                }
            } else {
                return Response.ok(mdProfile).build();
            }
        }

        @GET
        @Path("/profiles/{profileId}")
        @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON})
        @ApiOperation(value = "The component specification of a single profile")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Item requires authorisation and user is not authenticated"),
            @ApiResponse(code = 403, message = "Non-public item is not owned by current user"),
            @ApiResponse(code = 404, message = "Item does not exist")
        })
        public Response getRegisteredProfile(
                @PathParam("profileId") String profileId) throws IOException {
            LOG.debug("Profile with id {} is requested.", profileId);
            try {
                ComponentSpec mdProfile = this.getBaseRegistry().getMDProfileAccessControled(profileId);
                return createComponentSpecResponse(mdProfile);
            } catch (ItemNotFoundException e) {
                return Response.status(Status.NOT_FOUND).build();
            } catch (ComponentRegistryException e1) {
                return Response.serverError().status(Status.CONFLICT).build();
            } catch (UserUnauthorizedException e) {
                return Response.serverError().status(Status.FORBIDDEN).build();
            } catch (AuthenticationRequiredException e) {
                return Response.serverError().status(Status.UNAUTHORIZED).build();
            }
        }

        @GET
        @Path("/components/{componentId}/{rawType}")
        @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
        @ApiOperation(value = "The expanded XML or XSD represenation of the component specification of a single component (publicly accessible regardless of state!)")
        @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Item does not exist")
        })
        public Response getRegisteredComponentRawType(
                @PathParam("componentId") final String componentId, @PathParam("rawType") String rawType) throws ComponentRegistryException {

            LOG.debug("Component with id: {} and rawType: {} is requested.", componentId, rawType);
            try {
                final ComponentRegistry registry = this.getBaseRegistry();
                ComponentDescription desc = registry.getComponentDescription(componentId);
                StreamingOutput result;
                String fileName = desc.getName() + "." + rawType;
                if ("xml".equalsIgnoreCase(rawType)) {
                    result = new StreamingOutput() {

                        @Override
                        public void write(OutputStream output) throws IOException,
                                WebApplicationException {
                            try {
                                try {
                                    try {
                                        registry.getMDComponentAsXml(componentId, getCmdVersion(), output); //TODO: function of version number
                                    } catch (ItemNotFoundException e) {
                                        LOG.warn("Could not retrieve component {}: {}",
                                                componentId, e.getMessage());
                                        LOG.debug("Details", e);
                                        throw new WebApplicationException(Response
                                                .serverError()
                                                .status(Status.INTERNAL_SERVER_ERROR)
                                                .build());
                                    }
                                } catch (ComponentRegistryException e) {
                                    LOG.warn("Could not retrieve component {}: {}",
                                            componentId, e.getMessage());
                                    LOG.debug("Details", e);
                                    throw new WebApplicationException(Response
                                            .serverError()
                                            .status(Status.INTERNAL_SERVER_ERROR)
                                            .build());
                                }

                            } catch (UserUnauthorizedException e2) {
                                LOG.error(e2.toString());
                            }
                        }
                    };
                    return createDownloadResponse(result, fileName);
                } else {
                    //other 'raw types', including xsd are not supported
                    return Response.status(Status.NOT_FOUND).entity("Usupported raw type " + rawType).build();
                }
            } catch (ItemNotFoundException e3) {
                return Response.status(Status.NOT_FOUND).build();
            } catch (AuthenticationRequiredException e) {
                return Response.serverError().status(Status.UNAUTHORIZED).build();
            }
        }

        @GET
        @Path("/components/usage/{componentId}")
        @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON})
        @ApiOperation(value = "Returns a descriptions listing of components that use the identified component")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Item requires authorisation and user is not authenticated")
        })
        public List<BaseDescription> getComponentUsage(
                @PathParam("componentId") String componentId) throws ComponentRegistryException, IOException {

            final long start = System.currentTimeMillis();
            try {
                ComponentRegistry registry = this.getBaseRegistry();
                List<ComponentDescription> components = registry.getUsageInComponents(componentId);
                List<ProfileDescription> profiles = registry.getUsageInProfiles(componentId);

                LOG.debug(
                        "Found {} components and {} profiles that use component {} ({} millisecs)",
                        components.size(), profiles.size(), componentId,
                        (System.currentTimeMillis() - start));

                List<BaseDescription> usages = new ArrayList<>(components.size() + profiles.size());
                usages.addAll(components);
                usages.addAll(profiles);

                return usages;
            } catch (ComponentRegistryException e) {
                LOG.warn("Could not retrieve profile usage {}", componentId);
                LOG.debug("Details", e);
                return Collections.emptyList();
            } catch (AuthenticationRequiredException e1) {
                servletResponse.sendError(Status.UNAUTHORIZED.getStatusCode());
                return Collections.emptyList();
            }
        }

        @GET
        @Path("/profiles/{profileId}/comments")
        @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON})
        @ApiOperation(value = "Returns a listing of all comments that have been made on the identified profile")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Item requires authorisation and user is not authenticated"),
            @ApiResponse(code = 403, message = "Non-public item is not owned by current user and user is no administrator"),
            @ApiResponse(code = 404, message = "Item does not exist")
        })
        public List<Comment> getCommentsFromProfile(
                @PathParam("profileId") String profileId)
                throws IOException {
            long start = System.currentTimeMillis();
            try {
                List<Comment> comments = this.getBaseRegistry().getCommentsInProfile(profileId);
                LOG.debug(
                        "Releasing {} registered comments in profile into the world ({} millisecs)",
                        comments.size(), (System.currentTimeMillis() - start));
                return comments;
            } catch (ComponentRegistryException e) {
                servletResponse.sendError(Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());
                return Collections.emptyList();
            } catch (ItemNotFoundException e) {
                servletResponse.sendError(Status.NOT_FOUND.getStatusCode(), e.getMessage());
                return Collections.emptyList();
            } catch (UserUnauthorizedException e) {
                servletResponse.sendError(Status.FORBIDDEN.getStatusCode(), e.getMessage());
                return Collections.emptyList();
            } catch (AuthenticationRequiredException e1) {
                servletResponse.sendError(Status.UNAUTHORIZED.getStatusCode(), e1.getMessage());
                return Collections.emptyList();
            }
        }

        @GET
        @Path("/components/{componentId}/comments")
        @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON})
        @ApiOperation(value = "Returns a listing of all comments that have been made on the identified component")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Item requires authorisation and user is not authenticated"),
            @ApiResponse(code = 403, message = "Non-public item is not owned by current user and user is no administrator"),
            @ApiResponse(code = 404, message = "Item does not exist")
        })
        public List<Comment> getCommentsFromComponent(
                @PathParam("componentId") String componentId)
                throws IOException {
            long start = System.currentTimeMillis();
            try {
                List<Comment> comments = this.getBaseRegistry().getCommentsInComponent(componentId);
                LOG.debug(
                        "Releasing {} registered comments in Component into the world ({} millisecs)",
                        comments.size(), (System.currentTimeMillis() - start));
                return comments;
            } catch (ComponentRegistryException e) {
                servletResponse.sendError(Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());
                return Collections.emptyList();
            } catch (ItemNotFoundException e) {
                servletResponse.sendError(Status.NOT_FOUND.getStatusCode(), e.getMessage());
                return Collections.emptyList();
            } catch (UserUnauthorizedException e1) {
                servletResponse.sendError(Status.FORBIDDEN.getStatusCode(), e1.getMessage());
                return Collections.emptyList();
            } catch (AuthenticationRequiredException e1) {
                servletResponse.sendError(Status.UNAUTHORIZED.getStatusCode(), e1.getMessage());
                return Collections.emptyList();
            }
        }

        @GET
        @Path("/profiles/{profileId}/comments/{commentId}")
        @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON})
        @ApiOperation(value = "Returns a single comment on a profile")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Component requires authorisation and user is not authenticated"),
            @ApiResponse(code = 403, message = "Non-public component is not owned by current user"),
            @ApiResponse(code = 404, message = "Component or comment does not exist")
        })
        public Comment getSpecifiedCommentFromProfile(
                @PathParam("profileId") String profileId,
                @PathParam("commentId") String commentId)
                throws IOException {

            LOG.debug("Comments of profile with id {} are requested.", commentId);
            try {

                return this.getBaseRegistry().getSpecifiedCommentInProfile(profileId, commentId);
            } catch (ComponentRegistryException e) {
                servletResponse.sendError(Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());
                return new Comment();
            } catch (ItemNotFoundException e) {
                servletResponse.sendError(Status.NOT_FOUND.getStatusCode(), e.getMessage());
                return new Comment();
            } catch (UserUnauthorizedException e1) {
                servletResponse.sendError(Status.FORBIDDEN.getStatusCode(), e1.getMessage());
                return new Comment();
            } catch (AuthenticationRequiredException e1) {
                servletResponse.sendError(Status.UNAUTHORIZED.getStatusCode(), e1.getMessage());
                return new Comment();
            }
        }

        @GET
        @Path("/components/{componentId}/comments/{commentId}")
        @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON})
        @ApiOperation(value = "Returns a single comment on a component")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Component requires authorisation and user is not authenticated"),
            @ApiResponse(code = 403, message = "Non-public component is not owned by current user"),
            @ApiResponse(code = 404, message = "Component or comment does not exist")
        })
        public Comment getSpecifiedCommentFromComponent(
                @PathParam("componentId") String componentId,
                @PathParam("commentId") String commentId)
                throws IOException {
            LOG.debug("Comments of component with id {} are requested.", commentId);
            try {
                Comment result = this.getBaseRegistry().getSpecifiedCommentInComponent(componentId, commentId);
                return result;
            } catch (ComponentRegistryException e) {
                servletResponse.sendError(Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());
                return new Comment();
            } catch (ItemNotFoundException e) {
                servletResponse.sendError(Status.NOT_FOUND.getStatusCode(), e.getMessage());
                return new Comment();
            } catch (UserUnauthorizedException e1) {
                servletResponse.sendError(Status.FORBIDDEN.getStatusCode(), e1.getMessage());
                return new Comment();
            } catch (AuthenticationRequiredException e1) {
                servletResponse.sendError(Status.UNAUTHORIZED.getStatusCode(), e1.getMessage());
                return new Comment();
            }
        }

        /**
         *
         * Purely helper method for my front-end (FLEX) which only does post/get
         * requests. The query param is checked and the "proper" method is
         * called.
         *
         * @param profileId
         * @param method
         * @return
         */
        // TODO: test via POSTMAN
        @POST
        @Path("/profiles/{profileId}")
        @ApiOperation(value = "Allows for deletion of single profile (workaround for Flex which does not support the DELETE method)")
        public Response manipulateRegisteredProfile(
                @PathParam("profileId") String profileId,
                @FormParam("method") String method) {
            if ("delete".equalsIgnoreCase(method)) {
                return this.deleteRegisteredProfile(profileId);
            } else {
                return Response.ok().build();
            }
        }

        @POST
        @Path("/profiles/{profileId}/comments/{commentId}")
        @ApiOperation(value = "Allows for deletion of single profile comment (workaround for Flex which does not support the DELETE method)")
        public Response manipulateCommentFromProfile(
                @PathParam("profileId") String profileId,
                @PathParam("commentId") String commentId,
                @FormParam("method") String method) {
            if ("delete".equalsIgnoreCase(method)) {
                return this.deleteCommentFromProfile(profileId, commentId);
            } else {
                return Response.ok().build();
            }
        }

        @POST
        @Path("/components/{componentId}/comments/{commentId}")
        @ApiOperation(value = "Allows for deletion of single component comment (workaround for Flex which does not support the DELETE method)")
        public Response manipulateCommentFromComponent(
                @PathParam("componentId") String componentId,
                @PathParam("commentId") String commentId,
                @FormParam("method") String method) {
            if ("delete".equalsIgnoreCase(method)) {
                return this.deleteCommentFromComponent(componentId, commentId);
            } else {
                return Response.ok().build();
            }
        }

        @POST
        @Path("/profiles/{profileId}/publish")
        @Consumes("multipart/form-data")
        @ApiOperation(value = "Changes the state of the specified profile to published")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Item requires authorisation and user is not authenticated"),
            @ApiResponse(code = 403, message = "Item is not owned by current user"),
            @ApiResponse(code = 404, message = "Item does not exist")
        })
        public Response publishRegisteredProfile(
                @PathParam("profileId") String profileId,
                @FormDataParam(DATA_FORM_FIELD) InputStream input,
                @FormDataParam(NAME_FORM_FIELD) String name,
                @FormDataParam(DESCRIPTION_FORM_FIELD) String description,
                @FormDataParam(GROUP_FORM_FIELD) String group,
                @FormDataParam(DOMAIN_FORM_FIELD) String domainName) {

            try {
                Principal principal = checkAndGetUserPrincipal();
                ComponentRegistry registry = this.getBaseRegistry();
                ProfileDescription desc = registry.getProfileDescriptionAccessControlled(profileId);
                if (desc != null) {
                    if (desc.isPublic()) {
                        LOG.warn("Attempt to publish already public profile {}", profileId);
                        return Response.status(Status.CONFLICT).entity("Cannot publish already published profile.")
                                .build();
                    }
                    desc.setPublic(true);
                    this.updateDescription(desc, name, description, domainName, group);
                    return this.register(input, desc, new PublishAction(principal), registry);
                } else {
                    LOG.error("Update of nonexistent profile {} failed.", profileId);
                    return Response
                            .serverError()
                            .entity("Invalid id, cannot update nonexistent profile")
                            .build();
                }

            } catch (AuthenticationRequiredException e) {
                return Response.serverError().status(Status.UNAUTHORIZED)
                        .build();
            } catch (ItemNotFoundException e1) {
                return Response.status(Status.NOT_FOUND).entity(e1.getMessage()).build();
            } catch (ComponentRegistryException e) {
                LOG.warn("Could not retrieve profile {}", profileId);
                LOG.debug("Details", e);
                return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                        .build();
            } catch (UserUnauthorizedException ex) {
                return Response.status(Status.FORBIDDEN).entity(ex.getMessage())
                        .build();
            }
        }

        @POST
        @Path("/profiles/{profileId}/update")
        @Consumes("multipart/form-data")
        @ApiOperation(value = "Updates an already registered (but unpublished) profile")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "User is not authenticated"),
            @ApiResponse(code = 403, message = "Item is not owned by current user"),
            @ApiResponse(code = 404, message = "Item does not exist")
        })
        public Response updateRegisteredProfile(
                @PathParam("profileId") String profileId,
                @FormDataParam(DATA_FORM_FIELD) InputStream input,
                @FormDataParam(NAME_FORM_FIELD) String name,
                @FormDataParam(DESCRIPTION_FORM_FIELD) String description,
                @FormDataParam(GROUP_FORM_FIELD) String group,
                @FormDataParam(DOMAIN_FORM_FIELD) String domainName) {
            try {
                final ComponentRegistry br = this.getBaseRegistry();
                final ProfileDescription desc = br.getProfileDescriptionAccessControlled(profileId);
                if (desc != null) {
                    if (desc.getStatus() != ComponentStatus.DEVELOPMENT) {
                        return Response.status(Status.CONFLICT).entity("Cannot update a profile with status " + desc.getStatus().toString())
                                .build();
                    }
                    final Number groupId;
                    final RegistrySpace space;
                    final List<Number> groupIds = br.getItemGroups(profileId);
                    if (groupIds == null || groupIds.isEmpty()) {
                        groupId = null;
                        space = RegistrySpace.PRIVATE;
                    } else {
                        groupId = groupIds.get(0);
                        space = RegistrySpace.GROUP;
                    }

                    updateDescription(desc, name, description, domainName, group);

                    final ComponentRegistry cr = this.getRegistry(space, groupId);
                    return register(input, desc, new UpdateAction(), cr);
                } else {
                    return createNonExistentItemResponse(profileId);
                }
            } catch (ComponentRegistryException e) {
                LOG.warn("Could not retrieve profile {}", profileId);
                LOG.debug("Details", e);
                return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                        .build();

            } catch (UserUnauthorizedException ex) {
                return Response.status(Status.FORBIDDEN).entity(ex.getMessage())
                        .build();

            } catch (ItemNotFoundException ex2) {
                return Response.status(Status.NOT_FOUND).entity(ex2.getMessage())
                        .build();
            } catch (AuthenticationRequiredException e1) {
                return Response.status(Status.UNAUTHORIZED).entity(e1.getMessage())
                        .build();
            }

        }

        /**
         *
         * Purely helper method for my front-end (FLEX) which van only do
         * post/get requests. The query param is checked and the "proper" method
         * is called.
         *
         * @param componentId
         * @param method
         * @return
         */
        @POST
        @Path("/components/{componentId}")
        @ApiOperation(value = "Allows for deletion of single component (workaround for Flex which does not support the DELETE method)")
        public Response manipulateRegisteredComponent(
                @PathParam("componentId") String componentId,
                @FormParam("method") String method) {
            if ("delete".equalsIgnoreCase(method)) {
                return this.deleteRegisteredComponent(componentId);
            } else {
                return Response.ok("Nothing to do, not 'delete' method").build();
            }
        }

        @POST
        @Path("/components/{componentId}/publish")
        @Consumes("multipart/form-data")
        @ApiOperation(value = "Changes the state of the specified component to published")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Item requires authorisation and user is not authenticated"),
            @ApiResponse(code = 403, message = "Item is not owned by current user"),
            @ApiResponse(code = 404, message = "Item does not exist")
        })
        public Response publishRegisteredComponent(
                @PathParam("componentId") String componentId,
                @FormDataParam(DATA_FORM_FIELD) InputStream input,
                @FormDataParam(NAME_FORM_FIELD) String name,
                @FormDataParam(DESCRIPTION_FORM_FIELD) String description,
                @FormDataParam(GROUP_FORM_FIELD) String group,
                @FormDataParam(DOMAIN_FORM_FIELD) String domainName) {

            try {
                Principal principal = checkAndGetUserPrincipal();
                ComponentRegistry registry = this.getBaseRegistry();
                ComponentDescription desc = registry.getComponentDescriptionAccessControlled(componentId);
                if (desc != null) {
                    if (desc.isPublic()) {
                        LOG.warn("Attempt to publish already public component {}", componentId);
                        return Response.status(Status.CONFLICT).entity("Cannot publish already published omponent.")
                                .build();
                    }
                    desc.setPublic(true);
                    this.updateDescription(desc, name, description, domainName, group);
                    return this.register(input, desc, new PublishAction(principal), registry);
                } else {
                    return createNonExistentItemResponse(componentId);
                }
            } catch (AuthenticationRequiredException e) {
                LOG.warn("Could not retrieve component {}", componentId);
                LOG.debug("Details", e);
                return Response.serverError().status(Status.UNAUTHORIZED)
                        .build();
            } catch (ComponentRegistryException e) {
                LOG.warn("Could not retrieve component {}", componentId);
                LOG.debug("Details", e);
                return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                        .build();
            } catch (ItemNotFoundException e) {
                LOG.warn("Could not retrieve component {}", componentId);
                LOG.debug("Details", e);
                return Response.serverError().status(Status.NOT_FOUND)
                        .build();
            } catch (UserUnauthorizedException ex) {
                return Response.status(Status.FORBIDDEN).entity(ex.getMessage())
                        .build();
            }
        }

        @POST
        @Path("/components/{componentId}/update")
        @Consumes("multipart/form-data")
        @ApiOperation(value = "Updates an already registered (but unpublished) component")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "User is not authenticated"),
            @ApiResponse(code = 403, message = "Item is not owned by current user"),
            @ApiResponse(code = 404, message = "Item does not exist")
        })
        public Response updateRegisteredComponent(
                @PathParam("componentId") String componentId,
                @FormDataParam(DATA_FORM_FIELD) InputStream input,
                @FormDataParam(NAME_FORM_FIELD) String name,
                @FormDataParam(DESCRIPTION_FORM_FIELD) String description,
                @FormDataParam(GROUP_FORM_FIELD) String group,
                @FormDataParam(DOMAIN_FORM_FIELD) String domainName) {
            try {
                final ComponentRegistry br = this.getBaseRegistry();
                final ComponentDescription desc = br.getComponentDescriptionAccessControlled(componentId);
                if (desc != null) {
                    if (desc.getStatus() != ComponentStatus.DEVELOPMENT) {
                        return Response.status(Status.CONFLICT).entity("Cannot update a component with status " + desc.getStatus().toString())
                                .build();
                    }
                    final Number groupId;
                    final RegistrySpace space;
                    final List<Number> groupIds = br.getItemGroups(componentId);
                    if (groupIds == null || groupIds.isEmpty()) {
                        groupId = null;
                        space = RegistrySpace.PRIVATE;
                    } else {
                        groupId = groupIds.get(0);
                        space = RegistrySpace.GROUP;
                    }

                    this.updateDescription(desc, name, description, domainName, group);

                    final ComponentRegistry cr = this.getRegistry(space, groupId);
                    return this.register(input, desc, new UpdateAction(), cr);
                } else {
                    LOG.error("Update of nonexistent id (" + componentId
                            + ") failed.");
                    return Response
                            .serverError()
                            .entity("Invalid id, cannot update nonexistent component")
                            .build();
                }
            } catch (ComponentRegistryException e) {
                LOG.warn("Could not retrieve component {}", componentId);
                LOG.debug("Details", e);
                return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                        .build();
            } catch (UserUnauthorizedException | ItemNotFoundException ex) {
                return Response.status(Status.FORBIDDEN).entity(ex.getMessage())
                        .build();
            } catch (AuthenticationRequiredException e1) {
                return Response.status(Status.UNAUTHORIZED).entity(e1.getMessage())
                        .build();
            }

        }

        private void updateDescription(BaseDescription desc, String name,
                String description, String domainName, String group) {
            desc.setName(name);
            desc.setDescription(description);
            desc.setDomainName(domainName);
            desc.setGroupName(group);
            desc.setRegistrationDate(new Date());
        }

        @DELETE
        @Path("/components/{componentId}")
        @ApiOperation(value = "Deletes the component with the specified ID from its registry")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "User is not authenticated"),
            @ApiResponse(code = 403, message = "Non-public item is not owned by current user and user is no administrator"),
            @ApiResponse(code = 404, message = "Item does not exist")
        })
        public Response deleteRegisteredComponent(
                @PathParam("componentId") String componentId) {
            try {
                ComponentRegistry registry = this.getBaseRegistry();
                LOG.debug("Component with id {} set for deletion.", componentId);
                registry.deleteMDComponent(componentId, false);
            } catch (DeleteFailedException e) {
                LOG.info("Component with id {} deletion failed. Reason: {}",
                        componentId, e.getMessage());
                LOG.debug("Deletion failure details:", e);
                return Response.status(Status.FORBIDDEN)
                        .entity("" + e.getMessage()).build();
            } catch (ComponentRegistryException e) {
                LOG.warn("Component with id " + componentId + " deletion failed.",
                        e);
                return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                        .build();
            } catch (ItemNotFoundException e) {
                LOG.warn("Component with id " + componentId + " is not found.");
                return Response.serverError().status(Status.NOT_FOUND)
                        .build();
            } catch (IOException e) {
                LOG.error("Component with id " + componentId + " deletion failed.",
                        e);
                return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                        .build();
            } catch (UserUnauthorizedException e) {
                LOG.info("Component with id {} deletion failed: {}", componentId,
                        e.getMessage());
                LOG.debug("Deletion failure details:", e);
                return Response.serverError().status(Status.FORBIDDEN)
                        .entity("" + e.getMessage()).build();
            } catch (AuthenticationRequiredException e1) {
                return Response.status(Status.UNAUTHORIZED).entity(e1.getMessage())
                        .build();
            }

            LOG.info("Component with id: {} deleted.", componentId);
            return Response.ok("Component with id" + componentId + " deleted.").build();
        }

        @DELETE
        @Path("/profiles/{profileId}")
        @ApiOperation(value = "Deletes the profile with the specified ID from its registry")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "User is not authenticated"),
            @ApiResponse(code = 403, message = "Non-public item is not owned by current user and user is no administrator"),
            @ApiResponse(code = 404, message = "Item does not exist")
        })
        public Response deleteRegisteredProfile(
                @PathParam("profileId") String profileId) {
            try {
                LOG.debug("Profile with id: {} set for deletion.", profileId);
                this.getBaseRegistry().deleteMDProfile(profileId);
            } catch (DeleteFailedException | UserUnauthorizedException e) {
                LOG.info("Profile with id: {} deletion failed: {}", profileId,
                        e.getMessage());
                LOG.debug("Deletion failure details:", e);
                return Response.serverError().status(Status.FORBIDDEN)
                        .entity("" + e.getMessage()).build();
            } catch (ComponentRegistryException e) {
                LOG.warn("Could not retrieve component", e);
                return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                        .build();
            } catch (ItemNotFoundException e) {
                LOG.warn("Profile with id " + profileId + " is not found.");
                return Response.serverError().status(Status.NOT_FOUND)
                        .build();
            } catch (IOException e) {
                LOG.error("Profile with id: " + profileId + " deletion failed.", e);
                return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                        .build();
            } catch (AuthenticationRequiredException e1) {
                return Response.status(Status.UNAUTHORIZED).entity(e1.getMessage())
                        .build();
            }

            LOG.info("Profile with id: {} deleted.", profileId);
            return Response.ok().build();
        }

        @DELETE
        @Path("/profiles/{profileId}/comments/{commentId}")
        @ApiOperation(value = "Deletes a comment from a profile")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "User is not authenticated"),
            @ApiResponse(code = 403, message = "Comment is not owned by current user and user is no administrator"),
            @ApiResponse(code = 404, message = "Comment does not exist")
        })
        public Response deleteCommentFromProfile(
                @PathParam("profileId") String profileId,
                @PathParam("commentId") String commentId) {
            try {
                final ComponentRegistry registry = this.getBaseRegistry();
                try {
                    final Comment comment = registry.getSpecifiedCommentInProfile(profileId, commentId);
                    if (comment != null
                            && profileId.equals(comment.getComponentRef())) {
                        LOG.debug("Comment with id: {} set for deletion.", commentId);
                        registry.deleteComment(commentId);
                    } else {
                        throw new ComponentRegistryException(
                                "Comment not found for specified profile");
                    }
                } catch (ItemNotFoundException e1) {
                    LOG.info("Comment with id: {} deletion failed: {}", commentId,
                            e1.getMessage());
                    LOG.debug("Deletion failure details:", e1);
                    return Response.serverError().status(Status.NOT_FOUND)
                            .entity("" + e1.getMessage()).build();
                }

            } catch (DeleteFailedException | UserUnauthorizedException e) {
                LOG.info("Comment with id: {} deletion failed: {}", commentId,
                        e.getMessage());
                LOG.debug("Deletion failure details:", e);
                return Response.serverError().status(Status.FORBIDDEN)
                        .entity("" + e.getMessage()).build();
            } catch (ComponentRegistryException e) {
                LOG.info("Could not find comment " + commentId + " for " + profileId);
                return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                        .build();
            } catch (IOException e) {
                LOG.error("Comment with id: " + commentId + " deletion failed.", e);
                return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                        .build();
            } catch (AuthenticationRequiredException e1) {
                return Response.status(Status.UNAUTHORIZED).entity(e1.getMessage())
                        .build();
            }

            LOG.info("Comment with id: {} deleted.", commentId);
            return Response.ok("Comment with id " + commentId + " deleted.").build();
        }

        @DELETE
        @Path("/components/{componentId}/comments/{commentId}")
        @ApiOperation(value = "Deletes a comment from a component")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "User is not authenticated"),
            @ApiResponse(code = 403, message = "Comment is not owned by current user and user is no administrator"),
            @ApiResponse(code = 404, message = "Comment does not exist")
        })
        public Response deleteCommentFromComponent(
                @PathParam("componentId") String componentId,
                @PathParam("commentId") String commentId) {
            try {
                final ComponentRegistry registry = this.getBaseRegistry();
                final Comment comment = registry.getSpecifiedCommentInComponent(componentId, commentId);
                if (comment != null
                        && componentId.equals(comment.getComponentRef())) {
                    LOG.debug("Comment with id: {} set for deletion.", commentId);
                    registry.deleteComment(commentId);
                } else {
                    throw new ComponentRegistryException(
                            "Comment not found for specified component");
                }
            } catch (ItemNotFoundException e) {
                LOG.info("Comment with id: {} deletion failed: {}", commentId,
                        e.getMessage());
                LOG.debug("Deletion failure details:", e);
                return Response.serverError().status(Status.NOT_FOUND)
                        .entity("" + e.getMessage()).build();
            } catch (DeleteFailedException | UserUnauthorizedException e) {
                LOG.info("Comment with id: {} deletion failed: {}", commentId,
                        e.getMessage());
                LOG.debug("Deletion failure details:", e);
                return Response.serverError().status(Status.FORBIDDEN)
                        .entity("" + e.getMessage()).build();
            } catch (ComponentRegistryException e) {
                LOG.info("Could not retrieve component " + componentId + " for the component " + componentId);
                return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                        .build();
            } catch (IOException e) {
                LOG.error("Comment with id: " + commentId + " deletion failed.", e);
                return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                        .build();
            } catch (AuthenticationRequiredException e1) {
                return Response.status(Status.UNAUTHORIZED).entity(e1.getMessage())
                        .build();
            }

            LOG.info("Comment with id: {} deleted.", commentId);
            return Response.ok("Comment with id " + commentId + " deleted.").build();
        }

        @GET
        @Path("/profiles/{profileId}/{targetVersion:([0-9]+\\.[0-9]+/)?}{rawType}")
        @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
        @ApiOperation(value = "The expanded XML or XSD represenation of the component specification of a single profile (publicly accessible regardless of state!)")
        @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Item does not exist")
        })
        public Response getRegisteredProfileRawType(
                @PathParam("profileId") final String profileId,
                @PathParam("targetVersion") final String targetVersion,
                @PathParam("rawType") String rawType)
                throws ComponentRegistryException, IllegalArgumentException {

            LOG.debug("Profile with id {} and rawType {} is requested.", profileId,
                    rawType);
            try {
                final ComponentRegistry registry = this.getBaseRegistry();

                final ProfileDescription desc = registry.getProfileDescription(profileId);
                if (desc == null) {
                    return Response.status(Status.NOT_FOUND).build();
                }

                StreamingOutput result;
                String fileName = desc.getName() + "." + rawType;
                if ("xml".equalsIgnoreCase(rawType)) {
                    result = new StreamingOutput() {

                        @Override
                        public void write(OutputStream output) throws IOException,
                                WebApplicationException {
                            try {
                                registry.getMDProfileAsXml(profileId, getCmdVersion(), output); //TODO: function of version number
                            } catch (ComponentRegistryException | UserUnauthorizedException | ItemNotFoundException e) {
                                LOG.warn("Could not retrieve component {}: {}",
                                        profileId, e.getMessage());
                                LOG.debug("Details", e);
                                throw new WebApplicationException(Response
                                        .serverError()
                                        .status(Status.INTERNAL_SERVER_ERROR)
                                        .build());
                            }
                        }
                    };
                } else if ("xsd".equalsIgnoreCase(rawType)) {
                    result = new StreamingOutput() {

                        @Override
                        public void write(OutputStream output) throws IOException,
                                WebApplicationException {
                            try {
                                final CmdVersion[] versions;
                                if (targetVersion != null && !targetVersion.isEmpty()) {
                                    //remove trailing slash from target version param and get matching CmdVersion value
                                    versions = new CmdVersion[]{getCmdVersion(), getCmdVersion(targetVersion.replaceAll("\\/$", ""))};
                                } else {
                                    versions = new CmdVersion[]{getCmdVersion()};
                                }
                                registry.getMDProfileAsXsd(profileId, versions, output);
                            } catch (ComponentRegistryException | UserUnauthorizedException | ItemNotFoundException e) {
                                LOG.warn("Could not retrieve component {}: {}",
                                        profileId, e.getMessage());
                                LOG.debug("Details", e);
                                throw new WebApplicationException(Response
                                        .serverError()
                                        .status(Status.INTERNAL_SERVER_ERROR)
                                        .build());
                            }
                        }
                    };
                } else {
                    return Response.status(Status.NOT_FOUND).entity("Unsupported raw type " + rawType).build();
                }
                return createDownloadResponse(result, fileName);
            } catch (ItemNotFoundException e) {
                return Response.serverError().status(Status.NOT_FOUND)
                        .entity("" + e.getMessage()).build();
            } catch (AuthenticationRequiredException e1) {
                return Response.status(Status.UNAUTHORIZED).entity(e1.getMessage())
                        .build();
            }

        }

        private Response createDownloadResponse(StreamingOutput result,
                String fileName) {
            // Making response so it triggers browsers native save as dialog.
            return Response
                    .ok()
                    .type("application/x-download")
                    .header("Content-Disposition",
                            "attachment; filename=\"" + fileName + "\"")
                    .entity(result).build();

        }

        @POST
        @Path("/profiles")
        @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON})
        @Consumes("multipart/form-data")
        @ApiOperation(value = "Registers a profile specification (data content) with the associated metadata (form content) in the user's private space")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "User is not authenticated"),
            @ApiResponse(code = 403, message = "Current user has no access")
        })
        public Response registerProfile(
                @FormDataParam(DATA_FORM_FIELD) InputStream input,
                @FormDataParam(NAME_FORM_FIELD) String name,
                @FormDataParam(DESCRIPTION_FORM_FIELD) String description,
                @FormDataParam(GROUP_FORM_FIELD) String group,
                @FormDataParam(DOMAIN_FORM_FIELD) String domainName) {
            try {
                Principal principal = checkAndGetUserPrincipal();
                ShhaaUserCredentials userCredentials = getUserCredentials(principal);
                ProfileDescription desc = this.createNewProfileDescription();
                desc.setCreatorName(userCredentials.getDisplayName());
                desc.setUserId(userCredentials.getPrincipalName()); // Hash used to be created here, now Id is constructed by impl
                desc.setName(name);
                desc.setDescription(description);
                desc.setGroupName(group);
                desc.setDomainName(domainName);
                desc.setPublic(false);
                LOG.debug("Trying to register Profile: {}", desc);
                ComponentRegistry cr = this.getRegistry(RegistrySpace.PRIVATE, null);
                return this.register(input, desc, new NewAction(), cr);
            } catch (AuthenticationRequiredException e) {
                LOG.debug("Details", e);
                return Response.serverError().status(Status.UNAUTHORIZED)
                        .build();
            } catch (UserUnauthorizedException ex) {
                return Response.status(Status.FORBIDDEN).entity(ex.getMessage())
                        .build();
            }
        }

        @POST
        @Path("/components")
        @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON})
        @Consumes("multipart/form-data")
        @ApiOperation(value = "Registers a component specification (data content) with the associated metadata (form content) in the user's private space")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "User is not authenticated"),
            @ApiResponse(code = 403, message = "Current user has no access")
        })
        public Response registerComponent(
                @FormDataParam(DATA_FORM_FIELD) InputStream input,
                @FormDataParam(NAME_FORM_FIELD) String name,
                @FormDataParam(DESCRIPTION_FORM_FIELD) String description,
                @FormDataParam(GROUP_FORM_FIELD) String group,
                @FormDataParam(DOMAIN_FORM_FIELD) String domainName) {
            try {
                Principal principal = checkAndGetUserPrincipal();
                ShhaaUserCredentials userCredentials = getUserCredentials(principal);
                ComponentDescription desc = this.createNewComponentDescription();
                desc.setCreatorName(userCredentials.getDisplayName());
                desc.setUserId(userCredentials.getPrincipalName()); // Hash used to
                // be created
                // here, now Id
                // is
                // constructed
                // by impl
                desc.setName(name);
                desc.setDescription(description);
                desc.setGroupName(group);
                desc.setDomainName(domainName);
                desc.setPublic(false);
                LOG.debug("Trying to register Component: {}", desc);
                ComponentRegistry cr = this.getRegistry(RegistrySpace.PRIVATE, null);
                return this.register(input, desc, new NewAction(), cr);
            } catch (AuthenticationRequiredException e) {
                LOG.debug("Details", e);
                return Response.serverError().status(Status.UNAUTHORIZED)
                        .build();
            } catch (UserUnauthorizedException ex) {
                return Response.status(Status.FORBIDDEN).entity(ex.getMessage())
                        .build();
            }
        }

        @POST
        @Path("/components/{componentId}/comments")
        @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON})
        @Consumes("multipart/form-data")
        @ApiOperation(value = "Publishes a comment on the specified component")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "User is not authenticated"),
            @ApiResponse(code = 403, message = "Non-public item is not owned by current user"),
            @ApiResponse(code = 404, message = "Item does not exist")
        })
        public Response registerCommentInComponent(
                @FormDataParam(DATA_FORM_FIELD) InputStream input,
                @PathParam("componentId") String componentId) throws ComponentRegistryException {
            try {
                ComponentRegistry registry = this.getBaseRegistry();
                ComponentDescription description = registry.getComponentDescriptionAccessControlled(componentId);

                LOG.debug("Trying to register comment to {}", componentId);

                return this.registerComment(input, description, registry);
            } catch (AuthenticationRequiredException e) {
                LOG.debug("Details", e);
                return Response.serverError().status(Status.UNAUTHORIZED)
                        .build();
            } catch (UserUnauthorizedException ex) {
                return Response.status(Status.FORBIDDEN)
                        .build();
            } catch (ItemNotFoundException e) {
                return Response.serverError().status(Status.NOT_FOUND)
                        .entity("" + e.getMessage()).build();
            }
        }

        @GET
        @Path("/profiles/{profileId}/status")
        @ApiOperation(value = "Gets the status of a registered profile")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "User is not authenticated"),
            @ApiResponse(code = 403, message = "Item is not owned by current user"),
            @ApiResponse(code = 404, message = "Item does not exist")
        })
        public Response getProfileStatus(
                @PathParam("profileId") String profileId) {
            try {
                final ComponentRegistry br = this.getBaseRegistry();
                final ProfileDescription desc = br.getProfileDescriptionAccessControlled(profileId);
                return Response.status(Status.OK)
                        .entity(desc.getStatus().toString())
                        .build();
            } catch (ComponentRegistryException e) {
                LOG.warn("Could not retrieve profile {}", profileId);
                LOG.debug("Details", e);
                return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                        .build();
            } catch (UserUnauthorizedException ex) {
                return Response.status(Status.FORBIDDEN).entity(ex.getMessage())
                        .build();
            } catch (ItemNotFoundException ex2) {
                return Response.status(Status.NOT_FOUND).entity(ex2.getMessage())
                        .build();
            } catch (AuthenticationRequiredException e1) {
                return Response.status(Status.UNAUTHORIZED).entity(e1.getMessage())
                        .build();
            }
        }

        @POST
        @Path("/profiles/{profileId}/status")
        @Consumes("multipart/form-data")
        @ApiOperation(value = "Updates the status of an already registered profile")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "User is not authenticated"),
            @ApiResponse(code = 403, message = "Item is not owned by current user"),
            @ApiResponse(code = 404, message = "Item does not exist")
        })
        public Response updateProfileStatus(
                @PathParam("profileId") String profileId,
                @FormDataParam(STATUS_FORM_FIELD) String newStatus) {
            try {
                final ComponentRegistry br = this.getBaseRegistry();
                final ProfileDescription desc = br.getProfileDescriptionAccessControlled(profileId);
                if (desc != null) {
                    final ComponentSpec spec = this.getBaseRegistry().getMDProfileAccessControled(profileId);
                    if (spec != null) {
                        return tryUpdateItemStatus(desc, spec, newStatus, br, profileId);
                    }
                }
                //description or spec not found...
                return createNonExistentItemResponse(profileId);
            } catch (ComponentRegistryException e) {
                LOG.warn("Could not retrieve profile {}", profileId);
                LOG.debug("Details", e);
                return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                        .build();

            } catch (UserUnauthorizedException ex) {
                return Response.status(Status.FORBIDDEN).entity(ex.getMessage())
                        .build();

            } catch (ItemNotFoundException ex2) {
                return Response.status(Status.NOT_FOUND).entity(ex2.getMessage())
                        .build();
            } catch (AuthenticationRequiredException e1) {
                return Response.status(Status.UNAUTHORIZED).entity(e1.getMessage())
                        .build();
            }
        }

        @GET
        @Path("/components/{componentId}/status")
        @ApiOperation(value = "Gets the status of a registered component")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "User is not authenticated"),
            @ApiResponse(code = 403, message = "Item is not owned by current user"),
            @ApiResponse(code = 404, message = "Item does not exist")
        })
        public Response getComponentStatus(
                @PathParam("componentId") String componentId) {
            try {
                final ComponentRegistry br = this.getBaseRegistry();
                final ComponentDescription desc = br.getComponentDescriptionAccessControlled(componentId);
                return Response.status(Status.OK)
                        .entity(desc.getStatus().toString())
                        .build();
            } catch (ComponentRegistryException e) {
                LOG.warn("Could not retrieve component {}", componentId);
                LOG.debug("Details", e);
                return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                        .build();
            } catch (UserUnauthorizedException ex) {
                return Response.status(Status.FORBIDDEN).entity(ex.getMessage())
                        .build();
            } catch (ItemNotFoundException ex2) {
                return Response.status(Status.NOT_FOUND).entity(ex2.getMessage())
                        .build();
            } catch (AuthenticationRequiredException e1) {
                return Response.status(Status.UNAUTHORIZED).entity(e1.getMessage())
                        .build();
            }
        }

        @POST
        @Path("/components/{componentId}/status")
        @Consumes("multipart/form-data")
        @ApiOperation(value = "Updates the status of an already registered component")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "User is not authenticated"),
            @ApiResponse(code = 403, message = "Item is not owned by current user"),
            @ApiResponse(code = 404, message = "Item does not exist")
        })
        public Response updateComponentStatus(
                @PathParam("componentId") String componentId,
                @FormDataParam(STATUS_FORM_FIELD) String newStatus) {
            try {
                final ComponentRegistry br = this.getBaseRegistry();
                final ComponentDescription desc = br.getComponentDescriptionAccessControlled(componentId);
                if (desc != null) {
                    final ComponentSpec spec = this.getBaseRegistry().getMDComponentAccessControlled(componentId);
                    if (spec != null) {
                        return tryUpdateItemStatus(desc, spec, newStatus, br, componentId);
                    }
                }
                //description or spec not found...
                return createNonExistentItemResponse(componentId);
            } catch (ComponentRegistryException e) {
                LOG.warn("Could not retrieve profile {}", componentId);
                LOG.debug("Details", e);
                return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                        .build();
            } catch (UserUnauthorizedException ex) {
                return Response.status(Status.FORBIDDEN).entity(ex.getMessage())
                        .build();

            } catch (ItemNotFoundException ex2) {
                return Response.status(Status.NOT_FOUND).entity(ex2.getMessage())
                        .build();
            } catch (AuthenticationRequiredException e1) {
                return Response.status(Status.UNAUTHORIZED).entity(e1.getMessage())
                        .build();
            }
        }

        private Response tryUpdateItemStatus(final BaseDescription desc, final ComponentSpec spec, String newStatus, final ComponentRegistry br, String profileId) throws UserUnauthorizedException, ItemNotFoundException, AuthenticationRequiredException {
            final ComponentStatus targetStatus;
            try {
                targetStatus = ComponentStatus.valueOf(newStatus.toUpperCase());
            } catch (IllegalArgumentException ex) {
                LOG.warn("Invalid component status {}", newStatus, ex);
                return createBadRequestResponse("Invalid component status, must be one of accepted values " + Arrays.asList(ComponentStatus.values()));
            }

            //Status of deprecated components cannot be changed
            if (desc.getStatus() == ComponentStatus.DEPRECATED) {
                //TODO: allow for admin
                return createBadRequestResponse("Status of deprecated components cannot be changed");
            } //Cannot put item back into development status
            else if (targetStatus == ComponentStatus.DEVELOPMENT && desc.getStatus() == ComponentStatus.PRODUCTION) {
                return createBadRequestResponse("Cannot put item back into development status");
            } //Cannot put a private component in production status
            else if (targetStatus == ComponentStatus.PRODUCTION && !desc.isPublic()) {
                return createBadRequestResponse("Cannot put a private component in production status");
            } else {
                // all is ok
                spec.getHeader().setStatus(targetStatus.toString());
                desc.setStatus(targetStatus);

                final int returnCode = br.update(desc, spec, true);
                if (returnCode == 0) {
                    return Response.status(Status.OK)
                            .entity(targetStatus.toString())
                            .build();
                } else {
                    return Response.status(Status.INTERNAL_SERVER_ERROR)
                            .entity("Failed to upate status")
                            .build();
                }
            }
        }

        @GET
        @Path("/components/{componentId}/successor")
        @ApiOperation(value = "Gets the successor of a registered component")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "User is not authenticated"),
            @ApiResponse(code = 403, message = "Item is not owned by current user"),
            @ApiResponse(code = 404, message = "Item does not exist (no item or no successor)")
        })
        public Response getComponentSuccessor(
                @PathParam("componentId") String componentId) {
            try {
                final ComponentRegistry br = this.getBaseRegistry();
                final ComponentDescription desc = br.getComponentDescriptionAccessControlled(componentId);
                final String successorId = desc.getSuccessor();
                if (successorId == null || successorId.isEmpty()) {
                    return Response
                            .status(Status.NOT_FOUND)
                            .entity("Component has no successor")
                            .build();
                } else {
                    return Response
                            .status(Status.OK)
                            .entity(successorId)
                            .build();
                }
            } catch (ComponentRegistryException e) {
                LOG.warn("Could not retrieve component {}", componentId);
                LOG.debug("Details", e);
                return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                        .build();
            } catch (UserUnauthorizedException ex) {
                return Response.status(Status.FORBIDDEN).entity(ex.getMessage())
                        .build();
            } catch (ItemNotFoundException ex2) {
                return Response.status(Status.NOT_FOUND).entity(ex2.getMessage())
                        .build();
            } catch (AuthenticationRequiredException e1) {
                return Response.status(Status.UNAUTHORIZED).entity(e1.getMessage())
                        .build();
            }
        }

        @GET
        @Path("/profiles/{profileId}/successor")
        @ApiOperation(value = "Gets the successor of a registered component")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "User is not authenticated"),
            @ApiResponse(code = 403, message = "Item is not owned by current user"),
            @ApiResponse(code = 404, message = "Item does not exist (no item or no successor)")
        })
        public Response getProfileSuccessor(
                @PathParam("profileId") String profileId) {
            try {
                final ComponentRegistry br = this.getBaseRegistry();
                final ProfileDescription desc = br.getProfileDescriptionAccessControlled(profileId);
                final String successorId = desc.getSuccessor();
                if (successorId == null || successorId.isEmpty()) {
                    return Response
                            .status(Status.NOT_FOUND)
                            .entity("Component has no successor")
                            .build();
                } else {
                    return Response
                            .status(Status.OK)
                            .entity(successorId)
                            .build();
                }
            } catch (ComponentRegistryException e) {
                LOG.warn("Could not retrieve component {}", profileId);
                LOG.debug("Details", e);
                return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                        .build();
            } catch (UserUnauthorizedException ex) {
                return Response.status(Status.FORBIDDEN).entity(ex.getMessage())
                        .build();
            } catch (ItemNotFoundException ex2) {
                return Response.status(Status.NOT_FOUND).entity(ex2.getMessage())
                        .build();
            } catch (AuthenticationRequiredException e1) {
                return Response.status(Status.UNAUTHORIZED).entity(e1.getMessage())
                        .build();
            }
        }

        @POST
        @Path("/components/{componentId}/successor")
        @Consumes("multipart/form-data")
        @ApiOperation(value = "Sets the successor for a registered component (must have deprecated status)")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "User is not authenticated"),
            @ApiResponse(code = 403, message = "Item is not owned by current user"),
            @ApiResponse(code = 404, message = "Item does not exist")
        })
        public Response updateComponentSuccessor(
                @PathParam("componentId") String componentId,
                @FormDataParam(SUCCESSOR_ID_FORM_FIELD) String successorId) {
            try {
                final ComponentRegistry br = this.getBaseRegistry();
                final ComponentDescription desc = br.getComponentDescriptionAccessControlled(componentId);
                if (desc != null) {
                    final ComponentSpec spec = this.getBaseRegistry().getMDComponentAccessControlled(componentId);
                    if (spec != null) {
                        return tryUpdateItemSuccessor(desc, spec, successorId, br);
                    }
                }
                //description or spec not found...
                return createNonExistentItemResponse(componentId);
            } catch (ComponentRegistryException e) {
                LOG.warn("Could not retrieve component {}", componentId);
                LOG.debug("Details", e);
                return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                        .build();
            } catch (UserUnauthorizedException ex) {
                return Response.status(Status.FORBIDDEN).entity(ex.getMessage())
                        .build();

            } catch (ItemNotFoundException ex2) {
                return Response.status(Status.NOT_FOUND).entity(ex2.getMessage())
                        .build();
            } catch (AuthenticationRequiredException e1) {
                return Response.status(Status.UNAUTHORIZED).entity(e1.getMessage())
                        .build();
            }
        }

        @POST
        @Path("/profiles/{profileId}/successor")
        @Consumes("multipart/form-data")
        @ApiOperation(value = "Sets the successor for a registered profile (must have deprecated status)")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "User is not authenticated"),
            @ApiResponse(code = 403, message = "Item is not owned by current user"),
            @ApiResponse(code = 404, message = "Item does not exist")
        })
        public Response updateProfileSuccessor(
                @PathParam("profileId") String profileId,
                @FormDataParam(SUCCESSOR_ID_FORM_FIELD) String successorId) {
            try {
                final ComponentRegistry br = this.getBaseRegistry();
                final ProfileDescription desc = br.getProfileDescriptionAccessControlled(profileId);
                if (desc != null) {
                    final ComponentSpec spec = this.getBaseRegistry().getMDProfileAccessControled(profileId);
                    if (spec != null) {
                        return tryUpdateItemSuccessor(desc, spec, successorId, br);
                    }
                }
                //description or spec not found...
                return createNonExistentItemResponse(profileId);
            } catch (ComponentRegistryException e) {
                LOG.warn("Could not retrieve profile {}", profileId);
                LOG.debug("Details", e);
                return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
                        .build();
            } catch (UserUnauthorizedException ex) {
                return Response.status(Status.FORBIDDEN).entity(ex.getMessage())
                        .build();

            } catch (ItemNotFoundException ex2) {
                return Response.status(Status.NOT_FOUND).entity(ex2.getMessage())
                        .build();
            } catch (AuthenticationRequiredException e1) {
                return Response.status(Status.UNAUTHORIZED).entity(e1.getMessage())
                        .build();
            }
        }

        private Response tryUpdateItemSuccessor(BaseDescription desc, ComponentSpec spec, final String successorId, ComponentRegistry registry) throws UserUnauthorizedException, AuthenticationRequiredException, ItemNotFoundException, ComponentRegistryException {
            //check if successor is of right type
            if (ComponentUtils.isProfileId(desc.getId()) != ComponentUtils.isProfileId(successorId)) {
                return createBadRequestResponse("Successor item must be of the same type");
            }

            //check if successor exists
            final BaseDescription successorDescription;
            try {
                if (desc.isProfile()) {
                    successorDescription = registry.getProfileDescription(successorId);
                } else {
                    successorDescription = registry.getComponentDescription(successorId);
                }
            } catch (ItemNotFoundException ex) {
                LOG.warn("Request to set non-existing successor {} for {}", successorId, desc.getId());
                return createBadRequestResponse("Successor id does not resolve to existing item of the same type");
            }

            if (desc.getStatus() != ComponentStatus.DEPRECATED) {
                //Only deprecated items can get a successor
                return createBadRequestResponse("Cannot set successor unless component has been deprecated");
            } else if (desc.getSuccessor() != null) {
                //Successor can be set only once
                return createBadRequestResponse("Cannot set successor if already set");
            } else if (successorDescription.getStatus() == ComponentStatus.DEPRECATED) {
                //Successor must not be deprecated
                return createBadRequestResponse("Cannot set deprecated item as successor");
            } else {
                // all is ok
                spec.getHeader().setSuccessor(successorId);
                desc.setSuccessor(successorId);

                final int returnCode = registry.update(desc, spec, true);
                if (returnCode == 0) {
                    return Response.status(Status.OK)
                            .entity(successorId)
                            .build();
                } else {
                    return Response.status(Status.INTERNAL_SERVER_ERROR)
                            .entity("Failed to upate successor information")
                            .build();
                }
            }
        }

        @POST
        @Path("/profiles/{profileId}/comments")
        @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON})
        @Consumes("multipart/form-data")
        @ApiOperation(value = "Publishes a comment on the specified profile")
        @ApiResponses(value = {
            @ApiResponse(code = 401, message = "User is not authenticated"),
            @ApiResponse(code = 403, message = "Non-public item is not owned by current user"),
            @ApiResponse(code = 404, message = "Item does not exist")
        })
        public Response registerCommentInProfile(
                @FormDataParam(DATA_FORM_FIELD) InputStream input,
                @PathParam("profileId") String profileId)
                throws ComponentRegistryException {
            try {
                ComponentRegistry registry = this.getBaseRegistry();
                ProfileDescription description = registry
                        .getProfileDescriptionAccessControlled(profileId);

                LOG.debug("Trying to register comment to {}", profileId);

                return this.registerComment(input, description, registry);
            } catch (AuthenticationRequiredException e) {
                LOG.debug("Details", e);
                return Response.serverError().status(Status.UNAUTHORIZED)
                        .build();
            } catch (UserUnauthorizedException ex) {
                return Response.status(Status.FORBIDDEN)
                        .build();
            } catch (ItemNotFoundException e1) {
                return Response.serverError().status(Status.NOT_FOUND)
                        .entity("" + e1.getMessage()).build();
            }
        }

        @GET
        @Path("/pingSession")
        @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON})
        @ApiOperation(value = "Keeps the session alive")
        public Response pingSession() {
            boolean stillActive = false;
            Principal userPrincipal = security.getUserPrincipal();
            if (LOG.isInfoEnabled()) {
                LOG.debug("ping by <{}>",
                        (userPrincipal == null ? "unauthorized user"
                                : userPrincipal.getName()));
            }
            if (servletRequest != null) {
                if (userPrincipal != null
                        && !ComponentRegistryFactory.ANONYMOUS_USER
                        .equals(userPrincipal.getName())) {
                    stillActive = !((HttpServletRequest) servletRequest).getSession()
                            .isNew();
                }
            }
            return Response
                    .ok()
                    .entity(String.format("<session stillActive=\"%s\"/>",
                            stillActive)).build();
        }

        private Response register(InputStream input, BaseDescription desc, RegisterAction action, ComponentRegistry registry) throws UserUnauthorizedException, AuthenticationRequiredException {
            if (getCmdVersion() != CANONICAL_CMD_VERSION) {
                //don't allow registration on the non-canonical registries
                throw serviceException(Status.BAD_REQUEST, "New or updated components and profiles should be submitted to the registry for " + CANONICAL_CMD_VERSION.toString());
            }

            try {
                final RegisterResponse response = new RegisterResponse();
                response.setIsPrivate(!desc.isPublic());

                final ComponentSpec spec = validate(desc, input, registry, action, response);

                if (response.getErrors().isEmpty()) {
                    try {
                        // Add profile
                        final int returnCode = action.execute(desc, spec, response, registry);
                        if (returnCode == 0) {
                            desc.setHref(createXlink(desc.getId()));
                            response.setRegistered(true);
                            response.setDescription(desc);
                        } else {
                            response.setRegistered(false);
                            response.addError("Unable to register at this moment. Internal server error. Error code: " + returnCode);
                        }
                    } catch (ItemNotFoundException ex) {
                        // Recursion detected
                        response.setRegistered(false);
                        response.addError("Error while expanding specification. "
                                + ex.getMessage());
                    }
                }

                if (!response.getErrors().isEmpty()) {
                    LOG.warn("Registration failed with validation errors: {}",
                            Arrays.toString(response.getErrors().toArray()));
                    response.setRegistered(false);
                } else {
                    LOG.info("Registered new {} {}", desc.isProfile() ? "profile"
                            : "component", desc);
                }
                response.setIsProfile(desc.isProfile());
                return Response.ok(response).build();
            } finally {
                try {
                    input.close();// either we read the input or there was an
                    // exception, we need to close it.
                } catch (IOException e) {
                    LOG.error("Error when closing inputstream: ", e);
                }
            }
        }

        private Response registerComment(InputStream input, BaseDescription description, ComponentRegistry registry) throws UserUnauthorizedException, AuthenticationRequiredException {
            try {
                final CommentValidator validator = new CommentValidator(input, description, marshaller);
                final CommentResponse responseLocal = new CommentResponse();
                responseLocal.setIsPrivate(!description.isPublic());

                this.applyValidators(responseLocal, validator);

                if (responseLocal.getErrors().isEmpty()) {
                    Comment com = validator.getCommentSpec();
                    // int returnCode = action.executeComment(com, response,
                    // registry, principal.getName());

                    // If user name is left empty, fill it using the user's display
                    // name
                    Principal principal = this.checkAndGetUserPrincipal();
                    ShhaaUserCredentials userCredentials = this.getUserCredentials(principal);
                    if (null == com.getUserName() || "".equals(com.getUserName())) {
                        if (userCredentials != null) {
                            com.setUserName(userCredentials.getDisplayName());
                        } else {
                            com.setUserName(principal.getName());
                        }
                    }
                    try {
                        int returnCode = registry.registerComment(com,
                                principal.getName());
                        if (returnCode == 0) {
                            responseLocal.setRegistered(true);
                            responseLocal.setComment(com);
                        } else {
                            responseLocal.setRegistered(false);
                            responseLocal.addError("Unable to post at this moment. Internal server error.");
                        }
                        if (com.getComponentRef() != null) {
                            LOG.info("Posted new comment on component {}",
                                    com.getComponentRef());
                        } else {
                            LOG.info("Posted new comment on profile {}",
                                    com.getComponentRef());
                        }
                    } catch (ItemNotFoundException e) {
                        return Response.serverError().status(Status.NOT_FOUND)
                                .entity("" + e.getMessage()).build();
                    }
                } else {
                    LOG.warn(
                            "Posting of comment failed with validation errors: {}",
                            Arrays.toString(responseLocal.getErrors().toArray()));
                    responseLocal.setRegistered(false);
                }
                return Response.ok(responseLocal).build();
            } catch (ComponentRegistryException ex) {
                LOG.error("Error while inserting comment: ", ex);
                return Response.serverError().entity(ex.getMessage()).build();
            } finally {
                try {
                    input.close();// either we read the input or there was an
                    // exception, we need to close it.
                } catch (IOException e) {
                    LOG.error("Error when closing inputstream: ", e);
                    return Response.serverError().build();
                }
            }
        }

        private ComponentDescription createNewComponentDescription() {
            ComponentDescription desc = ComponentDescription.createNewDescription();
            return desc;
        }

        private ProfileDescription createNewProfileDescription() {
            ProfileDescription desc = ProfileDescription.createNewDescription();
            return desc;
        }

        private String createXlink(String id) {
            return uriInfo
                    .getRequestUriBuilder()
                    .path(id) // add component or profile id to get its path 
                    .replaceQuery("") // no query parameters needed (or desired), so reset query to prevent adoption from current request
                    .build()
                    .toString();
        }

        /**
         * Fills in the 'href' property of descriptions in a list
         *
         * @param <T>
         * @param descriptions list of descriptions to expand href in
         * @see BaseDescription#setHref(java.lang.String)
         */
        private <T extends BaseDescription> void fillHrefs(List<T> descriptions) {
            for (T description : descriptions) {
                description.setHref(createXlink(description.getId()));
            }
        }

        /**
         *
         * @see
         * ComponentRegistryRestService#getApplicationBaseURI(javax.servlet.ServletContext,
         * javax.servlet.http.HttpServletRequest)
         */
        private String getApplicationBaseURI() {
            return ComponentRegistryRestService.getApplicationBaseURI(servletContext, servletRequest);
        }

        /**
         * Performs a number of validation steps and adds all errors encountered
         * to the response object. A component spec is unmarshalled in the
         * process, and returned in the end
         *
         * @param desc description of registered item
         * @param input input stream of registered spec
         * @param registry registry on which we are registering
         * @param action current registration action
         * @param response response object we are building (will be modified)
         * @return the original (unexpanded) component spec object unmarshalled from the input stream
         * while validating
         * @throws UserUnauthorizedException
         */
        private ComponentSpec validate(BaseDescription desc, InputStream input, ComponentRegistry registry, RegisterAction action, final RegisterResponse response) throws UserUnauthorizedException {
            //validators: description validator
            final DescriptionValidator descriptionValidator = new DescriptionValidator(desc);

            //spec validator
            final ComponentSpecValidator specValidator = new ComponentSpecValidator(input, desc, registry, marshaller);
            specValidator.setPreRegistrationMode(action.isPreRegistration());

            //recursion detection (additional validation, must be executed AFTER a succesful spec validation!!)
            final RecursionDetector recursionDetector = new RecursionDetector(new RecursionDetector.ComponentSpecCopyFactory() {
                @Override
                public ComponentSpec newSpecCopy() throws ComponentRegistryException {
                    try {
                        //fresh spec copy can be obtained from the spec validator (after validation)
                        return specValidator.getCopyOfCMDComponentSpec();
                    } catch (JAXBException ex) {
                        throw new ComponentRegistryException(
                                "Unmarshalling failed while preparing recursion detection",
                                ex);
                    }
                }
            }, registry, desc);

            //validation of the expanded spec (additional validation, must be executed AFTER a succesful recursion detection!!)
            final ExpandedSpecValidator expandedSpecValidator = new ExpandedSpecValidator(marshaller, new ExpandedSpecValidator.ExpandedComponentSpecContainer() {
                @Override
                public ComponentSpec getExpandedSpec() throws ComponentRegistryException {
                    //is available after recursion detection
                    return recursionDetector.getExpandedSpecCopy();
                }
            });
            expandedSpecValidator.setPreRegistrationMode(action.isPreRegistration());
            
            //apply all the validators
            applyValidators(response, 
                    descriptionValidator, specValidator, recursionDetector, expandedSpecValidator);
            
            //return component spec for reuse
            return specValidator.getComponentSpec();
        }

        private void applyValidators(ComponentRegistryResponse response, Validator... validators) throws UserUnauthorizedException {
            for (Validator validator : validators) {
                // if there have been validation errors before, check if the validator can be run
                if (validator.runIfInvalid() || response.getErrors().isEmpty()) {
                    // perform actual validation
                    if (!validator.validate()) {
                        // any errors?
                        for (String error : validator.getErrorMessages()) {
                            response.addError(error);
                        }
                    }
                }
            }
        }

        private String helpToMakeTitleForRssDescriptions(String registrySpace, String groupId, String resource, ComponentRegistry cr) throws ItemNotFoundException {
            if (registrySpace == null || (registrySpace.equalsIgnoreCase(REGISTRY_SPACE_GROUP) && groupId == null)
                    || resource == null) {
                return "Undefined registry space or uindefined type of resource";
            }
            if (registrySpace.equalsIgnoreCase(REGISTRY_SPACE_PUBLISHED)) {
                return "Published " + resource;
            }
            if (registrySpace.equalsIgnoreCase(REGISTRY_SPACE_PRIVATE)) {
                return "Private " + resource;
            }

            if (registrySpace.equalsIgnoreCase(REGISTRY_SPACE_GROUP) && groupId != null) {
                return resource + " of group " + groupId + " '" + cr.getGroupName(Integer.parseInt(groupId)) + "'";
            }

            return "Undefined registry space or uindefined type of resource";
        }

        /**
         *
         * @param registrySpace @see RegistrySpace
         * @param limit the number of items to be displayed
         * @param groupId id for group (see {@link RegistrySpace#GROUP})
         * @return rss for the components in the database to which we are
         * currently connected
         * @throws ComponentRegistryException
         * @throws ParseException
         * @throws java.io.IOException
         */
        @GET
        @Path("/components/rss")
        @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON})
        public Rss getRssComponent(@QueryParam(GROUPID_PARAM) String groupId,
                @QueryParam(REGISTRY_SPACE_PARAM) @DefaultValue(REGISTRY_SPACE_PUBLISHED) String registrySpace,
                @QueryParam(STATUS_FILTER_PARAM) List<String> status,
                @QueryParam(NUMBER_OF_RSSITEMS) @DefaultValue("20") String limit)
                throws ComponentRegistryException, ParseException, IOException {
            final List<ComponentDescription> components;
            final String title;
            try {
                ComponentRegistry cr = this.initialiseRegistry(registrySpace, groupId);
                components = cr.getComponentDescriptions(getStatusFilter(status, registrySpace));
                title = this.helpToMakeTitleForRssDescriptions(registrySpace, groupId, "Components", cr);
            } catch (AuthenticationRequiredException e) {
                servletResponse.sendError(Status.UNAUTHORIZED.getStatusCode(), e.toString());
                return new Rss();
            } catch (UserUnauthorizedException e) {
                servletResponse.sendError(Status.FORBIDDEN.getStatusCode(), e.toString());
                return new Rss();
            } catch (ItemNotFoundException e) {
                servletResponse.sendError(Status.NOT_FOUND.getStatusCode(), e.toString());
                return new Rss();
            }

            final RssCreatorDescriptions instance = new RssCreatorDescriptions(getApplicationBaseURI(), "components",
                    Integer.parseInt(limit), components,
                    BaseDescription.COMPARE_ON_DATE, title);
            final Rss rss = instance.getRss();
            LOG.debug("Releasing RSS of {} most recently registered components",
                    limit);
            return rss;
        }

        /**
         *
         * @param registrySpace @see RegistrySpace
         * @param limit the number of items to be displayed
         * @param groupId id for group (see {@link RegistrySpace#GROUP})
         * @return rss for the profiles in the database to which we are
         * currently connected
         * @throws ComponentRegistryException
         * @throws ParseException
         * @throws java.io.IOException @see RegistrySpace
         */
        @GET
        @Path("/profiles/rss")
        @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON})
        public Rss getRssProfile(@QueryParam(GROUPID_PARAM) String groupId,
                @QueryParam(REGISTRY_SPACE_PARAM) @DefaultValue(REGISTRY_SPACE_PUBLISHED) String registrySpace,
                @QueryParam(STATUS_FILTER_PARAM) List<String> status,
                @QueryParam(NUMBER_OF_RSSITEMS) @DefaultValue("20") String limit)
                throws ComponentRegistryException, ParseException, IOException {
            final List<ProfileDescription> profiles;
            final String title;
            try {
                ComponentRegistry cr = this.initialiseRegistry(registrySpace, groupId);
                profiles = cr.getProfileDescriptions(getStatusFilter(status, registrySpace));
                title = this.helpToMakeTitleForRssDescriptions(registrySpace, groupId, "Profiles", cr);
            } catch (AuthenticationRequiredException e) {
                servletResponse.sendError(Status.UNAUTHORIZED.getStatusCode(), e.toString());
                return new Rss();
            } catch (UserUnauthorizedException e) {
                servletResponse.sendError(Status.FORBIDDEN.getStatusCode(), e.toString());
                return new Rss();

            } catch (ItemNotFoundException e) {
                servletResponse.sendError(Status.NOT_FOUND.getStatusCode(), e.toString());
                return new Rss();
            }
            final RssCreatorDescriptions instance = new RssCreatorDescriptions(getApplicationBaseURI(), "profiles",
                    Integer.parseInt(limit), profiles,
                    BaseDescription.COMPARE_ON_DATE, title);
            final Rss rss = instance.getRss();
            LOG.debug("Releasing RSS of {} most recently registered profiles",
                    limit);
            return rss;
        }

        private String helpToMakeTitleForRssComments(String itemId, String resource) {
            if (itemId == null || resource == null) {
                return "Undefined description";
            }
            return ("Comments for " + resource + " " + itemId);
        }

        /**
         *
         * @param profileId the Id of a profile whose comments are to be rss-ed
         * @param limit the number of items to be displayed
         * @return rss of the comments for a chosen profile
         * @throws ComponentRegistryException
         * @throws IOException
         * @throws JAXBException
         * @throws ParseException
         */
        @GET
        @Path("/profiles/{profileId}/comments/rss")
        @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON})
        public Rss getRssOfCommentsFromProfile(
                @PathParam("profileId") String profileId,
                @QueryParam(NUMBER_OF_RSSITEMS) @DefaultValue("20") String limit)
                throws ComponentRegistryException, IOException, JAXBException,
                ParseException {
            try {

                ComponentRegistry cr = this.getBaseRegistry();

                final List<Comment> comments = cr.getCommentsInProfile(profileId);
                final ProfileDescription pd = cr.getProfileDescriptionAccessControlled(profileId);
                final String profileName = pd.getName();

                String title = this.helpToMakeTitleForRssComments(profileId, "profile");
                final RssCreatorComments instance = new RssCreatorComments(
                        getApplicationBaseURI(), Integer.parseInt(limit), profileId,
                        profileName, "profile", comments, Comment.COMPARE_ON_DATE, title);
                final Rss rss = instance.getRss();
                LOG.debug("Releasing RSS of {} most recent post on profile {}", limit,
                        profileId);
                return rss;
            } catch (UserUnauthorizedException ex) {
                servletResponse.sendError(Status.FORBIDDEN.getStatusCode());
                return new Rss();
            } catch (ItemNotFoundException e) {
                servletResponse.sendError(Status.NOT_FOUND.getStatusCode());
                return new Rss();
            } catch (AuthenticationRequiredException e) {
                servletResponse.sendError(Status.UNAUTHORIZED.getStatusCode(), e.toString());
                return new Rss();
            }
        }

        /**
         *
         * @param componentId the Id of a component whose comments are to be
         * rss-ed
         * @param limit the number of items to be displayed
         * @return rss of the comments for a chosen component
         * @throws ComponentRegistryException
         * @throws IOException
         * @throws JAXBException
         * @throws ParseException
         */
        @GET
        @Path("/components/{componentId}/comments/rss")
        @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON})
        public Rss getRssOfCommentsFromComponent(
                @PathParam("componentId") String componentId,
                @QueryParam(NUMBER_OF_RSSITEMS)
                @DefaultValue("20") String limit)
                throws ComponentRegistryException, IOException, JAXBException,
                ParseException {
            try {
                ComponentRegistry cr = this.getBaseRegistry();
                final List<Comment> comments = cr.getCommentsInComponent(componentId);
                final ComponentDescription cd = cr.getComponentDescriptionAccessControlled(componentId);
                final String componentName = cd.getName();
                //oboslete. status must be involved, not boolean isPrivate
                String title = this.helpToMakeTitleForRssComments(componentId, "component");
                final RssCreatorComments instance = new RssCreatorComments(
                        getApplicationBaseURI(), Integer.parseInt(limit), componentId,
                        componentName, "component", comments, Comment.COMPARE_ON_DATE, title);
                final Rss rss = instance.getRss();
                LOG.debug("Releasing RSS of {} most recent post on component {}",
                        limit, componentId);
                return rss;
            } catch (UserUnauthorizedException e) {
                servletResponse.sendError(Status.FORBIDDEN.getStatusCode());
                return new Rss();
            } catch (ItemNotFoundException e) {
                servletResponse.sendError(Status.NOT_FOUND.getStatusCode());
                return new Rss();
            } catch (AuthenticationRequiredException e1) {
                servletResponse.sendError(Status.UNAUTHORIZED.getStatusCode());
                return new Rss();
            }
        }

        private Response createNonExistentItemResponse(String itemId) {
            LOG.error("Update of nonexistent id ({}) failed.", itemId);
            return Response
                    .serverError()
                    .entity("Invalid id, cannot update nonexistent item")
                    .build();
        }

        private Response createBadRequestResponse(final String msg) {
            return Response
                    .status(Status.BAD_REQUEST)
                    .entity(msg)
                    .build();
        }

        private Set<ComponentStatus> getStatusFilter(List<String> filterRequest, String registrySpace) {
            if (filterRequest == null || filterRequest.isEmpty() || filterRequest.size() == 1 && filterRequest.get(0).isEmpty()) {
                //defaults depending on space
                if (REGISTRY_SPACE_PUBLISHED.equals(registrySpace)) {
                    return EnumSet.of(ComponentStatus.PRODUCTION);
                } else { //private or group
                    return EnumSet.of(ComponentStatus.DEVELOPMENT);
                }
            } else if (filterRequest.contains("*")) {
                return ImmutableSet.copyOf(ComponentStatus.values());
            } else {
                final Set<ComponentStatus> statusSet = new HashSet<ComponentStatus>(filterRequest.size());
                for (String status : filterRequest) {
                    if (status != null) {
                        try {
                            statusSet.add(ComponentStatus.valueOf(status.toUpperCase()));
                        } catch (IllegalArgumentException ex) {
                            LOG.warn("Unknown status type in filter: {}", status);
                            LOG.debug("Encountered invalid type while processing status filter for {} in {}", registrySpace, filterRequest, ex);
                        }
                    }
                }
                return statusSet;
            }
        }
    }

    /**
     *
     * @param servletContext
     * @param servletRequest
     * @return The application's base URI as defined by the following context
     * parameters:
     *
     * {@link APPLICATION_URL_BASE_PARAM},
     *     {@link APPLICATION_URL_PATH_PARAM},
     *     {@link APPLICATION_URL_PROTOCOL_HEADER_PARAM}, and
     * {@link APPLICATION_URL_HOST_HEADER_PARAM}
     *
     * If correctly configured, it should look something like
     * "http://catalog.clarin.eu/ds/ComponentRegistry". <em>Be aware that this
     * can also be null if configured incorrectly!</em>
     */
    public static String getApplicationBaseURI(ServletContext servletContext, HttpServletRequest servletRequest) {
        final String path = servletContext.getInitParameter(APPLICATION_URL_PATH_PARAM);
        if (path != null) {
            final String protocolHeader = servletContext.getInitParameter(APPLICATION_URL_PROTOCOL_HEADER_PARAM);
            final String hostHeader = servletContext.getInitParameter(APPLICATION_URL_HOST_HEADER_PARAM);
            if (protocolHeader != null && hostHeader != null) {

                return String.format("%s://%s%s",
                        servletRequest.getHeader(protocolHeader),
                        servletRequest.getHeader(hostHeader),
                        path
                );
            } else {
                final String base = servletContext.getInitParameter(APPLICATION_URL_BASE_PARAM);
                return base + path;
            }
        }
        LOG.error("Application URI could not be determined. Information available:\n"
                + " {}: {}\n {}: {}\n {}: {}\n {}: {}",
                APPLICATION_URL_PATH_PARAM, servletContext.getInitParameter(APPLICATION_URL_PATH_PARAM),
                APPLICATION_URL_BASE_PARAM, servletContext.getInitParameter(APPLICATION_URL_BASE_PARAM),
                APPLICATION_URL_PROTOCOL_HEADER_PARAM, servletContext.getInitParameter(APPLICATION_URL_PROTOCOL_HEADER_PARAM),
                APPLICATION_URL_HOST_HEADER_PARAM, servletContext.getInitParameter(APPLICATION_URL_HOST_HEADER_PARAM)
        );
        throw new RuntimeException("Cannot determine application path");
    }

}
