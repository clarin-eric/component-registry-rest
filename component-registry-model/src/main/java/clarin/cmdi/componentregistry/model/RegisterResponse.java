package clarin.cmdi.componentregistry.model;

import clarin.cmdi.componentregistry.BaseDescription;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "registerResponse")
@XmlType(propOrder = {"isProfile","description"})
public class RegisterResponse extends ComponentRegistryResponse {

    @XmlAttribute(required = true)
    private Boolean isProfile;

    @XmlElement
    private BaseDescription description;

    public void setDescription(BaseDescription description) {
        this.description = description;
    }

    public BaseDescription getDescription() {
        return description;
    }

    public boolean isProfile() {
        return isProfile;
    }

    public void setIsProfile(boolean isProfile) {
        this.isProfile = isProfile;
    }
    
}
