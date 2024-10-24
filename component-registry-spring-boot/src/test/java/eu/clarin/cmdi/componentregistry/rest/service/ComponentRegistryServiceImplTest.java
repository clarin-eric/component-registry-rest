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
package eu.clarin.cmdi.componentregistry.rest.service;

import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.componentregistry.components.ComponentSpec;
import eu.clarin.cmdi.componentregistry.rest.model.BaseDescription;
import eu.clarin.cmdi.componentregistry.rest.model.RegistryUser;
import eu.clarin.cmdi.componentregistry.rest.persistence.RegistryItemRepository;
import eu.clarin.cmdi.componentregistry.rest.persistence.SpecRepository;
import eu.clarin.cmdi.componentregistry.rest.persistence.UserRepository;
import eu.clarin.cmdi.componentregistry.rest.spec.ComponentSpecMarshaller;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 *
 * @author twagoo
 */
@Testcontainers
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Sql("/sql/create.sql")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ComponentRegistryServiceImplTest {

    @Container
    @ServiceConnection
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.4-alpine3.20")
            .withDatabaseName("compreg-test")
            .withUsername("compreg")
            .withPassword("compreg");

    @Autowired
    private RegistryItemRepository itemRepository;

    @Autowired
    private SpecRepository contentRepository;

    @Autowired
    private ComponentSpecMarshaller specMarshaller;

    @Autowired
    private UserRepository userRepository;

    private ComponentRegistryServiceImpl instance;

    @BeforeEach
    public void setUp() {
        instance = new ComponentRegistryServiceImpl(itemRepository, contentRepository, specMarshaller);
    }

    @Test
    public void testGetPublishedDescriptions() {
        long userId = insertUser("TestUser");
        insertDescriptions(1001, 5, userId);

        List<BaseDescription> result = instance.getPublishedDescriptions();
        assertThat(result).isNotNull();
        assertThat(result).hasSize(5);
    }

    @Test
    public void testGetDescriptionById() {
        long userId = insertUser("TestUser");
        insertDescriptions(1001, 1, userId);

        final BaseDescription descr = instance.getItemDescription("item1001");
        assertThat(descr).isNotNull();
        assertThat(descr).hasFieldOrPropertyWithValue("id", "item1001");

        final BaseDescription descr2 = instance.getItemDescription("item1002");
        assertThat(descr2).isNull();
    }

    @Test
    public void testGetSpecById() {
        final String xml
                = """
                  <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                  <ComponentSpec isProfile="true"/>
                  """;

        final long userId = insertUser("TestUser");
        insertDescriptions(1001, 1, userId)
                .forEach(id -> {
                    //contentRepository.getContentByComponentId();
                });

        final ComponentSpec spec = instance.getItemSpecification("item1001");
        assertThat(spec).isNotNull();
        assertThat(spec).hasFieldOrPropertyWithValue("isProfile", true);
    }

    private final static AtomicInteger userIdGenerator = new AtomicInteger(100);

    private long insertUser(String name) {
        final long userId = userIdGenerator.getAndIncrement();
        RegistryUser user = RegistryUser.builder()
                .id(userId)
                .name(name)
                .principalName(name)
                .build();
        return userRepository.saveAndFlush(user).getId();
    }

    private Iterable<Long> insertDescriptions(long startId, long number, Long userId) {
        return insertDescriptions(startId, number, userId, null);
    }

    private Iterable<Long> insertDescriptions(long startId, long number, Long userId, Consumer<BaseDescription.BaseDescriptionBuilder> descriptionConfigurer) {
        final ImmutableList.Builder<BaseDescription> descriptions = ImmutableList.builder();
        for (long id = startId; id < startId + number; id++) {
            final BaseDescription.BaseDescriptionBuilder builder = BaseDescription.builder()
                    .dbId(id)
                    .dbUserId(userId)
                    .ispublic(true)
                    .componentId("item" + id)
                    .name("item" + id)
                    .description("Item number " + id);
            if (descriptionConfigurer != null) {
                // configure more
                descriptionConfigurer.accept(builder);
            }
            descriptions.add(builder.build());
        }
        return itemRepository.saveAllAndFlush(descriptions.build())
                .stream().map(BaseDescription::getDbId).toList();
    }

}
