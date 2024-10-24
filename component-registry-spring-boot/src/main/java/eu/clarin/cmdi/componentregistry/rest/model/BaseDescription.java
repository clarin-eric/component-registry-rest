package eu.clarin.cmdi.componentregistry.rest.model;

import eu.clarin.cmdi.componentregistry.util.ComponentUtils;
import eu.clarin.cmdi.componentregistry.util.XmlDateAdapter;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

//import org.apache.commons.codec.digest.DigestUtils;
/**
 * The BaseComponent (formally AbstractDescription) models profiles and
 * components alike by containing <strong>all</strong> their persistent
 * attributes. It is meant to serve as a base for XML generation and JPA
 * persistence. Extending classes are not allowed to model any persistent
 * attributes.
 *
 * @author george.georgovassilis@mpi.nl
 *
 */
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@XmlRootElement(name = "description")
@XmlAccessorType(XmlAccessType.FIELD)
//@XmlSeeAlso({ComponentDescription.class, ProfileDescription.class})
@Entity
@Table(name = "basedescription")
public class BaseDescription implements Serializable {

    //TODO: Add status, derivedFrom and successor fields
//
//    @SequenceGenerator(name = "basedescription_id_seq", sequenceName = "basedescription_id_seq", allocationSize = 1, initialValue = 1)
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "basedescription_id_seq")
    @Id
    @Column(name = "id")
    @XmlTransient
    private Long dbId;
//
    @XmlElement(name = "id")
    @Column(name = "component_id", nullable = false, unique = true)
    private String componentId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "registration_date")
    @XmlJavaTypeAdapter(XmlDateAdapter.class)
    private Date registrationDate;

    @Column(name = "creator_name")
    private String creatorName;

    @XmlTransient
    @Column(name = "user_id", columnDefinition = "integer")
    private long dbUserId;

    @Transient
    private String userId;
//
    @Column(name = "domain_name")
    private String domainName;
//
    @Transient
    @XmlElement(namespace = "http://www.w3.org/1999/xlink")
    private String href;
//
    @Column(name = "group_name")
    private String groupName;
//
    @Column(name = "status")
    private ComponentStatus status;
//
    @Column(name = "derivedfrom", nullable = true)
    private String derivedfrom;
//
    @Column(name = "successor", nullable = true)
    private String successor;
//
    @Transient
    private int commentsCount;
//
    @Column(name = "show_in_editor", nullable = false)
    @XmlTransient
    @Builder.Default
    private boolean shownInEditor = true;
//
    @XmlTransient
    @Column(name = "content", nullable = false)
    @Builder.Default
    private String content = "";
//
    @XmlElement(name = "isPublic")
    @JsonProperty("isPublic")
    @Column(name = "is_public", nullable = false)
    private boolean ispublic;
//
    @XmlTransient
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;
//
    @XmlElement(name = "recommended")
    @Column(name = "recommended", nullable = false)
    private boolean recommended;

    public long getDbUserId() {
        return dbUserId;
    }

    public void setDbUserId(long dbUserId) {
        this.dbUserId = dbUserId;
        setUserId("" + dbUserId);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Whether this profile should be shown in metadata editor (e.g. Arbil)
     *
     * @return the value of showInEditor
     */
    public boolean isShowInEditor() {
        return shownInEditor;
    }

    /**
     * Gets whether this profile should be shown in metadata editor (e.g. Arbil)
     *
     * @param showInEditor new value of showInEditor
     */
    public void setShowInEditor(boolean showInEditor) {
        this.shownInEditor = showInEditor;
    }

    public void setId(String id) {
//        if (id != null && !ComponentUtils.isComponentId(id)
//                && !ComponentUtils.isProfileId(id)) {
//            throw new IllegalArgumentException(
//                    "ID doesn't follow the naming schema for components or profiles "
//                    + id);
//        }
        this.componentId = id;
    }

    public String getId() {
        return componentId;
    }

    public Long getDbId() {
        return dbId;
    }

    public void setDbId(Long dbId) {
        this.dbId = dbId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setUserId(String userId) {
        try {
            this.dbUserId = Long.parseLong(userId);
        } catch (Exception e) {
            this.dbUserId = 0;
        }
        this.userId = userId;
    }

    /**
     * MD5 string representation of the user id. Storing the hash because the
     * userId can be the email address which we don't want to make public.
     */
    public String getUserId() {
        if (userId == null) {
            return "" + dbUserId;
        }
        return userId;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getHref() {
        return href;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    /**
     * @return the number of comments posted on this component
     */
    public int getCommentsCount() {
        return commentsCount;
    }

    /**
     * @param commentsCount the number of comments posted on this component
     */
    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public ComponentStatus getStatus() {
        return status;
    }

    public void setStatus(ComponentStatus status) {
        this.status = status;
    }

    public void setDerivedfrom(String derivedfrom) {
        this.derivedfrom = derivedfrom;
    }

    public String getDerivedfrom() {
        return derivedfrom;
    }

    public void setSuccessor(String successor) {
        this.successor = successor;
    }

    public String getSuccessor() {
        return successor;
    }

    @Override
    public String toString() {
        return "Name=" + getName() + ", id=" + getId() + ", creatorName="
                + getCreatorName() + ", userId=" + getUserId();
    }

//    public boolean isProfile() {
//        return this instanceof ProfileDescription;
//    }
//
//    public String getType() {
//        return isProfile() ? "profile" : "component";
//    }
    /**
     * Helper method.
     *
     * @param userId normal string which will be checked to see if it matches
     * the md5 hash of the stored userId
     */
//    public boolean isThisTheOwner(String userId) {
//        String userHash = DigestUtils.md5Hex(userId);
//        return userHash.equals(getUserId());
//    }
    public boolean isPublic() {
        return ispublic;
    }

    public void setPublic(boolean ispublic) {
        this.ispublic = ispublic;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public void setRecommended(boolean recommended) {
        this.recommended = recommended;
    }

    /**
     * Compares two descriptions by the their value as returned by
     * {@link BaseDescription#getRegistrationDate() () * }
     */
    public static final Comparator<? super BaseDescription> COMPARE_ON_DATE = new Comparator<BaseDescription>() {
        /**
         * @returns 1 if o11 is older than o2 (to the bottom), returns -1 if o1
         * is younger (to the top) o2
         */
        @Override
        public int compare(BaseDescription o1, BaseDescription o2) {
            // we need to sort not in standard ascending orde, but in descending, from higher (later date) to the smaller (older date)
            return ComponentUtils.COMPARE_ON_DATE.compare(o1.getRegistrationDate(), o2.getRegistrationDate());
        }
    };

}
