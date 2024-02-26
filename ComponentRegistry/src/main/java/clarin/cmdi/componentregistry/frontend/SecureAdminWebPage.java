package clarin.cmdi.componentregistry.frontend;

import java.security.Principal;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.MultiLineLabel;

import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;
import com.google.common.collect.Ordering;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public abstract class SecureAdminWebPage extends WebPage {

    public SecureAdminWebPage(final PageParameters parameters) {
        super(parameters);
        Principal userPrincipal = getUserPrincipal();
        if (!Configuration.getInstance().isAdminUser(userPrincipal)) {
            setResponsePage(new AccessDeniedPage());
        }
        if (userPrincipal == null) {
            throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            add(new MultiLineLabel("message", "Component Registry Admin Page.\nYou are logged in as: " + userPrincipal.getName() + ".\n"));
        }
    }

    protected final Principal getUserPrincipal() {
        return getHttpServletRequest().getUserPrincipal();
    }

    protected HttpServletRequest getHttpServletRequest() {
        return (HttpServletRequest) getRequest().getContainerRequest();
    }

    protected IModel<List<RegistryUser>> createUsersModel(UserDao userDao) {
        return new AbstractReadOnlyModel<List<RegistryUser>>() {

            @Override
            public List<RegistryUser> getObject() {
                // return all users sorted by their tostring value (ignoring case)
                return new Ordering<Object>() {

                    @Override
                    public int compare(Object t, Object t1) {
                        return t.toString().compareToIgnoreCase(t1.toString());
                    }

                }.sortedCopy(userDao.getAllUsers());
            }
        };
    }

    @SuppressWarnings(value = "serial")
    protected void addLinks() {
        add(new Link("home") {
            @Override
            public void onClick() {
                setResponsePage(AdminHomePage.class);
            }
        });
    }

}
