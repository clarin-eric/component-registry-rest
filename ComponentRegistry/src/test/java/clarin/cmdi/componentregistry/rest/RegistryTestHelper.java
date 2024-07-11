package clarin.cmdi.componentregistry.rest;

import clarin.cmdi.componentregistry.AuthenticationRequiredException;
import clarin.cmdi.componentregistry.ComponentRegistryException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import clarin.cmdi.componentregistry.model.ComponentRegistry;
import clarin.cmdi.componentregistry.util.DatesHelper;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.ComponentSpec;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ComponentStatus;
import static clarin.cmdi.componentregistry.model.ComponentStatus.DEVELOPMENT;
import static clarin.cmdi.componentregistry.model.ComponentStatus.PRODUCTION;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.persistence.jpa.CommentsDao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Static helper methods to be used in tests
 *
 */
public final class RegistryTestHelper {
    
    private static MDMarshaller marshaller;
    
    @Autowired
    public void setMarshaller(MDMarshaller marshaller) {
        RegistryTestHelper.marshaller = marshaller;
    }
    
    public static ComponentDescription addComponent(ComponentRegistry testRegistry, String id, boolean isPublic) throws ParseException, JAXBException {
        final ComponentStatus status = isPublic ? PRODUCTION : DEVELOPMENT;
        return addComponent(testRegistry, id, isPublic, status);
    }
    
    public static ComponentDescription addComponent(ComponentRegistry testRegistry, String id, boolean isPublic, ComponentStatus status) throws ParseException, JAXBException {
        return addComponent(testRegistry, id, getComponentTestContent(id, status.toString()), isPublic, status);
    }
    
    public static ComponentDescription addComponent(ComponentRegistry testRegistry, String id, String content, boolean isPublic, ComponentStatus status) throws ParseException,
            JAXBException, UnsupportedEncodingException {
        return addComponent(testRegistry, id, new ByteArrayInputStream(content.getBytes("UTF-8")), isPublic, status);
    }
    
    private static ComponentDescription addComponent(ComponentRegistry testRegistry, String id, InputStream content, boolean isPublic, ComponentStatus status) throws ParseException,
            JAXBException {
        ComponentDescription desc = ComponentDescription.createNewDescription();
        desc.setCreatorName(DummyPrincipal.DUMMY_CREDENTIALS.getDisplayName());
        desc.setUserId(DummyPrincipal.DUMMY_PRINCIPAL.getName());
        desc.setName(id);
        desc.setDescription("Test Description");
        desc.setId(ComponentDescription.COMPONENT_PREFIX + id);
        desc.setPublic(isPublic);
        desc.setStatus(status);
        return addComponent(content, testRegistry, desc);
    }
    
    public static ComponentDescription addComponent(InputStream content, ComponentRegistry testRegistry, ComponentDescription desc) throws JAXBException {
        ComponentSpec spec = marshaller.unmarshal(ComponentSpec.class, content, marshaller.getComponentSchema());
        testRegistry.register(desc, spec);
        return desc;
    }
    
    public static ComponentDescription addComponentAnotherPrincipal(ComponentRegistry testRegistry, String id, boolean isPublic) throws ParseException, JAXBException {
        return addComponentAnotherPrincipal(testRegistry, id, getComponentTestContent(), isPublic);
    }
    
    private static ComponentDescription addComponentAnotherPrincipal(ComponentRegistry testRegistry, String id, InputStream content, boolean isPublic) throws ParseException,
            JAXBException {
        ComponentDescription desc = ComponentDescription.createNewDescription();
        desc.setCreatorName("AnotherPrincipal");
        desc.setUserId("AnotherPrincipal");
        desc.setDbUserId(2);
        desc.setName(id);
        desc.setDescription("Test Description");
        desc.setId(ComponentDescription.COMPONENT_PREFIX + id);
        desc.setPublic(isPublic);
        return addComponent(content, testRegistry, desc);
    }
    
    public static String getProfileTestContentString() {
        return getProfileTestContentString("Actor");
    }
    
    private static String getProfileTestContentString(String name) {
        return getProfileTestContentString(name, "development");
    }
    
