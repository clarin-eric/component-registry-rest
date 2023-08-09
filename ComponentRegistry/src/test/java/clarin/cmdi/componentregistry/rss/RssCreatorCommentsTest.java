package clarin.cmdi.componentregistry.rss;

import clarin.cmdi.componentregistry.DatesHelper;
import clarin.cmdi.componentregistry.model.Comment;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 *
 * @author olhsha
 */
public class RssCreatorCommentsTest {

    private Comment makeTestComment(boolean canDelete, boolean isFromProfile, String comtext, Date date, String commentId,
            String descrId, String userName) {
        Comment comm = new Comment();

        comm.setCanDelete(canDelete);
        comm.setComment(comtext);
        comm.setCommentDate(date);
        comm.setComponentRef(descrId);
        comm.setId(commentId);
        comm.setUserName(userName);

        return comm;
    }

    /**
     *
     */
    private void compareInputsVsRssItems(String href, String commtext, String date, String title, RssItem rssItem) {
        assertEquals(href, rssItem.getGuid().getValue());
        assertEquals(href, rssItem.getLink());
        assertEquals(commtext, rssItem.getDescription());
        assertEquals(date, rssItem.getPubDate());
        assertEquals(title, rssItem.getTitle());

    }
    
    private Date date(String d) {
	return DatesHelper.parseIso(d);
    }

    @Test
    public void testMakeRss() throws ParseException {
        String testPrfId = "p_1234";
        String baseUri = "http://catalog.clarin.eu/ds/ComponentRegistry";
        boolean isFromProfile = true;

        Comment comm1 = makeTestComment(true, isFromProfile, "this is comment # 1", date("2012-04-02T11:38:23+00:00"), "commentId1", testPrfId,
                "userello");
        Comment comm2 = makeTestComment(false, isFromProfile, "this is comment # 2", date("2011-04-02T11:38:22+00:00"), "commentId2", testPrfId,
                "userino");
        Comment comm3 = makeTestComment(true, isFromProfile, "this is comment # 3", date("2010-05-02T11:38:22+00:00"), "commentId3", testPrfId,
                "userito");
        List<Comment> comms = new ArrayList<Comment>(Arrays.asList(comm1, comm2, comm3));

        RssCreatorComments instance = new RssCreatorComments(baseUri, 3, testPrfId, "Test Profile", "profile", comms, Comment.COMPARE_ON_DATE, "Comments for a private profile");
        Rss result = instance.getRss();

        String rfcdate1 = DatesHelper.getRFCDateTime(DatesHelper.parseIso("2012-04-02T11:38:23+00:00"));
        String rfcdate2 = DatesHelper.getRFCDateTime(DatesHelper.parseIso("2011-04-02T11:38:22+00:00"));
        String rfcdate3 = DatesHelper.getRFCDateTime(DatesHelper.parseIso("2010-05-02T11:38:22+00:00"));

        List<RssItem> resitems = result.getChannel().getItem();
        String channelLink = baseUri + "?item=" + testPrfId + "&browserview=comments";
        compareInputsVsRssItems(channelLink + "&commentId=commentId1", "this is comment # 1", rfcdate1,
                "Comment commentId1\nby userello", resitems.get(0));
        compareInputsVsRssItems(channelLink + "&commentId=commentId2", "this is comment # 2", rfcdate2,
                "Comment commentId2\nby userino", resitems.get(1));
        compareInputsVsRssItems(channelLink + "&commentId=commentId3", "this is comment # 3", rfcdate3,
                "Comment commentId3\nby userito", resitems.get(2));

        assertEquals("2.0", Double.toString(result.getVersion()));
        assertEquals("Comments for a private profile", result.getChannel().getTitle());
        assertEquals(channelLink, result.getChannel().getLink());
        assertEquals("Comments feed for the profile \"Test Profile\"", result.getChannel().getDescription());
    }
}
