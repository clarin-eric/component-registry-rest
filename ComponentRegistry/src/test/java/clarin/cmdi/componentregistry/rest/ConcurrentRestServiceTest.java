package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.RegistrySpace;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryTestDatabase;
import clarin.cmdi.componentregistry.BaseDescription;
import clarin.cmdi.componentregistry.ComponentDescription;
import clarin.cmdi.componentregistry.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegisterResponse;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.multipart.FormDataMultiPart;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestService.REGISTRY_SPACE_PARAM;
import static clarin.cmdi.componentregistry.rest.ComponentRegistryRestServiceTest.REGISTRY_BASE;
import static org.junit.Assert.*;

/**
 *
 * @author george.georgovassilis@mpi.nl
 *
 */
public class ConcurrentRestServiceTest extends
        ComponentRegistryRestServiceTestCase {

    private final static Logger LOG = LoggerFactory
            .getLogger(ConcurrentRestServiceTest.class);
    private final int NR_OF_PROFILES = 20;
    private final int NR_OF_COMPONENTS = 20;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void init() {
        ComponentRegistryTestDatabase.resetAndCreateAllTables(jdbcTemplate);
        createUserRecord();
    }

    @Test
    public void testConcurrentRegisterProfile() throws Exception {
        final List<String> errors = new ArrayList<>();
        final List<Thread> ts = new ArrayList<>();

        registerProfiles(ts, NR_OF_PROFILES, errors, RegistrySpace.PRIVATE);
        //a profile can be first registered (created) only in private space, and then moved.
        //registerProfiles(ts, NR_OF_PROFILES, errors, RegistrySpace.PUBLISHED);

        registerComponents(ts, NR_OF_COMPONENTS, errors, RegistrySpace.PRIVATE);
        //a profile can be first registered (created) only in private space, and then moved.
        //registerComponents(ts, NR_OF_COMPONENTS, errors, RegistrySpace.PUBLISHED);
        runAllThreads(ts);
        if (errors.size() > 0) {
            System.out.println(Arrays.toString(errors.toArray()));
            for (String e : errors) {
                System.err.println(e);
            }
            fail();
        }
        assertProfiles(NR_OF_PROFILES, RegistrySpace.PRIVATE);
        //assertProfiles(NR_OF_PROFILES, RegistrySpace.PUBLISHED);

        assertComponents(NR_OF_COMPONENTS, RegistrySpace.PRIVATE);
        //assertComponents(NR_OF_COMPONENTS, RegistrySpace.PUBLISHED);
    }

    private void assertProfiles(int nrOfProfiles, RegistrySpace registrySpace) {
        List<ProfileDescription> response = getAuthenticatedResource(
                getResource().path(REGISTRY_BASE + "/profiles").queryParam(
                REGISTRY_SPACE_PARAM, registrySpace.name())).accept(
                        MediaType.APPLICATION_XML).get(PROFILE_LIST_GENERICTYPE);
        Collections.sort(response, descriptionComparator);
        assertEquals("half should be deleted", nrOfProfiles / 2,
                response.size());
        for (int i = 0; i < nrOfProfiles / 2; i++) {
            ProfileDescription desc = response.get(i);
            assertEquals(makeValidNcName("Test Profile" + (i * 2 + 1000)), desc.getName());
            assertEquals(makeValidNcName("Test Profile" + (i * 2 + 1000)) + " description",
                    desc.getDescription());
        }
    }

    private Comparator<BaseDescription> descriptionComparator = new Comparator<BaseDescription>() {

        @Override
        public int compare(BaseDescription o1, BaseDescription o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    private void assertComponents(int nrOfComponents, RegistrySpace registrySpace) {
        List<ComponentDescription> cResponse = getAuthenticatedResource(
                getResource().path(REGISTRY_BASE + "/components").queryParam(
                REGISTRY_SPACE_PARAM, registrySpace.name())).accept(
                        MediaType.APPLICATION_XML).get(COMPONENT_LIST_GENERICTYPE);
        Collections.sort(cResponse, descriptionComparator);
        assertEquals("half should be deleted", nrOfComponents / 2,
                cResponse.size());
        for (int i = 0; i < nrOfComponents / 2; i++) {
            ComponentDescription desc = cResponse.get(i);
            assertEquals(makeValidNcName("Test Component" + (i * 2 + 1000)), desc.getName());
            assertEquals(makeValidNcName("Test Component" + (i * 2 + 1000)) + " description",
                    desc.getDescription());
        }
    }

    private void runAllThreads(List<Thread> ts) throws InterruptedException {
        for (Thread thread : ts) {
            thread.start();
            thread.join(10);
        }
        for (Thread thread : ts) {
            thread.join(); // Wait till all are finished
        }
    }

    private void registerProfiles(List<Thread> ts, int size,
            final List<String> errors, RegistrySpace registrySpace)
            throws InterruptedException {
        for (int i = 0; i < size; i++) {
            final boolean shouldDelete = (i % 2) == 1;
            LOG.debug("Profile {} should be registered in {} and {}",
                    new Object[]{i + 1000,
                        registrySpace.name(),
                        shouldDelete ? "ALSO DELETED" : "not deleted"});
            final String name = makeValidNcName("Test Profile" + (i + 1000));
            Thread thread = createThread(REGISTRY_BASE + "/profiles/", registrySpace, name, shouldDelete,
                    RegistryTestHelper.getTestProfileContent(name), errors);
            ts.add(thread);
        }
    }

    private void registerComponents(List<Thread> ts, int size,
            final List<String> errors, RegistrySpace registrySpace)
            throws InterruptedException {
        for (int i = 0; i < size; i++) {
            final boolean shouldDelete = (i % 2) == 1;
            LOG.debug("Component {} should be registered in {} and {}",
                    new Object[]{i + 1000,
                        registrySpace.name(),
                        shouldDelete ? "ALSO DELETED" : "not deleted"});
            final String name = makeValidNcName("Test Component" + (i + 1000));
            Thread thread = createThread(REGISTRY_BASE + "/components/", registrySpace, name, shouldDelete,
                    RegistryTestHelper.getComponentTestContent(name), errors);
            ts.add(thread);
        }
    }

    private Thread createThread(final String path, final RegistrySpace registrySpace,
            final String name, final boolean alsoDelete, InputStream content,
            final List<String> errors) throws InterruptedException {
        final FormDataMultiPart form = new FormDataMultiPart();
        form.field(ComponentRegistryRestService.DATA_FORM_FIELD, content,
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.field(ComponentRegistryRestService.NAME_FORM_FIELD, name);
        form.field(ComponentRegistryRestService.DESCRIPTION_FORM_FIELD, name
                + " description");
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                long timestamp = -System.currentTimeMillis();
                try {
                    // System.out.println("THREAD STARTED"+Thread.currentThread().getName());
                    RegisterResponse registerResponse = getAuthenticatedResource(
                            getResource().path(path)).type(
                            MediaType.MULTIPART_FORM_DATA).post(
                                    RegisterResponse.class, form);
                    if (!registerResponse.isRegistered()) {
                        errors.add("Failed to register "
                                + Arrays.toString(registerResponse.getErrors()
                                        .toArray()));
                    }
                    LOG.debug(">>>>>>>>>>>>>>>> [Thread " + hashCode()
                            + "] REGISTERING DESCRIPTION " + name + " "
                            + registerResponse.getDescription().getId()
                            + registrySpace.name()
                            + (alsoDelete ? " alsoDelete" : ""));
                    if (alsoDelete) {
                        LOG.debug(">>>>>>>>>>>>>>>> [Thread "
                                + hashCode()
                                + "] DELETING DESCRIPTION "
                                + name
                                + " "
                                + registerResponse.getDescription().getId()
                                + registrySpace.name()
                                + (alsoDelete ? " alsoDelete" : ""));
                        ClientResponse response = getAuthenticatedResource(
                                getResource().path(
                                        path
                                        + registerResponse
                                        .getDescription()
                                        .getId()))
                                .delete(ClientResponse.class);
                        if (response.getStatus() != 200) {
                            errors.add("Failed to delete "
                                    + registerResponse.getDescription());
                        }
                    }
                    // System.out.println("THREAD FINISHED"+Thread.currentThread().getName());
                } finally {
                    timestamp += System.currentTimeMillis();
                    LOG.info(Thread.currentThread().getName() + " duration: " + timestamp + " ms");
                }
            }
        });
        return t;

    }

    private String makeValidNcName(String string) {
        return string.replaceAll(" ", "_")
                .replaceAll("0", "a")
                .replaceAll("1", "b")
                .replaceAll("2", "c")
                .replaceAll("3", "d")
                .replaceAll("4", "e")
                .replaceAll("5", "f")
                .replaceAll("6", "g")
                .replaceAll("7", "h")
                .replaceAll("8", "i")
                .replaceAll("9", "j");
    }
}