    private static String getProfileTestContentString(String name, String status) {
        String profileContent = "";
        profileContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<ComponentSpec CMDVersion=\"1.2\" isProfile=\"true\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "    xsi:noNamespaceSchemaLocation=\"http://infra.clarin.eu/CMDI/1.x/xsd/cmd-component.xsd\">\n"
                + "    <Header>\n"
                + "        <ID>clarin.eu:cr1:p_12345678</ID>\n"
                + "        <Name>" + name + "</Name>\n"
                + "        <Description>" + name + " description</Description>\n"
                + "        <Status>" + status + "</Status>\n"
                + "    </Header>\n"
                + "    <Component name=\"" + name + "\" CardinalityMin=\"1\" CardinalityMax=\"1\">\n"
                + "        <AttributeList>\n"
                + "            <Attribute name=\"Name\" ValueScheme=\"string\" />\n"
                + "        </AttributeList>\n"
                + "        <Element name=\"Age\">\n"
                + "            <ValueScheme>\n"
                + "                <pattern>[23][0-9]</pattern>\n"
                + "            </ValueScheme>\n"
                + "        </Element>\n"
                + "    </Component>\n"
                + "</ComponentSpec>";
        return profileContent;
    }
    
    public static InputStream getTestProfileContent() {
        return getTestProfileContent("Actor");
    }
    
    public static InputStream getTestProfileContent(String name) {
        return getTestProfileContent(name, DEVELOPMENT.toString());
    }
    
    public static InputStream getTestProfileContent(String name, String status) {
        return new ByteArrayInputStream(getProfileTestContentString(name, status).getBytes());
    }
    
    public static ProfileDescription addProfile(ComponentRegistry testRegistry, String id, boolean isPublic) throws ParseException, JAXBException, ItemNotFoundException {
        final ComponentStatus status = isPublic ? PRODUCTION : DEVELOPMENT;
        return addProfile(testRegistry, id, RegistryTestHelper.getTestProfileContent(id, status.toString()), isPublic, status);
    }
    
    public static ProfileDescription addProfile(ComponentRegistry testRegistry, String id, String content, boolean isPublic, ComponentStatus status) throws ParseException,
            JAXBException, ItemNotFoundException {
        return addProfile(testRegistry, id, new ByteArrayInputStream(content.getBytes()), isPublic, status);
    }
    
    private static ProfileDescription addProfile(ComponentRegistry testRegistry, String id, InputStream content, boolean isPublic, ComponentStatus status) throws ParseException,
            JAXBException, ItemNotFoundException {
        ProfileDescription desc = ProfileDescription.createNewDescription();
        desc.setCreatorName(DummyPrincipal.DUMMY_CREDENTIALS.getDisplayName());
        desc.setUserId(DummyPrincipal.DUMMY_CREDENTIALS.getPrincipalName());
        desc.setName(id);
        desc.setDescription("Test Description");
        desc.setId(ProfileDescription.PROFILE_PREFIX + id);
        desc.setPublic(isPublic);
        desc.setStatus(status);
        return addProfile(content, testRegistry, desc);
    }
    
    public static ProfileDescription addProfile(InputStream content, ComponentRegistry testRegistry, ProfileDescription desc) throws JAXBException {
        ComponentSpec spec = marshaller.unmarshal(ComponentSpec.class, content, marshaller.getComponentSchema());
        testRegistry.register(desc, spec);
        return desc;
    }
    
    public static ProfileDescription addProfileAnotherPrincipal(ComponentRegistry testRegistry, String id, boolean isPublic) throws ParseException, JAXBException, ItemNotFoundException {
        return addProfileAnotherPrincipal(testRegistry, id, RegistryTestHelper.getTestProfileContent(), isPublic);
    }
    
    private static ProfileDescription addProfileAnotherPrincipal(ComponentRegistry testRegistry, String id, InputStream content, boolean isPublic) throws ParseException,
            JAXBException, ItemNotFoundException {
        ProfileDescription desc = ProfileDescription.createNewDescription();
        desc.setCreatorName("AnotherPrincipal");
        desc.setUserId("AnotherPrincipal");
        desc.setDbUserId(2);
        desc.setName(id);
        desc.setDescription("Test Description");
        desc.setId(ProfileDescription.PROFILE_PREFIX + id);
        desc.setPublic(isPublic);
        return addProfile(content, testRegistry, desc);
    }
    
