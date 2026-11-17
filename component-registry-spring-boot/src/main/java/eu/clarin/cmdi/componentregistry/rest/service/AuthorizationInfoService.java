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
package eu.clarin.cmdi.componentregistry.rest.service;

import eu.clarin.cmdi.componentregistry.rest.model.RegistryUser;
import eu.clarin.cmdi.componentregistry.rest.model.UserGroup;
import eu.clarin.cmdi.componentregistry.rest.persistence.UserRepository;
import static java.lang.StrictMath.log;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

/**
 *
 * @author twagoo
 */
@Service
@Slf4j
public class AuthorizationInfoService {

    private final UserRepository repository;

    public AuthorizationInfoService(UserRepository repository) {
        this.repository = repository;
    }

    public Optional<RegistryUser> getCurrentUser(Authentication authentication) {
        if (authentication != null
                && authentication.getPrincipal() instanceof User principal) {
            return repository.findByPrincipalName(principal.getUsername());
        } else {
            return Optional.empty();
        }
    }

    public void checkUserInfoAccessPermissions(Optional<Long> userId, Authentication authentication, Optional<String> principalName)
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

    public boolean canAccessUserInfo(Authentication authentication, Long id) throws AccessDeniedException {
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

    public boolean canAccessUserInfo(Authentication authentication, String principal) throws AccessDeniedException {
        if (authentication == null) {
            throw new AccessDeniedException("Need to be authenticated to request a user's info");
        } else {
            if (authentication.getPrincipal() instanceof User authPrincipal) {
                if (principal.equals(authPrincipal.getUsername())) {
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

    public boolean canUserAccessGroup(RegistryUser user, Long groupId) {
        return user
                .getGroups().stream()
                .map(UserGroup::getId)
                .anyMatch(id -> groupId.equals(id));
    }
}
