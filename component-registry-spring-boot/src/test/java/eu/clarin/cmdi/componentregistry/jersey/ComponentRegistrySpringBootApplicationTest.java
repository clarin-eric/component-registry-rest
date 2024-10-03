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
package eu.clarin.cmdi.componentregistry.jersey;

import eu.clarin.cmdi.componentregistry.jersey.configuration.JerseyConfiguration;
import eu.clarin.cmdi.componentregistry.jersey.persistence.RegistryItemRepository;
import eu.clarin.cmdi.componentregistry.jersey.persistence.UserRepository;
import eu.clarin.cmdi.componentregistry.jersey.resource.ComponentRegistryResource;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ComponentRegistrySpringBootApplicationTest {

    @Autowired
    private RegistryItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComponentRegistryResource registryResource;

    @Autowired
    private JerseyConfiguration jerseyConfiguration;

    @Test
    void contextLoads() {
        assertThat(itemRepository).isNotNull();
        assertThat(userRepository).isNotNull();
        assertThat(registryResource).isNotNull();
        assertThat(jerseyConfiguration).isNotNull();
        assertThat(jerseyConfiguration.isRegistered(ComponentRegistryResource.class)).isTrue();
    }

}