    public static ComponentSpec getTestProfile() throws JAXBException {
        return marshaller.unmarshal(ComponentSpec.class, getTestProfileContent(), marshaller.getComponentSchema());
    }

    public static ComponentSpec getTestProfile(String name, String status) throws JAXBException {
        return marshaller.unmarshal(ComponentSpec.class, getTestProfileContent(name, status), marshaller.getComponentSchema());
    }
    
    public static String getComponentTestContentString() {
        return getComponentTestContentString("Access");
    }
    
    public static InputStream getComponentTestContent() {
        return getComponentTestContent("Access");
    }
    
    public static InputStream getComponentTestContent(String name) {
        return getComponentTestContentAsStream(name);
    }
    
    public static InputStream getComponentTestContent(String name, String status) {
        return getComponentTestContentAsStream(name, status);
    }
    
    public static String getComponentTestContentString(String componentName) {
        return getComponentTestContentString(componentName, "development");
    }
    
    public static String getComponentTestContentString(String componentName, String status) {
        String compContent = "";
        compContent += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        compContent += "\n";
        compContent += "<ComponentSpec CMDVersion=\"1.2\" isProfile=\"false\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        compContent += "    xsi:noNamespaceSchemaLocation=\"http://infra.clarin.eu/CMDI/1.x/xsd/cmd-component.xsd\">\n";
        compContent += "    <Header>\n";
        compContent += "     <ID>clarin.eu:cr1:p_12345678</ID>\n";
        compContent += "     <Name>" + componentName + "</Name>\n";
        compContent += "     <Description>" + componentName + " description</Description>\n";
        compContent += "     <Status>" + status + "</Status>\n";
        compContent += "    </Header>\n";
        compContent += "    <Component name=\"" + componentName + "\" CardinalityMin=\"1\" CardinalityMax=\"1\">\n";
        compContent += "        <Element name=\"Availability\" ValueScheme=\"string\" />\n";
        compContent += "        <Element name=\"Date\">\n";
        compContent += "            <ValueScheme>\n";
        compContent += "                <!-- matching dates of the pattern yyyy-mm-dd (ISO 8601); this only matches dates from the years 1000 through 2999 and does allow some invalid dates (e.g. February, the 30th) -->\n";
        compContent += "                <pattern>(1|2)\\d{3}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])</pattern>                \n";
        compContent += "            </ValueScheme>\n";
        compContent += "        </Element>\n";
        compContent += "        <Element name=\"Owner\" ValueScheme=\"string\" />\n";
        compContent += "        <Element name=\"Publisher\" ValueScheme=\"string\" />\n";
        compContent += "    </Component>\n";
        compContent += "\n";
        compContent += "</ComponentSpec>\n";
        return compContent;
    }
    
