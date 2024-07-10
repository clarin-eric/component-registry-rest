package clarin.cmdi.componentregistry.validation;

import clarin.cmdi.componentregistry.BaseUnitTest;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

/**
 * Test class for CommentValidator
 * 
 * @author jean-charles Ferrières <jean-charles.ferrieres@mpi.nl>
 * @author George.Georgovassilis@mpi.nl
 */
public class CommentValidatorTest extends BaseUnitTest {

    /**
     * Fully successful comment validation
     */
    @Test
    public void testValidateSucces() {
	BaseDescription description = ProfileDescription
		.createNewDescription();
	String comContent = "";
	comContent += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
	comContent += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
	comContent += "    <comments>myCom</comments>\n";
	comContent += "    <commentDate></commentDate>\n";
	comContent += "     <componentId>" + description.getId()+ "</componentId>";
	comContent += "    <userId>0</userId>\n";
	comContent += "    <id>1</id>\n";
	comContent += "</comment>\n";
	InputStream input = new ByteArrayInputStream(comContent.getBytes());
	CommentValidator validator = new CommentValidator(input, description,
		marshaller);
	assertTrue(validator.validate());
    }

    /**
     * Test the validation of comment with componentID
     */
    @Test
    public void testValidateComponent() {
	BaseDescription desc = ComponentDescription.createNewDescription();
	String comContent = "";
	comContent += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
	comContent += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
	comContent += "    <comments>myCom</comments>\n";
	comContent += "    <commentDate></commentDate>\n";
	comContent += "     <componentDescriptionId></componentDescriptionId>";
	comContent += "    <userId>0</userId>\n";
	comContent += "    <id>1</id>\n";
	comContent += "</comment>\n";
	InputStream input = new ByteArrayInputStream(comContent.getBytes());
	CommentValidator validator = new CommentValidator(input, desc,
		marshaller);
	assertFalse(validator.validate());// missing componentId will return an
					  // error
	assertEquals(validator.getErrorMessages().size(), 1);
	assertTrue(validator.getErrorMessages().get(0)
		.startsWith(CommentValidator.COMMENT_SPECIFICATION_ERROR));

	String commentContent = "";
	commentContent += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
	commentContent += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
	commentContent += "    <comments>myCom</comments>\n";
	commentContent += "    <commentDate></commentDate>\n";
	commentContent += "     <componentId>" + desc.getId()
		+ "</componentId>";
	commentContent += "    <userId>0</userId>\n";
	commentContent += "    <id>1</id>\n";
	commentContent += "</comment>\n";
	input = new ByteArrayInputStream(commentContent.getBytes());
	validator = new CommentValidator(input, desc, marshaller);
	assertTrue(validator.validate()); // componentId valid, validation
					  // complete
    }

    /***
     * Test the validation of comment with profileId
     */
    @Test
    public void testValidateProfile() {
	BaseDescription desc = ProfileDescription.createNewDescription();
	String comContent = "";
	comContent += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
	comContent += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
	comContent += "    <comments>myCom</comments>\n";
	comContent += "    <commentDate></commentDate>\n";
	comContent += "     <profileDescriptionId></profileDescriptionId>";
	comContent += "    <userId>0</userId>\n";
	comContent += "    <id>1</id>\n";
	comContent += "</comment>\n";
	InputStream input = new ByteArrayInputStream(comContent.getBytes());
	CommentValidator validator = new CommentValidator(input, desc,
		marshaller);
	assertFalse(validator.validate());// missing profileId will return an
					  // error
	assertEquals(validator.getErrorMessages().size(), 1);
	assertTrue(validator.getErrorMessages().get(0)
		.startsWith(CommentValidator.COMMENT_SPECIFICATION_ERROR));

	String commentContent = "";
	commentContent += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
	commentContent += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
	commentContent += "    <comments>myCom</comments>\n";
	commentContent += "    <commentDate></commentDate>\n";
	commentContent += "     <componentId>" + desc.getId()
		+ "</componentId>";
	commentContent += "    <userId>0</userId>\n";
	commentContent += "    <id>1</id>\n";
	commentContent += "</comment>\n";
	input = new ByteArrayInputStream(commentContent.getBytes());
	validator = new CommentValidator(input, desc, marshaller);
	assertTrue(validator.validate()); // profileId valid, validation
					  // complete
    }

    /**
     * Test the validation of comment with content comments
     * 
     * @throws Exception
     */
    @Test
    public void testValidateNoCommentContent() throws Exception {
	BaseDescription desc = ProfileDescription.createNewDescription();
	String commentContent = "";
	commentContent += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
	commentContent += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
	commentContent += "    <comments></comments>\n";
	commentContent += "    <commentDate></commentDate>\n";
	commentContent += "     <profileDescriptionId>" + desc.getId()
		+ "</profileDescriptionId>";
	commentContent += "    <userId>0</userId>\n";
	commentContent += "    <id>1</id>\n";
	commentContent += "</comment>\n";
	InputStream input = new ByteArrayInputStream(commentContent.getBytes());
	CommentValidator validator = new CommentValidator(input, desc,
		marshaller);
	assertFalse(validator.validate()); // missing content will return an
					   // error
	assertTrue(validator.getErrorMessages().get(0)
		.startsWith(CommentValidator.COMMENT_SPECIFICATION_ERROR));

	String comContent = "";
	comContent += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
	comContent += "<comment xmlns:ns2=\"http://www.w3.org/1999/xlink\">\n";
	comContent += "    <comments>myCom</comments>\n";
	comContent += "    <commentDate></commentDate>\n";
	comContent += "     <componentId>" + desc.getId()
		+ "</componentId>";
	comContent += "    <userId>0</userId>\n";
	comContent += "    <id>1</id>\n";
	comContent += "</comment>\n";
	input = new ByteArrayInputStream(comContent.getBytes());
	validator = new CommentValidator(input, desc, marshaller);
	assertTrue(validator.validate());// content comments is fill in,
					 // validation complete

    }
}
