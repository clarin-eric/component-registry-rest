package eu.clarin.cmdi.componentregistry.rest;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Component Registry API",
                version = "1.0",
                description = "Components and profiles registry for the Component Metadata Infrastructure",
                contact = @Contact(url = "https://www.clarin.eu", name = "CLARIN ERIC", email = "cmdi@clarin.eu")
        )
)
public class ComponentRegistrySpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComponentRegistrySpringBootApplication.class, args);
    }

}
