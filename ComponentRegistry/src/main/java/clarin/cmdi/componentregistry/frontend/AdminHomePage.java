package clarin.cmdi.componentregistry.frontend;

import java.security.Principal;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.model.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.IMarshaller;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.OwnerUser;
import clarin.cmdi.componentregistry.RegistrySpace;
import clarin.cmdi.componentregistry.ShhaaUserCredentials;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.ComponentUtils;
import clarin.cmdi.componentregistry.impl.database.AdminRegistry;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.DefaultNestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.extensions.markup.html.repeater.util.TreeModelProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.dao.DataAccessException;
import clarin.cmdi.componentregistry.GroupService;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;
import java.util.Objects;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.link.ExternalLink;

@SuppressWarnings("serial")
public class AdminHomePage extends SecureAdminWebPage {

    private static final long serialVersionUID = 1L;
    private final static Logger LOG = LoggerFactory.getLogger(AdminHomePage.class);
    private final CMDItemInfo info;
    private NestedTree<AdminTreeNode> tree;
    private transient AdminRegistry adminRegistry = new AdminRegistry();
    @SpringBean(name = "componentRegistryFactory")
    private ComponentRegistryFactory componentRegistryFactory;
    @SpringBean(name = "GroupService")
    private GroupService groupService;
    @SpringBean
    private ComponentDao componentDao;
    @SpringBean
    private UserDao userDao;
    @SpringBean
    private IMarshaller marshaller;

    private Component infoView;

    private IModel<Set<AdminTreeNode>> expansionModel = new Model(new HashSet<>());
    private IModel<List<RegistryUser>> usersModel;
    final IModel<RegistryUser> selectedItemOwnerModel = new CompoundPropertyModel<>(null);

    public AdminHomePage(final PageParameters parameters) throws ComponentRegistryException, ItemNotFoundException {
        super(parameters);
        adminRegistry.setComponentRegistryFactory(componentRegistryFactory);
        adminRegistry.setComponentDao(componentDao);
        adminRegistry.setMarshaller(marshaller);
        info = new CMDItemInfo(marshaller);
        this.usersModel = createUsersModel(userDao);
        addLinks();

        final FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);

        final CompoundPropertyModel<CMDItemInfo> infoModel = new CompoundPropertyModel<>(info);

        add(infoView = new WebMarkupContainer("infoView")
                .add(new Label("name"))
                .add(new Label("id"))
                .add(new ExternalLink("itemInfo", createItemLinkModel(infoModel)))
                .add(new Label("status", createStatusModel(infoModel)))
                .add(feedback)
                .add(createPublishDeleteForm())
                .add(createEditForm(feedback))
                .add(createOwnershipForm(feedback))
                .add(new Behavior() {
                    @Override
                    public void onConfigure(Component component) {
                        component.setVisible(info != null && info.getDataNode() != null);
                    }

                })
                .setDefaultModel(infoModel)
                .setOutputMarkupPlaceholderTag(true));

        try {
            tree = createTree("tree", createDBTreeModel());
            add(tree);

        } catch (UserUnauthorizedException e) {
            LOG.error("Admin: ", e);
            error("Cannot create tree: error = " + e);
        }

