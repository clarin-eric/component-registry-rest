package clarin.cmdi.componentregistry.rss;

import clarin.cmdi.componentregistry.util.DatesHelper;
import clarin.cmdi.componentregistry.model.Comment;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author olhsha
 */
public class RssCreatorComments extends RssCreator<Comment> {

    /**
     *
     * @param isPrivate if "true" then profiles and components from the user's
     * workspace, otherwise -- public (uri-parameter)
     * @param baseURI where the database is located
     * @param limit number of items to be displayed (uri-parameter)
     * @param descriptionId Id of the profile/component whose comments are
     * considered
     * @param descriptionName Name of the profile/component whose comments are
     * considered
     * @param descriptionType "profile" or "component"
     * @param comms the list of comments from which the list of rss-items for
     * the channel will be created
     * @param comparator compare comments by dates
     */
    public RssCreatorComments(String baseURI, int limit,
            String descriptionId, String descriptionName, String descriptionType, List<Comment> comms, Comparator<Comment> comparator, String title) {
        super(baseURI, limit, comms);
        setChannelLink(baseURI + "?item=" + descriptionId + "&browserview=comments");
        setChannelTitle(title);
        setChannelDescription(String.format("Comments feed for the %s \"%s\"", descriptionType, descriptionName));
        setComparator(comparator);
    }

    @Override
    protected RssItem fromArgToRssItem(Comment comm) {
        final String itemLink = getChannelLink() + "&commentId=" + comm.getId();
        RssItem retval = new RssItem();
        retval.setDescription(comm.getComment());
        retval.setGuid(makeGuid(itemLink));
        retval.setLink(itemLink);
        retval.setPubDate(DatesHelper.getRFCDateTime(comm.getCommentDate()));
        retval.setTitle(String.format("Comment %1$s\nby %2$s", comm.getId(), comm.getUserName()));
        return retval;
    }
}
