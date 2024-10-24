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
@Entity
@Table(name = "basedescription")
public class SpecItem implements Serializable {

    @Id
    @Column(name = "id",
            updatable = false,
            insertable = false)
    @XmlTransient
    private Long dbId;
//
    @XmlElement(name = "id")
    @Column(name = "component_id",
            updatable = false,
            insertable = false,
            unique = true)
    private String componentId;

    @XmlTransient
    @Column(name = "content", nullable = false)
    @Builder.Default
    private String content = "";

    @XmlTransient
    @Column(name = "is_deleted",
            updatable = false,
            insertable = false,
            nullable = false)
    private boolean deleted;

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

    public void setId(String id) {
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

    @Override
    public String toString() {
        return "Id=" + getId() + ", content=" + getContent();
    }

}
