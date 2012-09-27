package clarin.cmdi.componentregistry.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

/**
 *
 * @author Jean-Charles Ferrières <jean-charles.ferrieres@mpi.nl>
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
@XmlRootElement(name = "comment")
@XmlAccessorType(XmlAccessType.FIELD)
public class Comment {

    private String comments;
    private String commentDate;
    private String componentDescriptionId;
    private String profileDescriptionId;
    private String id;
    private String userName;
    private boolean canDelete;
    @XmlTransient // this prevents userId from being serialized to XML and thus exposed (which is useless and undesirable)
    private String userId;

    public void setComment(String comment) {
	this.comments = comment;
    }

    public String getComment() {
	return comments;
    }

    public void setCommentDate(String commentDate) {
	this.commentDate = commentDate;
    }

    public String getCommentDate() {
	return commentDate;
    }

    public void setId(String commentId) {
	this.id = commentId;
    }

    public String getId() {
	return id;
    }

    public String getComponentDescriptionId() {
	return componentDescriptionId;
    }

    public void setComponentDescriptionId(String ComponentDescriptionId) {
	this.componentDescriptionId = ComponentDescriptionId;
    }

    public void setProfileDescriptionId(String profileDescriptionId) {
	this.profileDescriptionId = profileDescriptionId;
    }

    public String getProfileDescriptionId() {
	return profileDescriptionId;
    }

    public void setUserId(String userId) {
	this.userId = userId;
    }

    public String getUserId() {
	return userId;
    }

    /**
     *
     * @return userName, that is the user's 'real' name, not login name
     */
    public String getUserName() {
	return userName;
    }

    /**
     * @param userName the user's 'real' name, not login name
     */
    public void setUserName(String userName) {
	this.userName = userName;
    }

    /**
     * @return whether comment can be deleted
     */
    public boolean isCanDelete() {
	return canDelete;
    }

    /**
     * @param canDelete whether comment can be deleted
     */
    public void setCanDelete(boolean canDelete) {
	this.canDelete = canDelete;
    }

    public static Date getDate(String registrationDate) throws ParseException {
	return DateUtils.parseDate(registrationDate, new String[]{DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern()});
    }

    
    
    /*
     * Helper method to set the Date
     */
    public static String createNewDate() {
	return createNewDate(new Date().getTime());
    }

    /*
     * Helper method to set the Date in the same format
     * @param time, long that contains the time to be set
     */
    public static String createNewDate(long time) {
	return DateFormatUtils.formatUTC(time, DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
    }

    public static Comment createANewComment() {
	Comment com = new Comment();
	com.setCommentDate(createNewDate());
	return com;
    }
    
    
    public static final Comparator<Comment> COMPARE_ON_DATE = new Comparator<Comment>() {
        
        //compare two commments by the date of registration
        // the dates, which are strings accrording to  ISO 8601, compared lexicographically 
    @Override    
    public int compare(Comment o1, Comment o2) {
            
            int result = 0;
            
            Date d1 = DatatypeConverter.parseDateTime(o1.getCommentDate()).getTime();
            Date d2 = DatatypeConverter.parseDateTime(o2.getCommentDate()).getTime();
            
            result = d2.compareTo(d1);
            
            return result;
        }
    };
}
