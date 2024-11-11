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
import eu.clarin.cmdi.componentregistry.components.ComponentSpec;
import eu.clarin.cmdi.componentregistry.rest.model.BaseDescription;
import eu.clarin.cmdi.componentregistry.rest.model.ComponentStatus;
import eu.clarin.cmdi.componentregistry.rest.model.ItemType;
import eu.clarin.cmdi.componentregistry.rest.service.ComponentRegistryService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author twagoo
 */
@RestController
@RequestMapping("/registry/items")
public class ItemsController {

    final ImmutableList<ComponentStatus> DEFAULT_STATUS = ImmutableList.of(ComponentStatus.PRODUCTION);

    @Autowired
    private ComponentRegistryService registryService;

    @GetMapping(path = {}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<BaseDescription> getItems(
            @RequestParam(value = "type") Optional<ItemType> itemType,
            @RequestParam(value = "status", required = false) List<ComponentStatus> status,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "ASC") Direction sortDirection
    ) {
        return itemType.map(
                (type) -> registryService.getItemDescriptions(type,
                        CollectionUtils.isEmpty(status) ? DEFAULT_STATUS : status,
                        Optional.of(sortBy), Optional.of(sortDirection)))
                .orElseGet(
                        () -> registryService.getItemDescriptions(
                                CollectionUtils.isEmpty(status) ? DEFAULT_STATUS : status,
                                Optional.of(sortBy), Optional.of(sortDirection)));
    }

    @GetMapping(path = "/{componentId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public BaseDescription getItem(
            @PathVariable("componentId") String componentId
    ) {
        return registryService.getItemDescription(componentId);
    }

    @GetMapping(path = "/{componentId}/spec", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ComponentSpec getItemSpec(
            @PathVariable(value = "componentId") String componentId
    ) {
        return registryService.getItemSpecification(componentId);
    }

    @GetMapping(path = "/{componentId}/spec", produces = {MediaType.APPLICATION_XML_VALUE})
    public String getItemSpecXml(
            @PathVariable(value = "componentId") String componentId
    ) {
        return registryService.getItemSpecificationXml(componentId);
    }
}
