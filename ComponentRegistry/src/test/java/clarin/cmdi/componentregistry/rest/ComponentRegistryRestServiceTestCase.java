package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.JAXBContextResolver;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.ComponentDescription;
import clarin.cmdi.componentregistry.model.Group;
import clarin.cmdi.componentregistry.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;
import com.google.common.collect.ImmutableMap;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.multipart.impl.FormDataMultiPartDispatchProvider;
import com.sun.jersey.spi.container.servlet.WebComponent;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;

/**
 * Base test that starts a servlet container with the component registry
 *
 * @author george.georgovassilis@mpi.nl
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:spring-config/applicationContext.xml",
    "classpath:spring-config/test-applicationContext-fragment.xml"})
//Important: these tests can not be configured with @Transactional because it spawns two (mutually deadlocking) transactions: the test itself and jersey services
public abstract class ComponentRegistryRestServiceTestCase extends JerseyTest {
    // CommandLine test e.g.: curl -i -H "Accept:application/json" -X GET
    // http://localhost:8080/ComponentRegistry/rest/registry/profiles

    protected final static GenericType<List<ProfileDescription>> PROFILE_LIST_GENERICTYPE = new GenericType<List<ProfileDescription>>() {
    };
    protected final static GenericType<List<ComponentDescription>> COMPONENT_LIST_GENERICTYPE = new GenericType<List<ComponentDescription>>() {
    };
    protected final static GenericType<List<Comment>> COMMENT_LIST_GENERICTYPE = new GenericType<List<Comment>>() {
    };
    protected final static GenericType<List<Group>> GROUP_LIST_GENERICTYPE = new GenericType<List<Group>>() {
    };
    protected final static GenericType<List<String>> STRING_LIST_GENERICTYPE = new GenericType<List<String>>() {
    };

    @Autowired
    private UserDao userDao;

    protected String getApplicationContextFile() {
        // sorry for the duplication, but JerseyTest is not aware of
        // @ContextConfiguration
        return "classpath:spring-config/applicationContext.xml, classpath:spring-config/test-applicationContext-fragment.xml";
    }

    @Override
    protected AppDescriptor configure() {
        final DefaultClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getClasses().add(JAXBContextResolver.class);

        WebAppDescriptor.Builder builder = new WebAppDescriptor.Builder()
                .clientConfig(clientConfig)
                .contextParam("contextConfigLocation",
                        getApplicationContextFile())
                .contextParam("eu.clarin.cmdi.componentregistry.serviceUrlBase", "localhost") // deliberately inaccurate,
                .contextParam("eu.clarin.cmdi.componentregistry.serviceUrlPath", "test") // for use of user/front end only
                .servletClass(SpringServlet.class)
                .initParam(WebComponent.RESOURCE_CONFIG_CLASS,
                        ClassNamesResourceConfig.class.getName())
                .initParam(
                        ClassNamesResourceConfig.PROPERTY_CLASSNAMES,
                        FormDataMultiPartDispatchProvider.class.getName() + ";"
                        + ComponentRegistryRestService.class.getName())
                .addFilter(DummySecurityFilter.class, "DummySecurityFilter", getSecurityFilterInitParams())
                .requestListenerClass(RequestContextListener.class)
                .contextListenerClass(ContextLoaderListener.class);
        return builder.build();
    }

    protected WebResource getResource() {
        return resource();
    }

    protected Builder getAuthenticatedResource(String path) {
        return getAuthenticatedResource(getResource().path(path));
    }

    protected Builder getAuthenticatedResource(WebResource resource) {
        return getAuthenticatedResource(DummyPrincipal.DUMMY_PRINCIPAL, resource);
    }

    protected Builder getAuthenticatedResource(Principal principal, WebResource resource) {
        return resource.header(HttpHeaders.AUTHORIZATION,
                "Basic "
                + new String(Base64
                        .encode(principal
                                .getName() + ":dummy")));
    }

    protected RegistryUser createUserRecord() {
        RegistryUser user = new RegistryUser();
        user.setName("Database test user");
        user.setPrincipalName(DummyPrincipal.DUMMY_PRINCIPAL.getName());
        return userDao.save(user);
    }

    protected Number getExpectedUserId(String principal) {
        return getUserDao().getByPrincipalName(principal).getId();
    }

    protected UserDao getUserDao() {
        return userDao;
    }

    protected Map<String, String> getSecurityFilterInitParams() {
        return Collections.emptyMap();
    }

}
