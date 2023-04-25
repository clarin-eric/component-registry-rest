/*
 * Copyright (C) 2023 CLARIN ERIC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.AuthenticationRequiredException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONException;

/**
 *
 * @author twagoo
 */
public interface IAuthenticationRestService {

    @GET
    @Produces(value = {MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Information on the current authentication state. Pass 'redirect' query parameter to make this method redirect to the URI specified as its value.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "If no query parameters are passed, with the authentications status in its body"),
        @ApiResponse(code = 303, message = "A redirect to the URI provided as the value of the 'redirect' parameter")})
    Response getAuthenticationInformation(@QueryParam(value = "redirect")
            @DefaultValue(value = "") String redirectUri) throws JSONException, AuthenticationRequiredException;

    @POST
    @ApiOperation(value = "Triggers the service to require the client to authenticate by means of the configured authentication mechanism. Notice that this might require user interaction!")
    @ApiResponses(value = {
        @ApiResponse(code = 303, message = "A redirect, either to a Shibboleth authentication page/discovery service or other identification mechanism, and ultimately to the same URI as requested (which should be picked up as a GET)"),
        @ApiResponse(code = 401, message = "If unauthenticated, a request to authenticate may be returned (not in case of Shibboleth authentication)")})
    Response triggerAuthenticationRequest();

}