    public static String getStringFromStream(InputStream largeProfileStream) throws IOException {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(largeProfileStream));
            StringBuilder profileStringBuilder = new StringBuilder();
            String line;
            while (null != (line = bufferedReader.readLine())) {
                profileStringBuilder.append(line);
            }
            return profileStringBuilder.toString();
        } finally {
            largeProfileStream.close();
        }
    }
    ///////////////////////////////////////////////////////

    public static String getLargeProfileContent() throws IOException {
        InputStream largeProfileStream = RegistryTestHelper.class.getResourceAsStream("/xml/largeProfile.xml");
        return getStringFromStream(largeProfileStream);
    }

    //////////////////////
    public static ComponentSpec getComponentFromString(String contentString) throws JAXBException {
        return marshaller.unmarshal(ComponentSpec.class, getComponentContentAsStream(contentString), marshaller.getComponentSchema());
    }
    
    public static InputStream getComponentContentAsStream(String content) {
        return new ByteArrayInputStream(content.getBytes());
    }
    
    public static InputStream getComponentTestContentAsStream(String componentName) {
        return getComponentContentAsStream(getComponentTestContentString(componentName));
    }
    
    public static InputStream getComponentTestContentAsStream(String componentName, String status) {
        return getComponentContentAsStream(getComponentTestContentString(componentName, status));
    }
    
    public static ComponentSpec getTestComponent() throws JAXBException {
        return marshaller.unmarshal(ComponentSpec.class, getComponentTestContent(), marshaller.getComponentSchema());
    }
    
    public static ComponentSpec getTestComponent(String name, String status) throws JAXBException {
        return marshaller.unmarshal(ComponentSpec.class, getComponentTestContent(name, status), marshaller.getComponentSchema());
    }
    
    public static ComponentSpec getTestComponent(String name) throws JAXBException {
        return marshaller.unmarshal(ComponentSpec.class, getComponentTestContentAsStream(name), marshaller.getComponentSchema());
    }
    
    public static String getXml(ComponentSpec componentSpec) throws JAXBException, UnsupportedEncodingException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        marshaller.marshal(componentSpec, os);
        String xml = os.toString();
        try {
            os.close();
        } catch (IOException ex) {
        }
        return xml;
    }
    
    public static Comment addComment(ComponentRegistry testRegistry, String id, String descriptionId, String principal) throws ParseException, JAXBException, ComponentRegistryException, ItemNotFoundException, UserUnauthorizedException, AuthenticationRequiredException {
        return addComment(testRegistry, RegistryTestHelper.getTestCommentContent(id, descriptionId), principal);
    }
    
    private static Comment addComment(ComponentRegistry testRegistry, InputStream content, String principal) throws ParseException,
            JAXBException,
            ComponentRegistryException, ItemNotFoundException, UserUnauthorizedException, AuthenticationRequiredException {
        Comment spec = marshaller.unmarshal(Comment.class, content, null);
        testRegistry.registerComment(spec, principal);
        return spec;
    }
    
    public Comment addCommentBypassAuthorisation(CommentsDao commentsDao, String id, String descriptionId, String principal) throws ParseException, JAXBException, ComponentRegistryException, ItemNotFoundException, UserUnauthorizedException {
        return addCommentBypassAuthorisation(commentsDao, RegistryTestHelper.getTestCommentContent(id, descriptionId), principal);
    }
    
    private Comment addCommentBypassAuthorisation(CommentsDao commentsDao, InputStream content, String principal) throws ParseException,
            JAXBException,
            ComponentRegistryException, ItemNotFoundException, UserUnauthorizedException {
        Comment comment = marshaller.unmarshal(Comment.class, content, null);
        comment.setCommentDate(new Date());
        comment.setUserId(2);
        commentsDao.saveAndFlush(comment);
        return comment;
    }
    
    public static String getCommentTestContentStringForProfile(String commentName, String profileId) {
        String comContent = "";
        comContent += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        comContent += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
        comContent += "    <comments>" + commentName + "</comments>\n";
        comContent += "    <commentDate>" + DatesHelper.createNewDate() + "</commentDate>\n";
        comContent += "    <componentId>" + profileId + "</componentId>\n";
        comContent += "    <userName>JUnit@test.com</userName>\n";
        comContent += "</comment>\n";
        return comContent;
    }
    
    public static String getanotherPrincipalCommentTestContentStringForProfile(String commentName, String profileId) {
        String comContent = "";
        comContent += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        comContent += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
        comContent += "    <comments>" + commentName + "</comments>\n";
        comContent += "    <commentDate>" + DatesHelper.createNewDate() + "</commentDate>\n";
        comContent += "    <componentId>" + profileId + "</componentId>\n";
        comContent += "    <userName>anotherPrincipal</userName>\n";
        comContent += "</comment>\n";
        return comContent;
    }
    
    public static String getCommentTestContentStringForComponent(String commentName, String componentId) {
        String comContent = "";
        comContent += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        comContent += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
        comContent += "    <comments>" + commentName + "</comments>\n";
        comContent += "    <commentDate>" + DatesHelper.createNewDate() + "</commentDate>\n";
        comContent += "     <componentId>" + componentId + "</componentId>";
        comContent += "    <userName>JUnit@test.com</userName>\n";
        comContent += "</comment>\n";
        return comContent;
    }
    
    public static String getAnotherPrincipalCommentTestContentStringForComponent(String commentName, String componentId) {
        String comContent = "";
        comContent += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        comContent += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
        comContent += "    <comments>" + commentName + "</comments>\n";
        comContent += "    <commentDate>" + DatesHelper.createNewDate() + "</commentDate>\n";
        comContent += "     <componentId>" + componentId + "</componentId>";
        comContent += "    <userName>anotherPrincipal</userName>\n";
        comContent += "</comment>\n";
        return comContent;
    }
    
    public static InputStream getTestCommentContent(String content, String descriptionId) {
        if (descriptionId.contains("profile")) {
            return new ByteArrayInputStream(getCommentTestContentStringForProfile(content, descriptionId).getBytes());
        } else {
            return new ByteArrayInputStream(getCommentTestContentStringForComponent(content, descriptionId).getBytes());
        }
    }
    
    public static String getCommentTestContent(String commentId, String descriptionId) {
        return getCommentTestContentStringForProfile(commentId, descriptionId);
    }
    
    public static InputStream getCommentTestContent() {
        return getTestCommentContent("Actual", ProfileDescription.PROFILE_PREFIX + "profile1");
    }

    /**
     * Testing a big xsd string is a bit hard, so doing a best effort by
     * checking the xs:element which represent the nested components used in a
     * profile/component
     */
    public static boolean hasComponent(String xsd, String name, String min, String max) {
        Pattern pattern = Pattern.compile("<xs:element name=\"" + name + "\" minOccurs=\"" + min + "\" maxOccurs=\"" + max + "\">");
        Matcher matcher = pattern.matcher(xsd);
        return matcher.find() && !matcher.find(); //find only one
    }

    /**
     *
     * @param bytes is an array of bytes to be written in the file filename
     * (from scratch!)
     * @param filename is the name of the file where the array "bytes" is to be
     * written to
     * @throws IOException
     * @throws JAXBException
     */
    public static void writeBytesToFile(byte[] bytes, String filename) throws IOException, JAXBException {
        
        File file = new File(filename);
        FileOutputStream fop = new FileOutputStream(file);
        
        fop.write(bytes);
        
        fop.flush();
        fop.close();
        
    }

    /**
     *
     * @param str is a string which is to be written into the filename (from
     * scratch!)
     * @param filename is a filename where the string is to be written to
     * @throws IOException
     * @throws JAXBException
     */
    public static void writeStringToFile(String str, String filename) throws IOException, JAXBException {
        
        writeBytesToFile(str.getBytes(), filename);
        
    }

    /**
     *
     * @param os is an output stream which is to be written into the filename
     * (from scratch!)
     * @param filename is a filename where the stream is to be written to
     * @throws IOException
     * @throws JAXBException
     */
    public static void writeStreamToFile(ByteArrayOutputStream os, String filename) throws IOException, JAXBException {
        
        writeBytesToFile(os.toByteArray(), filename);
        
    }

    /**
     *
     * @param cdesc is a component which is to be written into the filename
     * (from scratch!)
     * @param filename is a filename where the component is to be written to
     * @throws IOException
     * @throws JAXBException
     */
    public static void writeComponentIntoFile(ComponentDescription cdesc, String filename) throws IOException, JAXBException {
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        marshaller.marshal(cdesc, os);
        
        writeStreamToFile(os, filename);
        
    }

    /**
     * opens a temporary sub-directory dirName in /target/
     *
     * @param dirName is the name of the temporary subdirectory which is to be
     * opened
     * @return the absolute part for this directory
     */
    public static String openTestDir(String dirName) {
        
        File testDir = new File("target/" + dirName);
        
        testDir.mkdir();
        
        System.out.println(dirName);
        //String retval = new File(testDir, dirName).getAbsolutePath();
        String retval = new File(testDir, "/").getAbsolutePath();
        
        return (retval);
        
    }
}