        add(new IndicatingAjaxLink<Void>("reload") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                try {
                    reloadTreeModel(info);
                    target.add(tree);
                } catch (ItemNotFoundException | UserUnauthorizedException ex) {
                    error(ex.getMessage());
                }
            }
        });
    }

    @Override
    protected void onInitialize() {
        super.onInitialize(); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        expansionModel.detach();
        usersModel.detach();
        selectedItemOwnerModel.detach();
    }

    @Override
    public final MarkupContainer add(Component... children) {
        return super.add(children);
    }

    private Form createPublishDeleteForm() throws ItemNotFoundException, ComponentRegistryException {
        final Form<CMDItemInfo> form = new Form<>("actionsForm");
        CompoundPropertyModel model = new CompoundPropertyModel(info);
        form.setModel(model);

        Button deleteButton = new IndicatingAjaxButton("delete", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                CMDItemInfo info = (CMDItemInfo) form.getModelObject();
                info("deleting:" + info.getName());
                Principal userPrincipal = getUserPrincipal();
                try {
                    adminRegistry.delete(info, userPrincipal);
                    info("Item deleted.");
                    reloadTreeModel(info);
                } catch (ItemNotFoundException | UserUnauthorizedException | SubmitFailedException e) {
                    LOG.error("Admin: ", e);
                    error("Cannot delete: " + info.getName() + "\n error=" + e);
                }
                target.add(infoView);
                target.add(tree);
            }

            @Override
            public boolean isEnabled() {
                return info.isDeletable();

            }
        };
        form.add(deleteButton);
        Button undeleteButton = new IndicatingAjaxButton("undelete", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                CMDItemInfo info = (CMDItemInfo) form.getModelObject();
                info("undeleting:" + info.getName());
                try {
                    adminRegistry.undelete(info);
                    info("Item put back.");
                    reloadTreeModel(info);
                } catch (ItemNotFoundException | UserUnauthorizedException | SubmitFailedException e) {
                    LOG.error("Admin: ", e);
                    error("Cannot undelete: " + info.getName() + "\n error=" + e);
                }
                target.add(infoView);
                target.add(tree);
            }

            @Override
            public boolean isEnabled() {
                return info.isUndeletable();
            }
        };
        form.add(undeleteButton);
        return form;
    }

    private Form createOwnershipForm(FeedbackPanel feedback) {
        final Form form = new Form("transferOwnershipForm");
        // target user selection dropdown
        form.add(new DropDownChoice<>("principal", selectedItemOwnerModel, usersModel)
                .add(new DisableOnDeletedBehavior(info))
        );

        // action button
        form.add(new IndicatingAjaxButton("submit", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                final RegistryUser targetUser = selectedItemOwnerModel.getObject();
                if (targetUser != null) {
                    LOG.info("Transfer of ownership for item {} to user {} requested", info.getId(), targetUser);
                    if (Objects.equals(targetUser.getId(), info.getDataNode().getDescription().getDbUserId())) {
                        warn("Target user is equal to current user. Ownership has NOT been changed.");
                    } else {
                        final Long itemId = info.getDataNode().getDescription().getDbId();
                        componentDao.setOwner(itemId, targetUser.getId());

                        // check results
                        final BaseDescription updatedDescription = componentDao.getById(itemId);
                        if (Objects.equals(targetUser.getId(), updatedDescription.getDbUserId())) {
                            // success
                            info("Owner of item '" + updatedDescription.getName() + "' has been set to [" + targetUser.getPrincipalName() + "]");
                            // update description
                            info.setDescription(updatedDescription);
                        } else {
                            error("User ID check failed: owner id NOT set to requested user id. Current owner of '" + updatedDescription.getName() + "': [" + targetUser.getPrincipalName() + "]");
                        }
                    }
                }

                if (target != null) {
                    target.add(infoView);
                    target.add(feedback);
                }
            }
        }.add(new DisableOnDeletedBehavior(info)));

        return form;

    }

    private Form createEditForm(final FeedbackPanel feedback) throws ItemNotFoundException, ComponentRegistryException {
        final Form<CMDItemInfo> form = new Form<>("form");
        CompoundPropertyModel model = new CompoundPropertyModel(info);
        form.setModel(model);

        form.add(new TextArea("description")
                .add(new DisableOnDeletedBehavior(info))
                .setOutputMarkupId(true)
        );
        form.add(new TextArea("content")
                .add(new DisableOnDeletedBehavior(info))
                .setOutputMarkupId(true)
        );

        CheckBox forceUpdateCheck = new CheckBox("forceUpdate");
        form.add(forceUpdateCheck);

        final Button submitButton = new IndicatingAjaxButton("submit", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                submitEditForm(form, feedback, target, false);
            }
        };
        form.add(submitButton
                .add(new DisableOnDeletedBehavior(info)));

        final Button publishButton = new IndicatingAjaxButton("publish", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                submitEditForm(form, feedback, target, true);
                //reload tree after publish
                try {
                    reloadTreeModel(info);
                    if (target != null) {
                        target.add(tree);
                    }
                } catch (UserUnauthorizedException | ItemNotFoundException ex) {
                    LOG.error("error reloading tree model", ex);
                }
            }

            @Override
            public boolean isEnabled() {
                return super.isEnabled() && !info.isPublished();
            }
        };
        form.add(publishButton
                .add(new DisableOnDeletedBehavior(info)));
        return form;
    }

    private void submitEditForm(Form<?> form, FeedbackPanel feedback, AjaxRequestTarget target, boolean publish) throws DataAccessException {
        final CMDItemInfo itemInfo = (CMDItemInfo) form.getModelObject();
        Principal userPrincipal = getUserPrincipal();
        info("submitting:" + itemInfo.getName() + " id=(" + itemInfo.getId() + ")");
        try {
            adminRegistry.submitFile(itemInfo, userPrincipal, publish);
            info("submitting done.");

            final BaseDescription newDescr = componentDao.getByCmdId(itemInfo.getId());
            itemInfo.setDescription(ComponentUtils.toTypeByIdPrefix(newDescr));
            itemInfo.setName(newDescr.getName());
        } catch (ComponentRegistryException | UserUnauthorizedException | SubmitFailedException | DataAccessException e) {
            LOG.error("Admin: ", e);
            error("Cannot submit: " + itemInfo.getName() + "\n error=" + e);
        }

        if (target != null) {
            target.add(infoView);
            target.add(feedback);
        }
    }

    private void reloadTreeModel(CMDItemInfo info) throws UserUnauthorizedException, ItemNotFoundException {
        try {
            tree = createTree("tree", createDBTreeModel());
            addOrReplace(tree);
        } catch (ComponentRegistryException e) {
            LOG.error("Admin: ", e);
            error("Cannot reload tree: " + info.getName() + "\n error=" + e);
        }
    }

    private NestedTree<AdminTreeNode> createTree(String id, TreeModel treeModel) throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException {
        final TreeModelProvider<AdminTreeNode> treeModelProvider = new TreeModelProvider<AdminTreeNode>(treeModel) {
            @Override
            public IModel<AdminTreeNode> model(AdminTreeNode object) {
                return Model.of(object);
            }

        };
        final NestedTree<AdminTreeNode> adminTree = new DefaultNestedTree<>(id, treeModelProvider, expansionModel) {
            @Override
            protected Component newContentComponent(String id, IModel<AdminTreeNode> nodeModel) {
                if (nodeModel.getObject().isLeaf()) {
                    return new AdminTreeItemLeafNode(id, tree, nodeModel);
                } else {
                    return super.newContentComponent(id, nodeModel);
                }
            }

        };

        adminTree.setOutputMarkupId(true);
        return adminTree;
    }

    private TreeModel createDBTreeModel() throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException {
        AdminTreeNode rootNode = new AdminTreeNode(DisplayDataNode.newNonItemNode("ComponentRegistry", false, null));
        AdminTreeNode publicNode = new AdminTreeNode(DisplayDataNode.newNonItemNode("Public", false, rootNode.getUserObject()));
        rootNode.add(publicNode);
        ComponentRegistry publicRegistry = componentRegistryFactory.getPublicRegistry();
        add(publicNode, publicRegistry);
        List<ComponentRegistry> userRegistries = componentRegistryFactory.getAllUserRegistries();
        for (ComponentRegistry registry : userRegistries) {
            addRegistry(rootNode, registry, registry.getName());
        }

        final ShhaaUserCredentials userCredentials = new ShhaaUserCredentials(getUserPrincipal());
        final RegistryUser user = componentRegistryFactory.getOrCreateUser(userCredentials);

        final List<String> groups = groupService.listGroupNames();
        for (String group : groups) {
            final Number groupId = groupService.getGroupIdByName(group);
            final ComponentRegistry registry = componentRegistryFactory.getComponentRegistry(RegistrySpace.GROUP, new OwnerUser(user.getId()), userCredentials, groupId);
            addRegistry(rootNode, registry, String.format("Registry of group %s", group));
        }
        TreeModel model = new DefaultTreeModel(rootNode);
        return model;
    }

    private void addRegistry(AdminTreeNode rootNode, ComponentRegistry registry, String name) throws UserUnauthorizedException, ItemNotFoundException, ComponentRegistryException {
        AdminTreeNode userNode = new AdminTreeNode(DisplayDataNode.newNonItemNode(name, false, rootNode.getUserObject()));
        rootNode.add(userNode);
        add(userNode, registry);
    }

    private void add(AdminTreeNode parent, ComponentRegistry registry) throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException {
        AdminTreeNode componentsNode = new AdminTreeNode(DisplayDataNode.newNonItemNode("Components", false, parent.getUserObject()));
        parent.add(componentsNode);
        add(componentsNode, registry.getComponentDescriptions(null), false, registry.getRegistrySpace());

        AdminTreeNode profilesNode = new AdminTreeNode(DisplayDataNode.newNonItemNode("Profiles", false, parent.getUserObject()));
        parent.add(profilesNode);
        add(profilesNode, registry.getProfileDescriptions(null), false, registry.getRegistrySpace());

        AdminTreeNode deletedCompNode = new AdminTreeNode(DisplayDataNode.newNonItemNode("Deleted Components", true, parent.getUserObject()));
        parent.add(deletedCompNode);

        List<ComponentDescription> deletedComponentDescriptions = registry.getDeletedComponentDescriptions();
        add(deletedCompNode, deletedComponentDescriptions, true, registry.getRegistrySpace());

        AdminTreeNode deletedProfNode = new AdminTreeNode(DisplayDataNode.newNonItemNode("Deleted Profiles", true, parent.getUserObject()));
        parent.add(deletedProfNode);
        List<ProfileDescription> deletedProfileDescriptions = registry.getDeletedProfileDescriptions();
        add(deletedProfNode, deletedProfileDescriptions, true, registry.getRegistrySpace());
    }

    private void add(AdminTreeNode parent, List<? extends BaseDescription> descs, boolean isDeleted, RegistrySpace space) {
        for (BaseDescription desc : descs) {
            AdminTreeNode child = new AdminTreeNode(new DisplayDataNode(desc.getName(), true, isDeleted, desc, space, parent.getUserObject()));
            parent.add(child);
        }
    }

    @Override
    protected final void addLinks() {
        //no call to super - no home link needed
    }

    private IModel<String> createStatusModel(IModel<CMDItemInfo> infoModel) {
        return () -> {
            final CMDItemInfo inf = infoModel.getObject();
            return String.format("%s (%s in %s)", inf.getStatus(), inf.isPublished() ? "published" : "unpublished", inf.getSpace());
        };
    }

    private IModel<String> createItemLinkModel(CompoundPropertyModel<CMDItemInfo> infoModel) {
        return () -> {
            return "../rest/items/" + infoModel.getObject().getId();
        };
    }

    private static class DisableOnDeletedBehavior extends Behavior {

        private final CMDItemInfo info;

        public DisableOnDeletedBehavior(CMDItemInfo info) {
            this.info = info;
        }

        @Override
        public void onConfigure(Component component) {
            component.setEnabled(info != null
                    && info.isEditable()
                    && info.getDataNode() != null
                    && !info.getDataNode().isDeleted());
        }

    }

    private class AdminTreeItemLeafNode extends Folder<AdminTreeNode> {

        public AdminTreeItemLeafNode(String id, AbstractTree tree, IModel<AdminTreeNode> nodeModel) {
            super(id, tree, nodeModel);
        }

        @Override
        protected boolean isClickable() {
            return true;
        }

        @Override
        protected boolean isSelected() {
            final DisplayDataNode dn = getModelObject().getUserObject();
            if (info.getId() != null && dn.getDescription() != null) {
                return dn.getDescription().getId().equals(info.getId());
            }
            return super.isSelected();
        }

        @Override
        protected void onClick(Optional<AjaxRequestTarget> target) {
            try {
                final DisplayDataNode dn = getModelObject().getUserObject();
                if (dn.getDescription() != null) {
                    //update description
                    dn.setDesc(ComponentUtils.toTypeByIdPrefix(componentDao.getDeletedById(dn.getDescription().getDbId())));
                }
                info.setDataNode(dn);

                final BaseDescription desc = dn.getDescription();
                if (desc != null) {
                    String content = componentDao.getContent(dn.isDeleted(), desc.getId());
                    info.setContent(content);
                }

                //update owner model for selection
                final RegistryUser itemOwner = userDao.getPrincipalNameById(dn.getDescription().getDbUserId());
                selectedItemOwnerModel.setObject(itemOwner);
            } catch (ComponentRegistryException ex) {
                LOG.error("Error getting node data", ex);
                getSession().error("Could not get data for node. See Tomcat log for details.");
            }

            target.ifPresent(t -> {
                t.add(infoView);
                t.add(tree);
            });
        }
    }

}
