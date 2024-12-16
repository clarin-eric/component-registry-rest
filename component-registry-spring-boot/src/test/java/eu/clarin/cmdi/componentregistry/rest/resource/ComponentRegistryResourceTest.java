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
package eu.clarin.cmdi.componentregistry.rest.resource;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import org.springframework.boot.test.web.server.LocalServerPort;
import static io.restassured.RestAssured.*;
import io.restassured.http.ContentType;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author twagoo
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ComponentRegistryResourceTest {

    @LocalServerPort
    private Integer port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
    }

    /**
     * Test of getItems method, of class ComponentRegistryResource.
     */
    @Test
    public void testGetItems() {
        given().contentType(ContentType.JSON)
                ///////                
                .when()
                .get("/registry/items")
                ///////
                .then()
                .statusCode(200)
                .body("baseDescriptions", notNullValue());
    }

}
