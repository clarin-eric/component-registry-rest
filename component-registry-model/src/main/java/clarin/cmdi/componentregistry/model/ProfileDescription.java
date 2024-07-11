package clarin.cmdi.componentregistry.model;

import static clarin.cmdi.componentregistry.ComponentRegistryConstants.REGISTRY_ID;
import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import clarin.cmdi.componentregistry.util.IdSequence;

@XmlRootElement(name = "profileDescription")
public class ProfileDescription extends BaseDescription implements Serializable {

    // Attention! PROFILE_PREFIX here and the client's Config.PROFILE_PREFIX must be the same 
    // If you change PROFILE_PREFIX here, then the client's  Config.PROFILE_PREFIX
    public static final String PROFILE_PREFIX = REGISTRY_ID + "p_";
    
    private static final long serialVersionUID = 1L;

    public static ProfileDescription createNewDescription() {
	String id = PROFILE_PREFIX + IdSequence.get();
	ProfileDescription desc = new ProfileDescription();
	desc.setId(id);
	desc.setRegistrationDate(new Date());
	return desc;
    }
    
    @XmlElement(name="showInEditor")
    @Override
    public void setShowInEditor(boolean showInEditor) {
        super.setShowInEditor(showInEditor);
    }
    
    @Override
    public boolean isShowInEditor() {
        return super.isShowInEditor();
    }

}
