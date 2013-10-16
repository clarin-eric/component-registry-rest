package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.model.Component;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author George.Georgovassilis@mpi.nl
 */
public class ProfileDescriptionDaoTest extends AbstractDescriptionDaoTest {

    @Autowired
    ComponentDao profileDescriptionDao;

    @Override
    protected ComponentDao getDao() {
	return profileDescriptionDao;
    }

    @Override
    protected Component createNewDescription() {
	return ProfileDescription.createNewDescription();
    }

    @Override
    protected String getContentString() {
	return RegistryTestHelper.getProfileTestContentString();
    }

}
