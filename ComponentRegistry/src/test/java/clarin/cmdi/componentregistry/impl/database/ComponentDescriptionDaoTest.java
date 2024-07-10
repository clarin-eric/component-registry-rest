package clarin.cmdi.componentregistry.impl.database;

import static clarin.cmdi.componentregistry.impl.database.ComponentRegistryTestDatabase.*;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author George.Georgovassilis@mpi.nl
 */
public class ComponentDescriptionDaoTest extends AbstractDescriptionDaoTest {

    @Autowired
    ComponentDao componentDao;

    @Override
    protected ComponentDao getDao() {
	return componentDao;
    }

    @Override
    protected BaseDescription createNewDescription() {
	return ComponentDescription.createNewDescription();
    }

    @Override
    protected String getContentString() {
	return RegistryTestHelper.getComponentTestContentString();
    }
}
