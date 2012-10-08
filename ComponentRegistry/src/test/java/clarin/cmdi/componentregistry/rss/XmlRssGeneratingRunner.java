package clarin.cmdi.componentregistry.rss;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.rest.RegistryTestHelper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author olhsha, non-automatised developer's test class Main method generates
 * an Rss xml-file to validate it later using one of the on-line rss-source
 * validators
 */
public class XmlRssGeneratingRunner {

    public static <T extends AbstractDescription> void printIds(List<T> desc) {
        for (T current : desc) {
            String currentId = current.getId();
            System.out.println(currentId);
        }
    }

    private static void printXmlRssToFile(Rss rssObject) throws IOException, JAXBException {

        String path = RegistryTestHelper.openTestDir("testRss");
        String os = MDMarshaller.marshalToString(rssObject);
        RegistryTestHelper.writeStringToFile(os, path + "testRssResl.xml");

    }

    private static Rss makeRssForDescriptions(List<? extends AbstractDescription> descriptions, int kind, String baseUri, int limit) throws ParseException {
        System.out.println(descriptions.size());
        Collections.sort(descriptions, AbstractDescription.COMPARE_ON_DATE);
        System.out.println(descriptions.size());

        System.out.println("check if the descriptions are sorted in a proper way, by the dates ");
        for (AbstractDescription desc : descriptions) {
            System.out.println(desc.getRegistrationDate());
        }

        RssCreatorDescriptions instance = new RssCreatorDescriptions(false, baseUri, (kind == 1) ? "profiles" : "components", limit, descriptions, AbstractDescription.COMPARE_ON_DATE);
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

        RssCreatorComments instance = new RssCreatorComments(false, baseUri, limit, profileId, profileName, (kind == 3) ? "profile" : "component", comments, Comment.COMPARE_ON_DATE);
        Rss result = instance.getRss();
        return result;
    }

    public static void main(String args[]) throws ComponentRegistryException, ParseException, IOException, JAXBException {

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

        Rss rss=null;
        String baseUri = "http://localhost:8080/ComponentRegistry"; /* used only as a nice URi-example content for the "link"-field
         in this test we do not click on link, we just generate an Rss xml-file to validate it
         using one of the on-line rss-source vaidators */
        
        if (kind == 1 || kind == 2) { // testing Rss for profiles/components
            List<? extends AbstractDescription> descriptions =
                (kind == 1) ? registry.getProfileDescriptions() : registry.getComponentDescriptions();
            rss = makeRssForDescriptions(descriptions, kind, baseUri, 10);
        };

        if (kind == 3 || kind == 4) { // testing Rss comments
            List<? extends AbstractDescription> descriptions =
                (kind == 3) ? registry.getProfileDescriptions() : registry.getComponentDescriptions();
            printIds(descriptions);
            System.out.println("Pick up and input one of the description id above");// "clarin.eu:cr1:p_1284723009187" "clarin.eu:cr1:c_1288172614011"
            try {
                buffer = br.readLine();
            } catch (IOException ioe) {
                System.out.println("IO error trying to get the id");
                System.exit(1);
            }
            final List<Comment> comments = (kind == 3) ? registry.getCommentsInProfile(buffer, null) : registry.getCommentsInComponent(buffer, null);
            final String name = (kind == 3) ? registry.getProfileDescription(buffer).getName() : registry.getComponentDescription(buffer).getName();
            rss = makeRssForComments(comments, kind, baseUri, 10, buffer, name);

        }
        printXmlRssToFile(rss);
    }
}
