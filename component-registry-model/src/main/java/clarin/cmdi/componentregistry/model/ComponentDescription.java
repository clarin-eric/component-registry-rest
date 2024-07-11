package clarin.cmdi.componentregistry.model;

import static clarin.cmdi.componentregistry.ComponentRegistryConstants.REGISTRY_ID;
import clarin.cmdi.componentregistry.util.IdSequence;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "componentDescription")
public class ComponentDescription extends BaseDescription implements Serializable {

    
    
    // Attention! COMPONENT_PREFIX here and the client's Config.COMPONENT_PREFIX must be the same 
    // If you change COMPONENT_PREFIX here, then change the client's  config.COMPONENT_PREFIX
    public static final String COMPONENT_PREFIX = REGISTRY_ID + "c_";
    
    private static final long serialVersionUID = 1L;
    
    public static final Comparator<? super ComponentDescription> COMPARE_ON_GROUP_AND_NAME = new Comparator<ComponentDescription>() {
	@Override
	public int compare(ComponentDescription o1, ComponentDescription o2) {
	    int result = 0;
	    if (o1.getGroupName() != null && o2.getGroupName() != null) {
		result = o1.getGroupName().compareToIgnoreCase(o2.getGroupName());
	    }
	    if (result == 0) {
		if (o1.getName() != null && o2.getName() != null) {
		    result = o1.getName().compareToIgnoreCase(o2.getName());
		} else {
		    result = o1.getId().compareTo(o2.getId());
		}
	    }
	    return result;
	}
    };

    public static ComponentDescription createNewDescription() {
	String id = COMPONENT_PREFIX + IdSequence.get();
	ComponentDescription desc = new ComponentDescription();
	desc.setId(id);
	desc.setRegistrationDate(new Date());
	return desc;
    }
}
