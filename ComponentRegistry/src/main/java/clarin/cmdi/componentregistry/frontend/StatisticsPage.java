package clarin.cmdi.componentregistry.frontend;

import java.io.IOException;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.spring.injection.annot.SpringBean;

import clarin.cmdi.componentregistry.model.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.RegistrySpace;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.ComponentSpec;
import clarin.cmdi.componentregistry.components.ComponentType;
import clarin.cmdi.componentregistry.components.ElementType;
import clarin.cmdi.componentregistry.impl.database.CMDComponentSpecExpanderDbImpl;
import clarin.cmdi.componentregistry.impl.database.ComponentRegistryDbImpl;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import java.util.Collection;
import java.util.Collections;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 *
 * @author paucas
 */
public class StatisticsPage extends SecureAdminWebPage {

    private static final long serialVersionUID = 1L;
    @SpringBean(name = "componentRegistryFactory")
    private ComponentRegistryFactory componentRegistryFactory;

    private static class Statistics {
        private int componentnumber = 0;
        private int elementcounter = 0;
        private int conceptlinkcounter = 0;
    }

    public StatisticsPage(final PageParameters pageParameters) throws IOException, ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException {
        super(pageParameters);
        ComponentRegistry registry = componentRegistryFactory.getComponentRegistry(RegistrySpace.PUBLISHED, null, null, null);
        addLinks();
        displayStatistics(registry);
    }

    private void displayStatistics(ComponentRegistry registry) throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException {
        List<ProfileDescription> profileList = registry.getProfileDescriptions(null);
        RepeatingView repeating = new RepeatingView("repeating");
        add(repeating);
        add(new Label("profilenumbermessage", "Current number of profiles in the component registry: " + profileList.size()));
        for (ProfileDescription pd : profileList) {
            displayProfileStatistics(pd, repeating, registry);
        }
        List<ComponentDescription> componentList = registry.getComponentDescriptions(null);
        RepeatingView repeatingcomp = new RepeatingView("repeatingcomp");
        add(repeatingcomp);
        add(new Label("componentnumbermessage", "Current number of components in the component registry: " + componentList.size()));
        for (ComponentDescription cd : componentList) {
            displayComponentStatistics(cd, repeatingcomp, registry);
        }
    }

    private void displayProfileStatistics(ProfileDescription pd, RepeatingView repeatingview, ComponentRegistry registry) throws ComponentRegistryException {
        WebMarkupContainer item = new WebMarkupContainer(repeatingview.newChildId());
        repeatingview.add(item);
        item.add(new Label("ID", pd.getId()));
        item.add(new Label("profname", pd.getName()));
        ComponentSpec profile = CMDComponentSpecExpanderDbImpl.expandProfile(pd.getId(), (ComponentRegistryDbImpl) registry);
        Statistics stats = new Statistics();
        componentCounter(profile, stats);
        item.add(new Label("nrcomp", "" + stats.componentnumber));
        item.add(new Label("nrprofelem", "" + stats.elementcounter));
        item.add(new Label("nrproflinks", "" + stats.conceptlinkcounter));
    }

    private void displayComponentStatistics(ComponentDescription cd, RepeatingView repeatingview,  ComponentRegistry registry) throws ComponentRegistryException {
        WebMarkupContainer item = new WebMarkupContainer(repeatingview.newChildId());
        repeatingview.add(item);
        item.add(new Label("ID", cd.getId()));
        item.add(new Label("compname", cd.getName()));
        ComponentSpec compspec = CMDComponentSpecExpanderDbImpl.expandComponent(cd.getId(), (ComponentRegistryDbImpl) registry);
        Statistics stats = new Statistics();
        componentCounter(compspec, stats);
        item.add(new Label("nrcomp", "" + stats.componentnumber));
        item.add(new Label("nrelem", "" + stats.elementcounter));
        item.add(new Label("nrcomplinks", "" + stats.conceptlinkcounter));
    }

    private void componentCounter(ComponentSpec components, Statistics stats) {
        componentCounter(Collections.singleton(components.getComponent()), stats);
    }
    
    private void componentCounter(Collection<ComponentType> components, Statistics stats) {
        if (components != null) {
            for (ComponentType component : components) {
                stats.componentnumber++;
                List<ElementType> elementlist = component.getElement();
                if (elementlist != null) {
                    stats.elementcounter += elementlist.size();
                    for (ElementType elem : elementlist) {
                        if (elem.getConceptLink() != null) {
                            stats.conceptlinkcounter++;
                        }
                    }
                }
                componentCounter(component.getComponent(), stats);
            }
        }
    }

}
