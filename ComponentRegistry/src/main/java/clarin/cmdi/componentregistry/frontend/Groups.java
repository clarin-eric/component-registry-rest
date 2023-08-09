package clarin.cmdi.componentregistry.frontend;

import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.impl.ComponentUtils;
import clarin.cmdi.componentregistry.impl.database.ValidationException;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import java.util.Collections;
import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import clarin.cmdi.componentregistry.GroupService;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class Groups extends SecureAdminWebPage {
    
    private final static Logger logger = LoggerFactory.getLogger(Groups.class);
    
    @SpringBean
    private GroupService groupService;
    @SpringBean
    private UserDao userDao;
    private final IModel<Long> selectedGroup = new Model<Long>(null);
    private final IModel<List<RegistryUser>> usersModel;
    private IModel<List<String>> groupsModel;
    
    public Groups(PageParameters parameters) {
        super(parameters);
// model for group options

        addLinks();
        
        usersModel = createUsersModel();
        groupsModel = createGroupsModel();
        
        add(createGroupForm("groupForm"));
        add(createGroupList("groups"));
        add(createGroupInfo("group"));
    }
    
    private IModel<List<String>> createGroupsModel() {
        return new AbstractReadOnlyModel<List<String>>() {
            
            @Override
            public List<String> getObject() {
                return groupService.listGroupNames();
            }
        };
    }
    
    private IModel<List<RegistryUser>> createUsersModel() {
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
    
    private Component createGroupForm(String id) {
        final IModel<String> nameModel = new Model<String>("");
        final IModel<RegistryUser> ownerModel = new Model<RegistryUser>(null);
        
        final Form form = new Form(id) {
            
            @Override
            protected void onSubmit() {
                final String ownerPrincipal = ownerModel.getObject().getPrincipalName();
                final String group = nameModel.getObject();
                try {
                    final long groupId = groupService.createNewGroup(group, ownerPrincipal);
                    info("Group with id " + groupId + " has been created");
                    try {
                        groupService.makeMember(ownerPrincipal, group);
                        info("User " + ownerPrincipal + " added to group " + group);
                    } catch (ItemNotFoundException ex) {
                        error("Could not add owner to group. Reason: " + ex.getMessage());
                    }
                } catch (ValidationException ex) {
                    error(ex.getMessage());
                }
            }
            
        };
        form.add(new FeedbackPanel("feedback"));
        form.add(new TextField("name", nameModel).setRequired(true));
        // owner user selector
        final DropDownChoice<RegistryUser> usersChoice = new DropDownChoice<RegistryUser>("ownerPrincipal", ownerModel, usersModel);
        usersChoice.setRequired(true);
        form.add(usersChoice);
        
        return form;
    }
    
    private Component createGroupList(String id) {
        final IModel<String> groupModel = new Model<String>(null);
        final Form form = new Form(id) {
            
            @Override
            protected void onSubmit() {
                final String groupName = groupModel.getObject();
                if (groupName != null) {
                    try {
                        final Long groupId = (Long) groupService.getGroupIdByName(groupName);
                        selectedGroup.setObject(groupId);
                    } catch (ItemNotFoundException ex) {
                        logger.warn("Could", ex);
                        selectedGroup.setObject(null);
                    }
                }
            }
            
        };

        // group selector
        final DropDownChoice<String> groupChoice = new DropDownChoice<String>("group", groupModel, groupsModel);
        groupChoice.setRequired(true);
        form.add(groupChoice);
        
        return form;
    }
    
    private Component createGroupInfo(String id) {
        final WebMarkupContainer container = new WebMarkupContainer(id) {
            
            @Override
            public boolean isVisible() {
                return selectedGroup.getObject() != null;
            }
            
        };
        
        container.add(new Label("name", new AbstractReadOnlyModel<String>() {
            
            @Override
            public String getObject() {
                try {
                    return groupService.getGroupNameById(selectedGroup.getObject());
                } catch (ItemNotFoundException ex) {
                    return "NOT FOUND";
                }
            }
        }));
        container.add(createNewMemberForm("newMember"));
        container.add(createGroupMembersView("members"));
        container.add(createItemsView("items"));
        
        return container;
    }
    
    private Form createNewMemberForm(String id) {
        final IModel<RegistryUser> principalModel = new Model<RegistryUser>(null);
        final Form memberForm = new Form(id) {
            
            @Override
            protected void onSubmit() {
                try {
                    final String groupName = groupService.getGroupNameById(selectedGroup.getObject());
                    groupService.makeMember(principalModel.getObject().getPrincipalName(), groupName);
                    info("User " + principalModel.getObject() + " added to group");
                } catch (ItemNotFoundException ex) {
                    error(ex);
                }
            }
            
        };
        memberForm.add(new FeedbackPanel("feedback"));
        
        final DropDownChoice<RegistryUser> usersChoice = new DropDownChoice<RegistryUser>("principal", principalModel, usersModel);
        usersChoice.setRequired(true);
        memberForm.add(usersChoice);
        return memberForm;
    }
    
    private ListView createGroupMembersView(String id) {
        final IModel<List> membersModel = new AbstractReadOnlyModel<List>() {
            
            @Override
            public List getObject() {
                final Long groupId = selectedGroup.getObject();
                if (groupId == null) {
                    return Collections.emptyList();
                } else {
                    return groupService.getUsersInGroup(groupId);
                }
            }
        };
        final ListView membersView = new ListView(id, membersModel) {
            
            @Override
            protected void populateItem(ListItem li) {
                final RegistryUser user = (RegistryUser) li.getModelObject();
                li.add(new Label("name", String.format("%s [%s]", user.getName(), user.getPrincipalName())));
                li.add(new Link("remove") {
                    
                    @Override
                    public void onClick() {
                        try {
                            final String groupName = groupService.getGroupNameById(selectedGroup.getObject());
                            final String userName = user.getPrincipalName();
                            
                            logger.info("Revoking team membership: '{}' in '{}'", userName, groupName);
                            groupService.removeMember(userName, groupName);
                            info(String.format("User '%s' removed from team '%s'", userName, groupName));
                        } catch (ItemNotFoundException ex) {
                            error(ex);
                        }
                    }
                });
            }
        };
        return membersView;
    }
    
    private ListView createItemsView(String id) {
        final IModel<List> itemsModel = new AbstractReadOnlyModel<List>() {
            
            @Override
            public List getObject() {
                final Long groupId = selectedGroup.getObject();
                return Lists.newArrayList(Iterables.concat(
                        groupService.getComponentIdsInGroup(groupId),
                        groupService.getProfileIdsInGroup(groupId)));
            }
        };
        final ListView itemsView = new ListView(id, itemsModel) {
            
            @Override
            protected void populateItem(ListItem li) {
                final String id = (String) li.getModelObject();
                final ExternalLink link = new ExternalLink("link", new AbstractReadOnlyModel<String>() {
                    
                    @Override
                    public String getObject() {
                        if (ComponentUtils.isProfileId(id)) {
                            return "../rest/registry/profiles/" + id;
                        } else {
                            return "../rest/registry/components/" + id;
                        }
                    }
                });
                link.add(new Label("id", li.getModel()));
                li.add(link);
            }
        };
        return itemsView;
    }
    
}
