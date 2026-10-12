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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
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
@Slf4j
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

    @Operation(summary = "Info for a user, normally the current user")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "User info")})
    @GetMapping(path = {}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public RegistryUser getInfo(@RequestParam(required = false) Optional<Long> userId,
            @RequestParam(required = false) Optional<String> principalName,
            Authentication authentication
    ) {
        checkUserInfoAccessPermissions(userId, authentication, principalName);

        //Produce user info
        return userId.flatMap(id -> repository.findById(id))
                //if userId not provided
                .or(() -> principalName.flatMap(repository::findByPrincipalName))
                //if principal name also not provided
                .or(() -> getCurrentUser(authentication))
                //if user not found
                .orElseThrow(() -> new UserNotFoundException("User not found or not allowed to find it"));
    }

    @Operation(summary = "Get a user's teams")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "A list of teams of which the user is a member")})
    @GetMapping(path = "/teams", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<UserGroup> getTeams(
            @RequestParam(required = false) Optional<Long> userId,
            Authentication authentication) {
        //Check if request for another user is allowed, 
        //or if the requested user is the current user
        checkUserInfoAccessPermissions(userId, authentication, Optional.empty());

        //Get the user
        final Optional<RegistryUser> user
                = userId.or(() -> getCurrentUser(authentication)
                .map(RegistryUser::getId))
                        //id to user
                        .flatMap(repository::findById);

        //Get the teams ('user groups')
        final Optional<List> groups = user
                .map(RegistryUser::getGroups)
                .map(Lists::newArrayList);

        //if the groups are found we return them, or else an empty list
        return groups.orElseGet(Collections::emptyList);

    }

    private Optional<RegistryUser> getCurrentUser(Authentication authentication) {
        if (authentication != null
                && authentication.getPrincipal() instanceof User principal) {
            return repository.findByPrincipalName(principal.getUsername());
        } else {
            return Optional.empty();
        }
    }

    private void checkUserInfoAccessPermissions(Optional<Long> userId, Authentication authentication, Optional<String> principalName)
            throws AccessDeniedException {
        //Check if request for another user is allowed,
        //or if the requested user is the current user
        userId.ifPresent((id) -> {
            if (!canAccessUserInfo(authentication, id)) {
                throw new AccessDeniedException("Cannot access user info for the requested user");
            }
        });

        principalName.ifPresent((principal) -> {
            if (!canAccessUserInfo(authentication, principal)) {
                throw new AccessDeniedException("Cannot access user info for the requested user");
            }
        });
    }

    private boolean canAccessUserInfo(Authentication authentication, Long id) throws AccessDeniedException {
        if (authentication == null) {
            throw new AccessDeniedException("Need to be authenticated to request a user's info");
        } else {
            if (authentication.getPrincipal() instanceof User authPrincipal) {
                if (repository
                        .findByPrincipalName(authPrincipal.getUsername())
                        .filter(user -> Objects.equals(id, user.getId()))
                        .isPresent()) {
                    //allowed
                    return true;
                } else {
                    return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));
                }
            } else {
                log.warn("Unexpected principal in authentication context:", authentication);
                throw new RuntimeException("Principal name not set or not a string");
            }
        }
    }

    private boolean canAccessUserInfo(Authentication authentication, String principal) throws AccessDeniedException {
        if (authentication == null) {
            throw new AccessDeniedException("Need to be authenticated to request a user's info");
        } else {
            if (authentication.getPrincipal() instanceof User authPrincipal) {
                if (Objects.equals(principal, authPrincipal.getUsername())) {
                    //allowed
                    return true;
                } else {
                    return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"));
                }
            } else {
                log.warn("Unexpected principal in authentication context:", authentication);
                throw new RuntimeException("Principal name not set or not a string");
            }
        }
    }

}
