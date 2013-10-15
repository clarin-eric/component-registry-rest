package clarin.cmdi.componentregistry.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "registerResponse")
public class RegisterResponse extends ComponentRegistryResponse {

    @XmlAttribute(required = true)
    private Boolean isProfile;

    @XmlElement
    private BaseComponent description;

    public void setDescription(BaseComponent description) {
        this.description = description;
    }

    public BaseComponent getDescription() {
        return description;
    }

    public boolean isProfile() {
        return isProfile;
    }

    public void setIsProfile(boolean isProfile) {
        this.isProfile = isProfile;
    }
    
}
