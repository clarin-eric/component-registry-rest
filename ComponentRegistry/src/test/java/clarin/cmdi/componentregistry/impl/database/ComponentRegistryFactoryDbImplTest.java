package clarin.cmdi.componentregistry.impl.database;

import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.Before;

import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.BaseUnitTest;
import clarin.cmdi.componentregistry.model.ComponentRegistry;
import clarin.cmdi.componentregistry.OwnerUser;
import clarin.cmdi.componentregistry.RegistrySpace;
import clarin.cmdi.componentregistry.ShhaaUserCredentials;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.rest.DummyPrincipal;

import org.springframework.beans.factory.annotation.Autowired;
import org.junit.Test;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author George.Georgovassilis@mpi.nl
 */
public class ComponentRegistryFactoryDbImplTest extends BaseUnitTest {

    @Autowired
    private ComponentRegistryFactoryDbImpl componentRegistryFactory;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void init() {
        ComponentRegistryTestDatabase.resetDatabase(jdbcTemplate);
        ComponentRegistryTestDatabase.createTableRegistryUser(jdbcTemplate);
    }

    @Test
    public void testInjection() {
        assertNotNull(componentRegistryFactory);
        assertNotNull(jdbcTemplate);
    }

    @Test
    public void testGetPublicRegistry() {
        ComponentRegistry registry = componentRegistryFactory
                .getPublicRegistry();
        assertNotNull(registry);

    }

    @Test
    public void testGetBaseRegistry() {
        ComponentRegistry registry = componentRegistryFactory.getBaseRegistry(DummyPrincipal.DUMMY_CREDENTIALS);
        assertNotNull(registry);
        assertNotNull(registry.getRegistryOwner());

    }

    @Test
    public void getComponentRegistry() throws UserUnauthorizedException {
        // Get public
        assertNotNull(componentRegistryFactory.getComponentRegistry(
                RegistrySpace.PUBLISHED, null, null, null));

        // Get for non-existing user
        final RegistryUser testUser = UserDaoTest.createTestUser();
        ShhaaUserCredentials credentials = new DummyPrincipal(
                testUser.getPrincipalName()).getCredentials();

        final ComponentRegistry cr1 = componentRegistryFactory
                .getComponentRegistry(RegistrySpace.PRIVATE, null,
                        credentials, null);
        assertNotNull(cr1);
        
        // Get for existing user
        final ComponentRegistry cr2 = componentRegistryFactory
                .getComponentRegistry(RegistrySpace.PRIVATE, null,
                        credentials, null);
        assertNotNull(cr2);
        assertEquals(cr1.getRegistryOwner(), cr2.getRegistryOwner());

        // Get for another new user
        final ShhaaUserCredentials credentials2 = new DummyPrincipal(
                testUser.getPrincipalName() + "2").getCredentials();
        final ComponentRegistry cr3 = componentRegistryFactory
                .getComponentRegistry(RegistrySpace.PRIVATE, null,
                        credentials2, null);
        assertNotNull(cr3);
        assertNotSame(cr1.getRegistryOwner(), cr3.getRegistryOwner());
    }

    @Test
    public void testGetOtherUserComponentRegistry()
            throws UserUnauthorizedException {
        ShhaaUserCredentials userCredentials = DummyPrincipal.DUMMY_PRINCIPAL
                .getCredentials();

        // Create registry for new user
        final ComponentRegistry cr1 = componentRegistryFactory
                .getComponentRegistry(RegistrySpace.PRIVATE, null,
                        userCredentials, null);

        final Number id = cr1.getRegistryOwner().getId();

        // Get it as admin
        final ComponentRegistry cr2 = componentRegistryFactory
                .getOtherUserComponentRegistry(
                        DummyPrincipal.DUMMY_ADMIN_PRINCIPAL, new OwnerUser(id));
        assertNotNull(cr2);
        // Should be this user's registry
        assertEquals(cr1.getRegistryOwner(), cr2.getRegistryOwner());

        // Try get it as non-admin
        try {
            componentRegistryFactory.getOtherUserComponentRegistry(
                    DummyPrincipal.DUMMY_PRINCIPAL, new OwnerUser(id));
            fail("Non-admin can get other user's component registry");
        } catch (Exception ex) {
            // Exception should occur
        }
    }
}
