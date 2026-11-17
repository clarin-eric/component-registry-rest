/*
 * Copyright (C) 2024 CLARIN ERIC
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

import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.componentregistry.rest.model.BaseDescription;
import eu.clarin.cmdi.componentregistry.rest.model.ComponentDescription;
import eu.clarin.cmdi.componentregistry.rest.model.ComponentStatus;
import eu.clarin.cmdi.componentregistry.rest.model.ComponentsList;
import eu.clarin.cmdi.componentregistry.rest.model.ItemType;
import eu.clarin.cmdi.componentregistry.rest.model.ProfileDescription;
import eu.clarin.cmdi.componentregistry.rest.model.ProfilesList;
import eu.clarin.cmdi.componentregistry.rest.model.RegistrySpace;
import eu.clarin.cmdi.componentregistry.rest.model.RegistryUser;
import eu.clarin.cmdi.componentregistry.rest.service.AuthorizationInfoService;
import eu.clarin.cmdi.componentregistry.rest.service.ComponentRegistryService;
import eu.clarin.cmdi.componentregistry.rest.service.ItemDescriptionConverter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 *
 * @author twagoo
 */
@RestController
@Tag(name = "Components and profiles", description = "Registry of components and profiles")
@RequestMapping("/registry")
public class RegistryController {

    final ImmutableList<ComponentStatus> DEFAULT_STATUS = ImmutableList.of(ComponentStatus.PRODUCTION);

    @Autowired
    private ComponentRegistryService registryService;

    @Autowired
    private ItemDescriptionConverter itemConverter;

    @Autowired
    private AuthorizationInfoService authInfoService;

    @GetMapping(path = "/components",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ComponentsList getComponents(
            Authentication authentication,
            @RequestParam(value = "registrySpace", defaultValue = "public") RegistrySpace registrySpace,
            @RequestParam(value = "status", required = false) List<ComponentStatus> status,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "ASC") Sort.Direction sortDirection,
            @RequestParam(value = "groupId", required = false) Long groupId
    ) {
        return itemConverter.descriptionsAsComponentsList(
                getItemsOfType(ItemType.COMPONENT, registrySpace, status, sortBy, sortDirection, groupId, authentication)
        );
    }

    @GetMapping(path = "/profiles",
            produces = {
                MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_XML_VALUE})
    public ProfilesList getProfiles(
            Authentication authentication,
            @RequestParam(value = "registrySpace", defaultValue = "published") RegistrySpace registrySpace,
            @RequestParam(value = "status", required = false) List<ComponentStatus> status,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "ASC") Sort.Direction sortDirection,
            @RequestParam(value = "groupId", required = false) Long groupId
    ) {
        return itemConverter.descriptionsAsProfilesList(
                getItemsOfType(ItemType.PROFILE, registrySpace, status, sortBy, sortDirection, groupId, authentication)
        );
    }

    @GetMapping(path = "/components/{componentId}/description",
            produces = {
                MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_XML_VALUE})
    public ComponentDescription getComponentItem(
            @PathVariable("componentId") String componentId
    ) {
        return itemConverter.baseDescriptionAsComponent(
                getItemDescriptionOfType(ItemType.COMPONENT, componentId));
    }

    @GetMapping(path = "/profiles/{componentId}/description",
            produces = {
                MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_XML_VALUE})
    public ProfileDescription getProfileItem(
            @PathVariable("componentId") String componentId
    ) {
        return itemConverter.baseDescriptionAsProfile(
                getItemDescriptionOfType(ItemType.PROFILE, componentId));
    }

    private List<BaseDescription> getItemsOfType(ItemType type,
            RegistrySpace space,
            List<ComponentStatus> status,
            String sortBy, Sort.Direction sortDirection, Long groupId, Authentication authentication) {

        final Long userId = checkUserSpaceAccess(space, authentication, groupId);

        return registryService.getItemDescriptions(type, space,
                CollectionUtils.isEmpty(status) ? DEFAULT_STATUS : status,
                sortBy, sortDirection, userId, groupId);
    }

    /**
     *
     * @param space space to access
     * @param authentication current authentication state
     * @param groupId id of group to access (only processed if space == RegistrySpace.GROUP
     * @return the user ID
     * @throws An exception if the user is not authorized to access the
     * specified space
     */
    private Long checkUserSpaceAccess(RegistrySpace space, Authentication authentication, Long groupId) {
        if (space == null) {
            return null;
        } else {
            switch (space) {
                case PRIVATE:
                    //current user must be set
                    return authInfoService.getCurrentUser(authentication)
                            //TODO: throw something specific
                            .orElseThrow()
                            //return its ID
                            .getId();
                case GROUP:
                    if(groupId == null) {
                        throw new RuntimeException("Group ID not given");
                    }
                    
                    // current user must exist
                    final RegistryUser user
                            = authInfoService.getCurrentUser(authentication)
                                    //TODO: throw something specific
                                    .orElseThrow();
                    
                    // user must have access to the specified group
                    if (authInfoService.canUserAccessGroup(user, groupId)) {
                        // user is authorized, return its id
                        return user.getId();
                    } else {
                        // user is not authorized
                        throw new RuntimeException("User not authorized to access group");
                    }
                default:
                    return null;
            }
        }

    }

    private BaseDescription getItemDescriptionOfType(ItemType type, String componentId) {
        final BaseDescription item = registryService.getItemDescription(componentId);
        if (item != null && registryService.itemIsOfType(item, type)) {
            return item;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "There is no item of type " + type + " with id " + componentId);
        }
    }

}
