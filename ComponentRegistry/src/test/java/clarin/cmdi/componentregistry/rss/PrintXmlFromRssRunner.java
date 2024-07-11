package clarin.cmdi.componentregistry.rss;

import clarin.cmdi.componentregistry.AuthenticationRequiredException;
import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.util.DatesHelper;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.util.ComponentUtils;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * non-automatised developer's test class Main method generates an Rss xml-file
 * to validate it later using one of the on-line rss-source validators
 */
public class PrintXmlFromRssRunner {

    private static MDMarshaller marshaller;

    @Autowired
    public void setMarshaller(MDMarshaller marshaller) {
	this.marshaller = marshaller;
    }

    public static <T extends BaseDescription> void printIds(List<T> desc) {
	for (T current : desc) {
	    String currentId = current.getId();
	    System.out.println(currentId);
	}
    }

    private static void printXmlRssToFile(Rss rssObject) throws IOException, JAXBException {

	String path = RegistryTestHelper.openTestDir("testRss");
	String os = marshaller.marshalToString(rssObject);
	RegistryTestHelper.writeStringToFile(os, path + "testRssResl.xml");

    }

    private static Rss makeRssForDescriptions(List<? extends BaseDescription> descriptions, int kind, String baseUri, int limit) throws ParseException {
	System.out.println(descriptions.size());
	Collections.sort(descriptions, BaseDescription.COMPARE_ON_DATE);
	System.out.println(descriptions.size());

	System.out.println("check if the descriptions are sorted in a proper way, by the dates ");
	for (BaseDescription desc : descriptions) {
	    Date date = desc.getRegistrationDate();
	    System.out.println(date + ", formatted: " + date
		    + ", Rss=formatted: " + DatesHelper.getRFCDateTime(date));
	}

	RssCreatorDescriptions instance = new RssCreatorDescriptions(baseUri, (kind == 1) ? "profiles" : "components", limit, descriptions, ComponentUtils.COMPARE_ON_DATE, "Public descriptions");
	Rss result = instance.getRss();

	return result;

    }

    private static Rss makeRssForComments(List<Comment> comments, int kind, String baseUri, int limit, String profileId, String profileName) throws ParseException {
	System.out.println(comments.size());
	Collections.sort(comments, Comment.COMPARE_ON_DATE);
	System.out.println(comments.size());

	System.out.println("check if the comments are sorted in a proper way, by the dates ");
	for (Comment commentje : comments) {
	    System.out.println(commentje.getCommentDate());
	}

	RssCreatorComments instance = new RssCreatorComments(baseUri, limit, profileId, profileName, (kind == 3) ? "profile" : "component", comments, Comment.COMPARE_ON_DATE, "Comments for profile "+profileId);
	Rss result = instance.getRss();
	return result;
    }

    /*
     * input: sort of rss -- profiles, or comopnents, or comments (see below the prompt string)
     */
    public static void main(String args[]) throws ComponentRegistryException, ParseException, IOException, JAXBException, UserUnauthorizedException, ItemNotFoundException, AuthenticationRequiredException {

	System.out.println("Type 1 or 2, or 3, or 4, \n "
		+ "to check Rss generaion for profiles, components  or comments for a profile or a component respectively: >> ");

	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	String buffer = null;

	//  read the username from the command-line; need to use try/catch with the
	//  readLine() method
	try {
	    buffer = br.readLine();
	} catch (IOException ioe) {
	    System.out.println("IO error trying get the number");
	    System.exit(1);
	}

	int kind = Integer.parseInt(buffer);

	ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/applicationContextJDBC.xml");
	ComponentRegistry registry = ((ComponentRegistryFactory) applicationContext.getBean("componentRegistryFactory")).getPublicRegistry();

	Rss rss = null;
	String baseUri = "http://localhost:8080/ComponentRegistry"; /* used only as a nice URi-example content for the "link"-field
	 in this test we do not click on link, we just generate an Rss xml-file to validate it
	 using one of the on-line rss-source vaidators */

	if (kind == 1 || kind == 2) { // testing Rss for profiles/components
	    List<? extends BaseDescription> descriptions =
		    (kind == 1) ? registry.getProfileDescriptions(null) : registry.getComponentDescriptions(null);
	    rss = makeRssForDescriptions(descriptions, kind, baseUri, 10);
	};

	if (kind == 3 || kind == 4) { // testing Rss comments
	    List<? extends BaseDescription> descriptions =
		    (kind == 3) ? registry.getProfileDescriptions(null) : registry.getComponentDescriptions(null);
	    printIds(descriptions);
	    System.out.println("Pick up and input one of the description id above");// "clarin.eu:cr1:p_1284723009187" "clarin.eu:cr1:c_1288172614011"
	    try {
		buffer = br.readLine();
	    } catch (IOException ioe) {
		System.out.println("IO error trying to get the id");
		System.exit(1);
	    }
	    final List<Comment> comments = (kind == 3) ? registry.getCommentsInProfile(buffer) : registry.getCommentsInComponent(buffer);
	    final String name = (kind == 3) ? registry.getProfileDescriptionAccessControlled(buffer).getName() : registry.getComponentDescriptionAccessControlled(buffer).getName();
	    rss = makeRssForComments(comments, kind, baseUri, 10, buffer, name);

	}
	printXmlRssToFile(rss);
    }
}
