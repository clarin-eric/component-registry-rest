package clarin.cmdi.componentregistry.rss;

import clarin.cmdi.componentregistry.util.DatesHelper;
import clarin.cmdi.componentregistry.util.ComponentUtils;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 *
 * @author olhsha
 */
public class RssCreatorDescriptionsTest {

    private void createTestDescription(BaseDescription desc, String creatorname,
            String description, String groupname,
            String name, Date date, String id) {

        desc.setId(id);
        desc.setName(name);
        desc.setCreatorName(creatorname);
        desc.setDescription(description);
        desc.setGroupName(groupname);
        desc.setRegistrationDate(date);

    }

    private ProfileDescription createTestProfileDescription(String creatorname,
            String description, String groupname,
            String name, boolean editorFlag, Date date, String id) {

        ProfileDescription pdesc = ProfileDescription.createNewDescription();
        createTestDescription(pdesc, creatorname, description, groupname, name, date, id);
        pdesc.setShowInEditor(editorFlag);
        return pdesc;
    }

    private ComponentDescription createTestComponentDescription(String creatorname,
            String description, String groupname,
            String name, Date date, String id) {

        ComponentDescription cdesc = ComponentDescription.createNewDescription();
        createTestDescription(cdesc, creatorname, description, groupname, name, date, id);
        return cdesc;
    }

    private void compareRssVsValues(String description, String itemLink, Date date, String title, RssItem item) {

        assertEquals(description, item.getDescription());
        assertEquals(itemLink, item.getGuid().getValue());
        assertEquals(itemLink, item.getLink());
        assertEquals(DatesHelper.getRFCDateTime(date), item.getPubDate());
        assertEquals(title, item.getTitle());
    }
    
    private Date date(String ymd) {
	String[] parts = ymd.split("-");
	return new Date(Integer.parseInt(parts[0])-1900,Integer.parseInt(parts[1])-1,Integer.parseInt(parts[2]));
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

        ProfileDescription desc1 = createTestProfileDescription("Useratti",
                "description-1", "groupname-1", "name-1", true, date("2001-01-01"), ProfileDescription.PROFILE_PREFIX+"p_1");
        ProfileDescription desc2 = createTestProfileDescription("Usereno",
                "description-2", "groupname-2", "name-2", false, date("2001-01-02"), ProfileDescription.PROFILE_PREFIX+"p_2");
        ProfileDescription desc3 = createTestProfileDescription("Userio",
                "description-3", "groupname-3", "name-3", true, date("2001-01-03"), ProfileDescription.PROFILE_PREFIX+"p_3");

        List<ProfileDescription> descriptions = Arrays.asList(desc1, desc2, desc3);

        RssCreatorDescriptions instance = new RssCreatorDescriptions(baseURI, "profiles", 3, descriptions, BaseDescription.COMPARE_ON_DATE, "Public profiles");
        Rss result = instance.getRss();

        List<RssItem> items = result.getChannel().getItem();
        assertEquals(3, result.getChannel().getItem().size());
        // the youngest must be on the top (at "0"), the oldest must be on the bottom (at "3")
        compareRssVsValues("description-1", baseURI + "?item=clarin.eu:cr1:p_p_1", date("2001-01-01"),
                instance.makeDescriptionTitle("name-1", "Useratti", "groupname-1"), items.get(2));
        compareRssVsValues("description-2", baseURI + "?item=clarin.eu:cr1:p_p_2", date("2001-01-02"),
                instance.makeDescriptionTitle("name-2", "Usereno", "groupname-2"), items.get(1));
        compareRssVsValues("description-3", baseURI + "?item=clarin.eu:cr1:p_p_3", date("2001-01-03"),
                instance.makeDescriptionTitle("name-3", "Userio", "groupname-3"), items.get(0));

        assertEquals("2.0", Double.toString(result.getVersion()));
        assertEquals("News feed for the profiles", result.getChannel().getDescription());
        assertEquals(channelLink, result.getChannel().getLink());
        assertEquals("Public profiles", result.getChannel().getTitle());
    }
}