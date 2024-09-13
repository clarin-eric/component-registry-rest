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
package eu.clarin.cmdi.componentregistry.jersey.controller;

import eu.clarin.cmdi.componentregistry.jersey.service.ComponentRegistryService;
import eu.clarin.cmdi.componentregistry.jersey.model.BaseDescription;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author twagoo
 */
@Component
@Path("/registry")
public class ComponentRegistryController {

    @Autowired
    private ComponentRegistryService registryService;

    @GET
    @Path("/test")
    @Produces(APPLICATION_JSON)
    public BaseDescription getTestItem() {
        return registryService.getTestComponent();
    }

}
