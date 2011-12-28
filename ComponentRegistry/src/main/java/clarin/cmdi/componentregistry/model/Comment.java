package clarin.cmdi.componentregistry.model;

import java.text.ParseException;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

/**
 *
 * @author jean-charles Ferrières <jean-charles.ferrieres@mpi.nl>
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
}
