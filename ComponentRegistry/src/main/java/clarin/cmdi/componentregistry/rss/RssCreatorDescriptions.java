package clarin.cmdi.componentregistry.rss;

import clarin.cmdi.componentregistry.util.DatesHelper;
import clarin.cmdi.componentregistry.model.BaseDescription;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author olhsha
 */
public class RssCreatorDescriptions<T extends BaseDescription> extends RssCreator<T> {

    /**
     *
     * @param isPrivate if "true" then profiles and components from the user's
     * workspace, otherwise -- public (uri-parameter)
     * @param baseURI where the database is located
     * @param descriptionType "profile" or "component"
     * @param limit number of items to be displayed (uri-parameter)
     * @param descriptions the list of comments from which the list of rss-items
     * for the channel will be created
     * @param comparator compare descriptions by dates
     */
    public RssCreatorDescriptions(String baseURI, String descriptionType,
            int limit, List<T> descriptions, Comparator<T> comparator, String title) {
        super(baseURI, limit, descriptions);
        setChannelLink(baseURI + "/");
        setChannelTitle(title);
        setChannelDescription(String.format("News feed for the %s", descriptionType));
        setComparator(comparator);
    }

    /**
     * creator method, desc to rssItem, overrides the dummy method of the
     * RssCreatorClass
     *
     * @param desc
     * @return
     */
    @Override
    protected RssItem fromArgToRssItem(T desc) {
        String href = getBaseURI() + "?item=" + desc.getId();
        RssItem retval = new RssItem();
        retval.setDescription(desc.getDescription());
        retval.setGuid(makeGuid(href));
        retval.setLink(href);
        retval.setPubDate(DatesHelper.getRFCDateTime(desc.getRegistrationDate()));
        retval.setTitle(makeDescriptionTitle(desc.getName(), desc.getCreatorName(), desc.getGroupName()));
        return retval;
    }

    protected String makeDescriptionTitle(String name, String creatorname, String group) {
        final String help = (group == null) ? "is unspecified" : group;
        return (name + " by user " + creatorname + ", group " + help);
    }
}
