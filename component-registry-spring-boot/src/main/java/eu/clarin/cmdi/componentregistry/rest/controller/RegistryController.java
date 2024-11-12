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
import eu.clarin.cmdi.componentregistry.rest.service.ComponentRegistryService;
import eu.clarin.cmdi.componentregistry.rest.service.ItemDescriptionConverter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @GetMapping(path = "/components",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ComponentsList getComponents(
            @RequestParam(value = "status") List<ComponentStatus> status,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "ASC") Sort.Direction sortDirection
    ) {
        final List<BaseDescription> items = getItemsOfType(ItemType.COMPONENT, status, sortBy, sortDirection);
        return itemConverter.descriptionsAsComponentsList(items);
    }

    @GetMapping(path = "/profiles",
            produces = {
                MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_XML_VALUE})
    public ProfilesList getProfiles(
            @RequestParam(value = "status") List<ComponentStatus> status,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "ASC") Sort.Direction sortDirection
    ) {
        final List<BaseDescription> items = getItemsOfType(ItemType.PROFILE, status, sortBy, sortDirection);
        return itemConverter.descriptionsAsProfilesList(items);
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

    private List<BaseDescription> getItemsOfType(ItemType type, List<ComponentStatus> status,
            String sortBy, Sort.Direction sortDirection) {
        return registryService.getItemDescriptions(
                type,
                CollectionUtils.isEmpty(status) ? DEFAULT_STATUS : status,
                Optional.of(sortBy), Optional.of(sortDirection));
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
