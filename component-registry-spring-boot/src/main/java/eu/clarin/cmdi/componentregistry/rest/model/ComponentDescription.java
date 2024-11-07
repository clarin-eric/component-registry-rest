package eu.clarin.cmdi.componentregistry.rest.model;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Comparator;

@XmlRootElement(name = "componentDescription")
public class ComponentDescription extends BaseDescription implements Serializable {
    
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

//    public static ComponentDescription createNewDescription() {
//	String id = COMPONENT_PREFIX + IdSequence.get();
//	ComponentDescription desc = new ComponentDescription();
//	desc.setId(id);
//	desc.setRegistrationDate(new Date());
//	return desc;
//    }
}
