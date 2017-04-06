/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clarin.cmdi.componentregistry.frontend;

import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.UserCredentials;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;

import java.security.Principal;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.dao.DataAccessException;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class UserSettingsPage extends WebPage {

    @SpringBean
    private UserDao userDao;
    @SpringBean(name = "componentRegistryFactory")
    private ComponentRegistryFactory componentRegistryFactory;
    private final RegistryUser registryUser;

    public UserSettingsPage(PageParameters parameters) {
	super(parameters);

        final HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();
        final Principal userPrincipal = request.getUserPrincipal();
        registryUser = componentRegistryFactory.getOrCreateUser(new UserCredentials(userPrincipal));

	add(new Label("userName", registryUser.getPrincipalName()));

	add(new FeedbackPanel("feedback"));
	add(new SettingsForm("settingsForm"));
    }

    private class SettingsForm extends Form {

	public SettingsForm(String id) {
	    super(id);

	    final TextField displayNameField = new RequiredTextField<String>("displayName", new IModel<String>() {

		@Override
		public String getObject() {
		    return registryUser.getName();
		}

		@Override
		public void setObject(String newName) {
		    registryUser.setName(newName);
		    try {
			RegistryUser u = userDao.findOne(registryUser.getId().longValue());
			u.setName(newName);
			userDao.saveAndFlush(u);
			info("User info has been updated");
		    } catch (DataAccessException daEx) {
			error("Database error: " + daEx.getMessage());
		    }
		}

		@Override
		public void detach() {
		}
	    });
	    add(displayNameField);
	}
    }
}
