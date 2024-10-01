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
package eu.clarin.cmdi.componentregistry.jersey.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import eu.clarin.cmdi.componentregistry.jersey.model.BaseDescription;
import eu.clarin.cmdi.componentregistry.jersey.model.RegistryUser;
import eu.clarin.cmdi.componentregistry.jersey.persistence.RegistryItemRepository;
import eu.clarin.cmdi.componentregistry.jersey.persistence.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
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
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.4-alpine3.20")
            .withDatabaseName("compreg-test")
            .withUsername("compreg")
            .withPassword("compreg");

    @Autowired
    private RegistryItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private ComponentRegistryServiceImpl instance;

    @BeforeEach
    public void setUp() {
        instance = new ComponentRegistryServiceImpl(itemRepository);
    }

    @Test
    public void testGetPublishedComponents() {
        //TODO: inject some test data

        final long userId = 1L;
        userRepository.saveAndFlush(createUser(userId));

        itemRepository.saveAllAndFlush(ImmutableList.of(BaseDescription.builder()
                .dbId(1001L)
                .dbUserId(userId)
                .ispublic(true)
                .componentId("item1")
                .name("item1")
                .description("item 1")
                .build()
        ));

        List<BaseDescription> result = instance.getPublishedComponents();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    private static RegistryUser createUser(final long userId) {
        return RegistryUser.builder()
                .id(userId)
                .name("TestUser")
                .principalName("TestUser")
                .build();
    }

}
