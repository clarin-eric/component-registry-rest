/**
 * 
 */
package clarin.cmdi.componentregistry.model;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

    public ProfileDescription createProfileDescription() {
        return new ProfileDescription();
    }

    public ComponentDescription createComponentDescription() {
        return new ComponentDescription();
    }

    public RegisterResponse createRegisterErrorResponse() {
        return new RegisterResponse();
    }
}