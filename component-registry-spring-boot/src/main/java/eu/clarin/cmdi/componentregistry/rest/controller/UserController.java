/*
 * Copyright (C) 2026 CLARIN ERIC
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
package eu.clarin.cmdi.componentregistry.rest.controller;

import com.google.common.collect.Lists;
import eu.clarin.cmdi.componentregistry.rest.model.RegistryUser;
import eu.clarin.cmdi.componentregistry.rest.model.UserGroup;
import eu.clarin.cmdi.componentregistry.rest.persistence.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author twagoo
 */
@RestController
@Tag(name = "User", description = "Information on the user")
@RequestMapping("/user")
public class UserController {

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public class UserNotFoundException extends RuntimeException {

        public UserNotFoundException() {
        }

        public UserNotFoundException(String message) {
            super(message);
        }

    }

    @Autowired
    private UserRepository repository;

    @Operation(summary = "Info for the current user")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "User info")})
    @GetMapping(path = {}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public RegistryUser getInfo(@RequestParam(required = false) Optional<Long> userId) {
        userId.ifPresent((id) -> {
            //TODO: check if user is allowed to request info for user
        });

        //Produce user info
        return userId.map(id -> repository.findById(id))
                //if userId not provided
                .orElseGet(() -> repository.findById(getCurrentUser()))
                //if user not found
                .orElseThrow(() -> new UserNotFoundException("User not found or not allowed to find it"));
    }

    @Operation(summary = "Get the user's teams")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "A list of teams of which the user is a member")})
    @GetMapping(path = "/teams", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<UserGroup> getTeams(@RequestParam(required = false) Optional<Long> userId) {
        userId.ifPresent((id) -> {
            //TODO: check if user is allowed to request info for user
        });

        final Long id = userId.orElseGet(this::getCurrentUser);

        final Optional<Set<UserGroup>> retrieved = repository.findById(id)
                .map(RegistryUser::getGroups);

        //return as a list, or if none found the empty list
        return retrieved.<List>map(group -> Lists.newArrayList(group))
                .orElseGet(Collections::emptyList);

    }

    private Long getCurrentUser() {
        //TODO: retrieve current user id
        final Long currentUser = 1L;
        return currentUser;
    }

}
