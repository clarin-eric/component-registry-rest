package clarin.cmdi.componentregistry.model;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;

import org.junit.Test;

import clarin.cmdi.componentregistry.BaseUnitTest;
import clarin.cmdi.componentregistry.DatesHelper;

public class RegisterResponseTest extends BaseUnitTest {

	private Date testDate = new Date();

	@Test
	public void testRegisterError() throws Exception {
		RegisterResponse resp = new RegisterResponse();
		resp.setRegistered(false);
		resp.setIsProfile(true);
		resp.setIsPrivate(true);
		resp.addError("Error 1");
		resp.addError("Error 2, <!-- to be escaped -->");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		marshaller.marshal(resp, out);
		String expected = "";
		expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
		expected += "<registerResponse isProfile=\"true\" registered=\"false\" isPrivate=\"true\" xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
		expected += "    <errors>\n";
		expected += "        <error>Error 1</error>\n";
		expected += "        <error>Error 2, &lt;!-- to be escaped --&gt;</error>\n";
		expected += "    </errors>\n";
		expected += "</registerResponse>\n";
		assertXMLEqual(expected, out.toString());
		RegisterResponse rr = marshaller.unmarshal(RegisterResponse.class,
				new ByteArrayInputStream(expected.getBytes()), null);
		assertFalse(rr.isRegistered());
		assertTrue(rr.isProfile());
		assertEquals(2, rr.getErrors().size());
	}

	@Test
	public void testRegisterSucces() throws Exception {
		RegisterResponse resp = new RegisterResponse();
		resp.setRegistered(true);
		resp.setIsProfile(true);
		resp.setIsPrivate(false);
		resp.setDescription(getProfileDescription());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		marshaller.marshal(resp, out);
		String expected = "";
		expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
		expected += "<registerResponse isProfile=\"true\" registered=\"true\" isPrivate=\"false\" xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
		expected += "    <errors/>\n";
		expected += "    <description xsi:type=\"profileDescription\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n";
		expected += "        <id>clarin.eu:cr1:p_myId</id>\n";
		expected += "        <name>Name</name>\n";
		expected += "        <description>myD</description>\n";
		expected += "        <registrationDate>" + DatesHelper.formatXmlDateTime(testDate) + "</registrationDate>\n";
		expected += "        <creatorName>myC</creatorName>\n";
		expected += "        <ns2:href>linkToMyProfile</ns2:href>\n";
		expected += "        <commentsCount>2</commentsCount>\n";
		expected += "        <isPublic>false</isPublic>\n";
		expected += "        <recommended>false</recommended>\n";
		expected += "        <showInEditor>true</showInEditor>\n";
		expected += "    </description>\n";
		expected += "</registerResponse>\n";
		assertXMLEqual(expected, out.toString());

		RegisterResponse rr = marshaller.unmarshal(RegisterResponse.class,
				new ByteArrayInputStream(expected.getBytes()), null);
		assertTrue(rr.isRegistered());
		assertTrue(rr.isProfile());
		assertEquals("clarin.eu:cr1:p_myId", rr.getDescription().getId());
	}

	@Test
	public void testRegisterSuccesComponent() throws Exception {
		RegisterResponse resp = new RegisterResponse();
		resp.setRegistered(true);
		resp.setIsProfile(false);
		resp.setIsPrivate(true);
		resp.setDescription(getComponentDescription());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		marshaller.marshal(resp, out);
		String expected = "";
		expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
		expected += "<registerResponse isProfile=\"false\" registered=\"true\" isPrivate=\"true\" xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
		expected += "    <errors/>\n";
		expected += "    <description xsi:type=\"componentDescription\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n";
		expected += "        <id>clarin.eu:cr1:c_myId</id>\n";
		expected += "        <name>Name</name>\n";
		expected += "        <description>myD</description>\n";
		expected += "        <registrationDate>" + DatesHelper.formatXmlDateTime(testDate) + "</registrationDate>\n";
		expected += "        <creatorName>myC</creatorName>\n";
		expected += "        <ns2:href>linkToMyProfile</ns2:href>\n";
		expected += "        <groupName>imdi</groupName>\n";
		expected += "        <commentsCount>2</commentsCount>\n";
		expected += "        <isPublic>false</isPublic>\n";
		expected += "        <recommended>false</recommended>\n";
                expected += "    </description>\n";
		expected += "</registerResponse>\n";
		assertXMLEqual(expected, out.toString());

		RegisterResponse rr = marshaller.unmarshal(RegisterResponse.class,
				new ByteArrayInputStream(expected.getBytes()), null);
		assertTrue(rr.isRegistered());
		assertFalse(rr.isProfile());
		assertEquals("clarin.eu:cr1:c_myId", rr.getDescription().getId());
	}

	private ComponentDescription getComponentDescription() {
		ComponentDescription desc = new ComponentDescription();
		desc.setName("Name");
		desc.setId(ComponentDescription.COMPONENT_PREFIX + "myId");
		desc.setGroupName("imdi");
		desc.setCreatorName("myC");
		desc.setDescription("myD");
		desc.setRegistrationDate(testDate);
		desc.setHref("linkToMyProfile");
		desc.setCommentsCount(2);
		return desc;
	}

	private ProfileDescription getProfileDescription() {
		ProfileDescription desc = new ProfileDescription();
		desc.setName("Name");
		desc.setId(ProfileDescription.PROFILE_PREFIX + "myId");
		desc.setCreatorName("myC");
		desc.setDescription("myD");
		desc.setRegistrationDate(testDate);
		desc.setHref("linkToMyProfile");
		desc.setCommentsCount(2);
		return desc;
	}
}
