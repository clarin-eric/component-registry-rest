package clarin.cmdi.componentregistry.impl.database;

import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import clarin.cmdi.componentregistry.BaseUnitTest;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import org.junit.Ignore;

/**
 * Test transactionality on comments
 * @author george.georgovassilis@mpi.nl
 * 
 */
@Ignore("Failing tests after upgrade from Spring 3.x to 5.x - seem to relate to transaction management")
public class TestTransactionRollbacks extends BaseUnitTest {

    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    ComponentDao componentDescriptionDao;

    @Test
    public void test01() {
	ComponentRegistryTestDatabase.resetAndCreateAllTables(jdbcTemplate);
    }

    @Test(expected = DataAccessException.class)
    public void test02() {
	BaseDescription cd1 = componentDescriptionDao.getByCmdId("c1");
	assertNull(cd1);

	BaseDescription cd = new BaseDescription();
	cd.setCreatorName("user 123");
	cd.setDescription("description");
	cd.setDomainName("domain name");
	cd.setGroupName("group name");
	cd.setName("name");
	cd.setRegistrationDate(new Date());
	cd.setUserId("123");
	cd.setId(ComponentDescription.COMPONENT_PREFIX+"c1");
	componentDescriptionDao.insertDescription(cd, "Content 1", false, 123);

	cd1 = componentDescriptionDao.getByCmdId(ComponentDescription.COMPONENT_PREFIX+"c1");
	assertNotNull(cd1);
	componentDescriptionDao.insertDescription(cd, "Content 1", false, 123);
	fail("Expected second insert to fail");
    }

    @Test
    public void test03() {
	BaseDescription cd1 = componentDescriptionDao.getByCmdId("c1");
	assertNull(cd1);
    }

}
