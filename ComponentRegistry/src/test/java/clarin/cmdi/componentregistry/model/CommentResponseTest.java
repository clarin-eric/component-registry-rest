package clarin.cmdi.componentregistry.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import clarin.cmdi.componentregistry.BaseUnitTest;
import clarin.cmdi.componentregistry.util.DatesHelper;
import clarin.cmdi.componentregistry.MDMarshaller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;

import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
/**
 *
 * @author jean-charles Ferrières <jean-charles.ferrieres@mpi.nl>
 * @author george.georgovassilis@mpi.nl
 */
public class CommentResponseTest extends BaseUnitTest{

    private Date testDate = new Date();

    /**
     * Test with no validate attribute should return errors
     *
     * @throws Exception
     */
    @Test
    public void testRegisterError() throws Exception {
	CommentResponse resp = new CommentResponse();
	resp.setRegistered(false);
	resp.setIsPrivate(true);
	resp.addError("Error 1");
	resp.addError("Error 2, <!-- to be escaped -->");
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	marshaller.marshal(resp, out);
	String expected = "";
	expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
	expected += "<commentResponse registered=\"false\" isPrivate=\"true\" xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
	expected += "    <errors>\n";
	expected += "        <error>Error 1</error>\n";
	expected += "        <error>Error 2, &lt;!-- to be escaped --&gt;</error>\n";
	expected += "    </errors>\n";
	expected += "</commentResponse>\n";
		assertXMLEqual(expected, out.toString());

	CommentResponse rr = marshaller.unmarshal(CommentResponse.class, new ByteArrayInputStream(expected.getBytes()), null);
	assertFalse(rr.isRegistered());
	assertEquals(2, rr.getErrors().size());
    }

    /**
     * Test successfully processed
     *
     * @throws Exception
     */
    @Test
    public void testRegisterSucces() throws Exception {
	CommentResponse resp = new CommentResponse();
	resp.setRegistered(true);
	resp.setIsPrivate(false);
	resp.setComment(getComment());
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	marshaller.marshal(resp, out);
	String expected = "";
	expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
	expected += "<commentResponse registered=\"true\" isPrivate=\"false\" xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
	expected += "    <errors/>\n";
	expected += "    <comment>\n";
	expected += "        <comments>Name</comments>\n";
	expected += "        <commentDate>"+DatesHelper.formatXmlDateTime(testDate)+"</commentDate>\n";
	expected += "        <componentId>myD</componentId>\n";
	expected += "        <id>myId</id>\n";
	expected += "        <userName>J. Unit</userName>\n";
	expected += "        <canDelete>false</canDelete>\n";
	expected += "    </comment>\n";
	expected += "</commentResponse>\n";
	assertXMLEqual(expected, out.toString());

	CommentResponse rr = marshaller.unmarshal(CommentResponse.class, new ByteArrayInputStream(expected.getBytes()), null);
	assertTrue(rr.isRegistered());
	assertEquals("myId", rr.getComment().getId());
    }

    private Comment getComment() {
	Comment com = new Comment();
	com.setComment("Name");
	com.setId("myId");
	com.setUserId(123);
	com.setComponentRef("myD");
	com.setCommentDate(testDate);
	com.setUserName("J. Unit");
	return com;
    }
}
