package clarin.cmdi.componentregistry.frontend;

import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;
import com.google.common.collect.Ordering;
import java.util.List;
import joptsimple.internal.Strings;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class Accounts extends SecureAdminWebPage {

    private Logger logger = LoggerFactory.getLogger(Accounts.class);

    @SpringBean
    private UserDao userDao;
    private final IModel<List<RegistryUser>> usersModel;

    public Accounts(PageParameters parameters) {
        super(parameters);
        super.addLinks();

        usersModel = createUsersModel(userDao);

        add(new FeedbackPanel("feedback"));

        //manage account
        final IModel<RegistryUser> principalModel = new CompoundPropertyModel<>(null);
        add(new Form("accountSelect")
                .add(new DropDownChoice<>("principal", principalModel, usersModel)));
        add(new ManageUserForm("userDetails", principalModel)
                .setDefaultModel(principalModel)
                .add(new Label("id"))
                .add(new TextField("name").setRequired(true))
                .add(new TextField("principalName").setRequired(true))
                .add(new AccountDeleteButton("delete", principalModel))
                .add(new Behavior() {
                    @Override
                    public void onConfigure(Component component) {
                        component.setVisible(principalModel.getObject() != null);
                    }

                })
        );

        //create account
        final IModel<RegistryUser> newPrincipalModel = new CompoundPropertyModel<>(new RegistryUser());
        add(new CreateUserForm("newAccount", newPrincipalModel)
                .setDefaultModel(newPrincipalModel)
                .add(new TextField("principalName").setRequired(true))
                .add(new TextField("name").setRequired(true))
        );

    }

    private class AccountDeleteButton extends Link {

        private final IModel<RegistryUser> principalModel;

        public AccountDeleteButton(String id, IModel<RegistryUser> model) {
            super(id);
            this.principalModel = model;
        }

        @Override
        public void onClick() {
            if (principalModel.getObject() != null) {
                final String principalName = principalModel.getObject().getPrincipalName();
                logger.info("Deleting user '{}'", principalName);
                try {
                    userDao.delete(principalModel.getObject());
                    info(String.format("User '%s' deleted", principalName));
                    principalModel.setObject(null);
                } catch (RuntimeException ex) {
                    logger.warn("Failed to delete user {}", principalName, ex);
                    error("Failed to delete user: " + ex.getMessage());
                }
            }
        }

    }

    private class ManageUserForm extends Form<RegistryUser> {

        public ManageUserForm(String id, IModel<RegistryUser> model) {
            super(id, model);
        }

        @Override
        protected void onSubmit() {
            final RegistryUser modified = getModelObject();
            if (modified != null) {
                logger.info("Updating user {}", modified.getId());

                final RegistryUser target = userDao.getPrincipalNameById(modified.getId());

                if (!target.getName().equals(modified.getName())) {
                    logger.info("Setting display name '{}' to '{}' for user {}", target.getName(), modified.getName(), target.getId());
                    target.setName(modified.getName());
                }

                if (!target.getPrincipalName().equals(modified.getPrincipalName())) {
                    //check if new principal name not already taken
                    if (userDao.getByPrincipalName(modified.getPrincipalName()) != null) {
                        error(String.format("A user with principal name '%s' already exist", modified.getPrincipalName()));
                        return;
                    } else {
                        logger.warn("Changing user name via admin interface! User {} '{}' becomes '{}'", target.getId(), target.getPrincipalName(), modified.getPrincipalName());
                        target.setPrincipalName(modified.getPrincipalName());
                    }
                }

                //apply update
                userDao.saveAndFlush(target);

                info("User info updated");
                setModelObject(null);
            }
        }
    }

    private class CreateUserForm extends Form<RegistryUser> {

        public CreateUserForm(String id, IModel<RegistryUser> model) {
            super(id, model);
        }

        @Override
        protected void onSubmit() {
            final RegistryUser user = getModelObject();

            //check if principal name is provided
            if (Strings.isNullOrEmpty(user.getPrincipalName())) {
                error("Please provide a principal name for the new user");
                return;
            }

            //check if new principal name not already taken
            if (userDao.getByPrincipalName(user.getPrincipalName()) != null) {
                error(String.format("A user with principal name '%s' already exist", user.getPrincipalName()));
                return;
            }

            logger.info("Creating new user with principal name '{}' and display name '{}'", user.getPrincipalName(), user.getName());
            final RegistryUser savedUser = userDao.saveAndFlush(user);
            info("User " + savedUser.getPrincipalName() + " created with id " + savedUser.getId());

            //empty new user for form
            setModelObject(new RegistryUser());
        }
    }

}
