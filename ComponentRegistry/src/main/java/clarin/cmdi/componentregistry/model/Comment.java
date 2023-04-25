package clarin.cmdi.componentregistry.model;

import clarin.cmdi.componentregistry.impl.ComponentUtils;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import clarin.cmdi.componentregistry.util.XmlDateAdapter;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

/**
 * 
 * @author Jean-Charles Ferrières <jean-charles.ferrieres@mpi.nl>
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author george.georgovassilis@mpi.nl
 */
@XmlRootElement(name = "comment")
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "comments")
@XmlType(propOrder={"comments","commentDate","componentId","id","userName","canDelete"})
public class Comment {

    @Column(nullable = false)
    private String comments;

    @XmlJavaTypeAdapter(XmlDateAdapter.class)
    @Column(name = "comment_date", nullable = false)
    private Date commentDate;

    @Column(name = "component_id")
    private String componentId;

    @XmlTransient
    @Column(name = "id")
    @Id
    @SequenceGenerator(name = "comments_id_seq", sequenceName = "comments_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comments_id_seq")
    private Long dbId;

    @Transient
    @XmlTransient
    private String id;

    @Column(name = "user_name")
    private String userName;

    @Transient
    private boolean canDelete;
    @XmlTransient
    // this prevents userId from being serialized to XML and thus exposed (which
    // is useless and undesirable)
    @Column(name = "user_id", nullable = false)
    private long userId;

    public void setComment(String comment) {
	this.comments = comment;
    }

    public String getComment() {
	return comments;
    }

    public void setCommentDate(Date commentDate) {
	this.commentDate = commentDate;
    }

    public Date getCommentDate() {
	return commentDate;
    }

    public void setId(String commentId) {
	this.id = commentId;
	try {
	    this.dbId = Long.valueOf(commentId);
	} catch (NumberFormatException e) {
	}
    }

    @XmlElement
    public String getId() {
	return id== null?dbId+"":id;
    }

    public String getComponentRef() {
	return componentId;
    }

    public void setComponentRef(String componentDescriptionId) {
	this.componentId = componentDescriptionId;
    }

    public void setUserId(long userId) {
	this.userId = userId;
    }

    public long getUserId() {
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
     * @param userName
     *            the user's 'real' name, not login name
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
     * @param canDelete
     *            whether comment can be deleted
     */
    public void setCanDelete(boolean canDelete) {
	this.canDelete = canDelete;
    }

    public static Date getDate(String registrationDate) throws ParseException {
	return DateUtils.parseDate(registrationDate,
		new String[] { DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT
			.getPattern() });
    }

    /**
     * Compares two comments by their value as returned by
     * {@link Comment#getCommentDate()
     * }
     */
    public static final Comparator<Comment> COMPARE_ON_DATE = new Comparator<Comment>() {
	/**
	 * @return -1 if o11-s date is bigger than  o2's (o1 is younger), o1 goes up
         * so the standard order (older up, smaller date is up) is reversed
	 */
	@Override
	public int compare(Comment o1, Comment o2) {
	    if (o1.getCommentDate() == o2.getCommentDate())
		return 0;
	    if (o1.getCommentDate() == null)
		return -1; // o1 goes down
	    if (o2.getCommentDate() == null)
		return 1; // o1 goes up
	    return ComponentUtils.COMPARE_ON_DATE.compare(o1.getCommentDate(),  o2.getCommentDate());
	}
    };
}
