package eu.clarin.cmdi.componentregistry.rest.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "profileDescription")
public class ProfileDescription extends BaseDescription implements Serializable {

    private static final long serialVersionUID = 1L;

//    public static ProfileDescription createNewDescription() {
//	String id = PROFILE_PREFIX + IdSequence.get();
//	ProfileDescription desc = new ProfileDescription();
//	desc.setId(id);
//	desc.setRegistrationDate(new Date());
//	return desc;
//    }
    @XmlElement(name = "showInEditor")
    @Override
    public void setShowInEditor(boolean showInEditor) {
        super.setShowInEditor(showInEditor);
    }

    @Override
    public boolean isShowInEditor() {
        return super.isShowInEditor();
    }

}
