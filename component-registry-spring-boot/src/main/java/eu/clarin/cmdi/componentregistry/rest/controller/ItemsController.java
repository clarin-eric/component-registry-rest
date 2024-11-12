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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 *
 * @author twagoo
 */
@RestController
@Tag(name = "Items", description = "Components and profiles as items (common data model)")
@RequestMapping("/registry/items")
public class ItemsController {

    final ImmutableList<ComponentStatus> DEFAULT_STATUS = ImmutableList.of(ComponentStatus.PRODUCTION);

    @Autowired
    private ComponentRegistryService registryService;

    @Operation(summary = "Get a filtered list of descriptions of profiles and/or components")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "A list of items that meet the filter criteria (if applicable)",
                content = {
                    @Content(
                            mediaType = MediaType.APPLICATION_XML_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = BaseDescription.class))),
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = BaseDescription.class)))})})
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

    @Operation(summary = "Get the description of a profile or component")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "A description of the identified item",
                content = {
                    @Content(
                            mediaType = MediaType.APPLICATION_XML_VALUE,
                            schema = @Schema(implementation = BaseDescription.class)),
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BaseDescription.class))}),
        @ApiResponse(
                responseCode = "404",
                description = "Item not found",
                content = @Content)})
    @GetMapping(path = "/{componentId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public BaseDescription getItem(
            @PathVariable("componentId") String componentId
    ) {
        final BaseDescription description = registryService.getItemDescription(componentId);
        if (description == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            return description;
        }
    }

    @Operation(summary = "Get the specification for the profile or component")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = """
                              The component specification of the identified item. 
                              The JSON representation is derived from the primary
                              specification which is stored as XML.
                              """,
                content = {
                    @Content(
                            mediaType = MediaType.APPLICATION_XML_VALUE,
                            schema = @Schema(implementation = ComponentSpec.class)),
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ComponentSpec.class))}),
        @ApiResponse(
                responseCode = "404",
                description = "Item not found",
                content = @Content)})
    @GetMapping(path = "/{componentId}/spec", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> getItemSpec(
            @PathVariable(value = "componentId") String componentId,
            @RequestHeader(value = "Accept", required = false) String acceptHeader
    ) {
        /**
         * Produce JSON or XML depending on Accept header
         */
        final List<MediaType> mediaTypes = MediaType.parseMediaTypes(acceptHeader);
        if (!CollectionUtils.isEmpty(mediaTypes) && mediaTypes.contains(MediaType.APPLICATION_XML)) {
            /**
             * If XML is requested, we can return the original XML content from
             * the database and avoid re-serialization
             */
            return getItemSpecXml(componentId);
        } else {
            /**
             * Otherwise we return the object (which will be serialized to JSON
             * by the framework)
             */
            return getSpecObject(componentId);
        }
    }

    private ResponseEntity<String> getItemSpecXml(String componentId) {
        final String xml = registryService.getItemSpecificationXml(componentId);
        if (xml == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(xml);
        }
    }

    private ResponseEntity<ComponentSpec> getSpecObject(String componentId) {
        final ComponentSpec spec = registryService.getItemSpecification(componentId);
        if (spec == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(spec);
        }
    }
}
