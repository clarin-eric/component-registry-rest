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
package eu.clarin.cmdi.componentregistry.jersey.configuration;

import eu.clarin.cmdi.componentregistry.jersey.resource.ComponentRegistryResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author twagoo
 */
@Configuration
@ApplicationPath("/rest")
@OpenAPIDefinition(
        info = @Info(
                title = "Component Registry API",
                version = "1.0",
                description = "Components and profiles registry for the Component Metadata Infrastructure",
                contact = @Contact(url = "https://www.clarin.eu", name = "CLARIN ERIC", email = "cmdi@clarin.eu")
        )
)
public class JerseyConfiguration extends ResourceConfig {

    @PostConstruct
    public void init() {
        //OpenAPI
        packages(true, "eu.clarin.cmdi.componentregistry.jersey");
        register(OpenApiResource.class);

        register(ComponentRegistryResource.class);
    }
}
