package clarin.cmdi.componentregistry.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import clarin.cmdi.componentregistry.BaseUnitTest;
import clarin.cmdi.componentregistry.util.DatesHelper;
import clarin.cmdi.componentregistry.MDMarshaller;

import javax.xml.transform.TransformerException;

import org.junit.Before;

public class ComponentDescriptionTest extends BaseUnitTest{

    private Date testDate = new Date();

    @Test
    public void testComponentToXml() throws JAXBException, UnsupportedEncodingException {
        ComponentDescription desc = new ComponentDescription();
        desc.setName("Name");
        desc.setId(ComponentDescription.COMPONENT_PREFIX+"myId");
        desc.setCreatorName("myC");
        desc.setUserId("user1");
        desc.setDescription("myD");
        desc.setRegistrationDate(testDate);
        desc.setHref("linkToMyComponent");
        desc.setGroupName("MyGroup");
        desc.setDomainName("Linguistics");
        desc.setPublic(false);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        marshaller.marshal(desc, out);
        String expected = "";
        expected += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        expected += "<componentDescription xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
        expected += "    <id>clarin.eu:cr1:c_myId</id>\n";
        expected += "    <name>Name</name>\n";
        expected += "    <description>myD</description>\n";
        expected += "    <registrationDate>"+DatesHelper.formatXmlDateTime(testDate)+"</registrationDate>\n";
        expected += "    <creatorName>myC</creatorName>\n";
        expected += "    <userId>user1</userId>\n";
        expected += "    <domainName>Linguistics</domainName>\n";
        expected += "    <ns2:href>linkToMyComponent</ns2:href>\n";
        expected += "    <groupName>MyGroup</groupName>\n";
        expected += "    <commentsCount>0</commentsCount>\n";
        expected += "    <isPublic>false</isPublic>\n";
        expected += "    <recommended>false</recommended>\n";
        expected += "</componentDescription>\n";
        assertEquals(expected, out.toString());

        ComponentDescription pd = marshaller.unmarshal(ComponentDescription.class, new ByteArrayInputStream(expected.getBytes()), null);
        assertEquals(desc.getId(), pd.getId());
    }

}
