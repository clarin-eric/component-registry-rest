package clarin.cmdi.componentregistry.impl.database;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import clarin.cmdi.componentregistry.BaseUnitTest;
import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.DeleteFailedException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.RegistrySpace;
import clarin.cmdi.componentregistry.frontend.CMDItemInfo;
import clarin.cmdi.componentregistry.frontend.DisplayDataNode;
import clarin.cmdi.componentregistry.frontend.SubmitFailedException;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import clarin.cmdi.componentregistry.persistence.jpa.UserDao;
import clarin.cmdi.componentregistry.rest.DummyPrincipal;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;

/**
 * 
 * @author george.georgovassilis@mpi.nl
 *
 */
public class AdminRegistryTest extends BaseUnitTest{

    @Autowired
    private ComponentDao componentDao;
    
    @Autowired
    private UserDao userDao;
    
    @Autowired
    private ComponentRegistryFactory componentRegistryFactory;
    private static final Principal PRINCIPAL_ADMIN = DummyPrincipal.DUMMY_ADMIN_PRINCIPAL;
    @Autowired
    private MDMarshaller marshaller;

    @Before
    public void init() {
	ComponentRegistryTestDatabase.resetAndCreateAllTables(jdbcTemplate);
    }

    // TODO: two questions
    @Test
    public void testForceUpdate() throws Exception {
        
        RegistryUser adminUser = new RegistryUser();
	adminUser.setName(PRINCIPAL_ADMIN.getName());
	adminUser.setPrincipalName(PRINCIPAL_ADMIN.getName());
        userDao.save(adminUser);

	ComponentRegistry testRegistry = componentRegistryFactory.getPublicRegistry();
	String content1 = "";
	content1 += "<ComponentSpec isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	content1 += "    xsi:noNamespaceSchemaLocation=\"http://lux16.mpi.nl/cmd-component.xsd\">\n";
	content1 += "    <Header/>\n";
	content1 += "    <Component name=\"XXX\" CardinalityMin=\"1\" CardinalityMax=\"10\">\n";
	content1 += "        <Element name=\"Availability\" ValueScheme=\"string\" />\n";
	content1 += "    </Component>\n";
	content1 += "</ComponentSpec>\n";
	ComponentDescription compDesc1 = RegistryTestHelper.addComponent(testRegistry, "XXX1", content1, true);
       
        assertEquals(1, testRegistry.getComponentDescriptions().size());
        
	String content2 = "";
	content2 += "<ComponentSpec isProfile=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
	content2 += "    xsi:noNamespaceSchemaLocation=\"http://lux16.mpi.nl/cmd-component.xsd\">\n";
	content2 += "    <Header/>\n";
	content2 += "    <Component name=\"YYY\" CardinalityMin=\"1\" CardinalityMax=\"unbounded\">\n";
	content2 += "        <Component ComponentId=\"" + compDesc1.getId() + "\" CardinalityMin=\"0\" CardinalityMax=\"99\">\n";
	content2 += "        </Component>\n";
	content2 += "    </Component>\n";
	content2 += "</ComponentSpec>\n";
	ProfileDescription profileDesc = RegistryTestHelper.addProfile(testRegistry, "YYY1", content2, true);
       

        // why two registries? 
        // if you are logged in as an admin then at any registry you have the same all-mighty rights?
        // testRegistry with the owner admin should be ok?
        // how are these two registries connected, via componentDao?
        
	AdminRegistry adminReg = new AdminRegistry();
	adminReg.setComponentRegistryFactory(componentRegistryFactory);
	adminReg.setComponentDao(componentDao);
	adminReg.setMarshaller(marshaller);
	CMDItemInfo fileInfo = new CMDItemInfo(marshaller);
	fileInfo.setForceUpdate(false);
	fileInfo.setDataNode(new DisplayDataNode(compDesc1.getName(), false, compDesc1, RegistrySpace.PUBLISHED));
	fileInfo.setContent(content1);
	// TODO: how it should be?
        try {
	    adminReg.submitFile(fileInfo, PRINCIPAL_ADMIN);
            
            // Twan ?? when submit should fail?
            //fail();
	} catch (SubmitFailedException e) {
	}
	fileInfo.setForceUpdate(true);
	adminReg.submitFile(fileInfo, PRINCIPAL_ADMIN); //Component needs to be forced because they can be used by other profiles/components

	assertEquals(1, testRegistry.getComponentDescriptions().size());
	
        try {
	    fileInfo.setForceUpdate(false);
	    adminReg.delete(fileInfo, PRINCIPAL_ADMIN);
	} catch (SubmitFailedException e) {
	    assertTrue(e.getCause() instanceof DeleteFailedException);
	}
	
        assertEquals(1, testRegistry.getComponentDescriptions().size());
	fileInfo.setForceUpdate(true);
	adminReg.delete(fileInfo, PRINCIPAL_ADMIN);
	assertEquals(0, testRegistry.getComponentDescriptions().size());

	assertEquals(1, testRegistry.getProfileDescriptions().size());
	fileInfo.setForceUpdate(false);
	fileInfo.setDataNode(new DisplayDataNode(profileDesc.getName(), false, profileDesc, RegistrySpace.PUBLISHED));
	adminReg.delete(fileInfo, PRINCIPAL_ADMIN); //Profile do not need to be forced they cannot be used by other profiles
	assertEquals(0, testRegistry.getProfileDescriptions().size());
    }
}
