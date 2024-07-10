package clarin.cmdi.componentregistry;

import java.io.ByteArrayInputStream;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class MDMarshallerTest extends BaseUnitTest{

    private static Logger logger = LoggerFactory.getLogger(MDMarshallerTest.class);
    
    @Test
    public void testMarshalToString() throws Exception {
	BaseDescription desc = ComponentDescription.createNewDescription();
	desc.setName("Test \u00CA test");
	String test = marshaller.marshalToString(desc);
	logger.info(test);
	Assert.assertTrue(test.contains("Test \u00CA test"));
	BaseDescription result = marshaller.unmarshal(ComponentDescription.class, new ByteArrayInputStream(test.getBytes("UTF-8")), null);
	logger.info(result.getName());
	Assert.assertEquals(desc.getName(), result.getName());
    }
}
