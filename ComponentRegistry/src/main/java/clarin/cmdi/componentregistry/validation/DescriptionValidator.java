package clarin.cmdi.componentregistry.validation;

import org.apache.commons.lang3.StringUtils;
import clarin.cmdi.componentregistry.BaseDescription;

public class DescriptionValidator extends BaseValidator {

    private final BaseDescription desc;

    public DescriptionValidator(BaseDescription desc) {
        this.desc = desc;
    }

    public boolean validate() {
        if (!isOk(desc.getCreatorName(), desc.getDescription(), desc.getName())) {
            addErrorMessage("Fields are not filled in correctly");
        }
        return !hasErrors();
    }

    @Override
    public boolean runIfInvalid() {
        return true;
    }

    private boolean isOk(String... fields) {
        boolean isOk = true;
        for (String field : fields) {
            isOk &= StringUtils.isNotBlank(field);
        }
        return isOk;
    }

}
