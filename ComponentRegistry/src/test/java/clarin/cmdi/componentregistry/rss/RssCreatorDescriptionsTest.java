package clarin.cmdi.componentregistry.rss;

import clarin.cmdi.componentregistry.DatesHelper;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBException;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author olhsha
 */
public class RssCreatorDescriptionsTest {

    private void createTestDescription(AbstractDescription desc, String creatorname,
            String description, String groupname,
            String name, String date, String id) {

        desc.setId(id);
        desc.setName(name);
        desc.setCreatorName(creatorname);
        desc.setDescription(description);
        desc.setGroupName(groupname);
        desc.setRegistrationDate(date);

    }

    private ProfileDescription createTestProfileDescription(String creatorname,
            String description, String groupname,
            String name, boolean editorFlag, String date, String id) {

        ProfileDescription pdesc = ProfileDescription.createNewDescription();
        createTestDescription(pdesc, creatorname, description, groupname, name, date, id);
        pdesc.setShowInEditor(editorFlag);
        return pdesc;
    }

    private ComponentDescription createTestComponentDescription(String creatorname,
            String description, String groupname,
            String name, String date, String id) {

        ComponentDescription cdesc = ComponentDescription.createNewDescription();
        createTestDescription(cdesc, creatorname, description, groupname, name, date, id);
        return cdesc;
    }

    private void compareRssVsValues(String description, String itemLink, String date, String title, RssItem item) {

        assertEquals(description, item.getDescription());
        assertEquals(itemLink, item.getGuid().getValue());
        assertEquals(itemLink, item.getLink());
        assertEquals(date, item.getPubDate());
        assertEquals(title, item.getTitle());
    }

    /**
     * Creates 3 profile descriptions, makes them into a list, and makes a
     * 3-item Rss out of this list Checks if the values of the fields in each
     * item are as expected
     */
    @Test
    public void testMakeRss() throws JAXBException, UnsupportedEncodingException, IOException, ParseException {
        String baseURI = "http://catalog.clarin.eu/ds/ComponentRegistry";
        String channelLink = baseURI + "/";
        boolean userspace = false;

        ProfileDescription desc1 = createTestProfileDescription("Useratti",
                "description-1", "groupname-1", "name-1", true, "2001-01-01", "p_1");
        ProfileDescription desc2 = createTestProfileDescription("Usereno",
                "description-2", "groupname-2", "name-2", false, "2001-01-02", "p_2");
        ProfileDescription desc3 = createTestProfileDescription("Userio",
                "description-3", "groupname-3", "name-3", true, "2001-01-03", "p_3");

        List<ProfileDescription> descriptions = Arrays.asList(desc1, desc2, desc3);

        RssCreatorDescriptions instance = new RssCreatorDescriptions(userspace, baseURI, "profiles", 3, descriptions, AbstractDescription.COMPARE_ON_DATE);
        Rss result = instance.getRss();

        List<RssItem> items = result.getChannel().getItem();
        assertEquals(3, result.getChannel().getItem().size());
        compareRssVsValues("description-1", baseURI + "?item=p_1", DatesHelper.getRFCDateTime("2001-01-01"),
                instance.makeDescriptionTitle("name-1", "Useratti", "groupname-1"), items.get(0));
        compareRssVsValues("description-2", baseURI + "?item=p_2", DatesHelper.getRFCDateTime("2001-01-02"),
                instance.makeDescriptionTitle("name-2", "Usereno", "groupname-2"), items.get(1));
        compareRssVsValues("description-3", baseURI + "?item=p_3", DatesHelper.getRFCDateTime("2001-01-03"),
                instance.makeDescriptionTitle("name-3", "Userio", "groupname-3"), items.get(2));

        assertEquals("2.0", Double.toString(result.getVersion()));
        assertEquals("News feed for the profiles", result.getChannel().getDescription());
        assertEquals(channelLink, result.getChannel().getLink());
        assertEquals("Public profiles", result.getChannel().getTitle());
    }
}