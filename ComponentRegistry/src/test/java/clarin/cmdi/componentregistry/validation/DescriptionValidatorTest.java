package clarin.cmdi.componentregistry.validation;

import clarin.cmdi.componentregistry.validation.DescriptionValidator;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import clarin.cmdi.componentregistry.BaseDescription;
import clarin.cmdi.componentregistry.ComponentDescription;

public class DescriptionValidatorTest {

    @Test
    public void testValidate() {
        BaseDescription desc = ComponentDescription.createNewDescription();
        DescriptionValidator validator = new DescriptionValidator(desc);
        assertFalse(validator.validate());

        desc.setName("n");
        validator = new DescriptionValidator(desc);
        assertFalse(validator.validate());

        desc.setCreatorName("n");
        validator = new DescriptionValidator(desc);
        assertFalse(validator.validate());

        desc.setDescription("a");
        validator = new DescriptionValidator(desc);
        assertTrue(validator.validate());

        desc.setDomainName("a"); //Not forced atm.
        validator = new DescriptionValidator(desc);
        assertTrue(validator.validate());
    }

}
