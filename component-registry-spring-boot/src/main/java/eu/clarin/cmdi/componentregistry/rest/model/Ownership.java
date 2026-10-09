package eu.clarin.cmdi.componentregistry.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import java.io.Serializable;

/**
 * Models ownership of a profile or component by a user or group. A valid
 * {@link Ownership} has either the {@link #profileId} or the
 * {@link #componentId} filled out and either the {@link #groupId} or the
 * {@link #userId}.
 *
 * @author twan@clarin.eu
 * @author george.georgovassilis@mpi.nl
 *
 */
@XmlRootElement(name = "ownership")
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "ownership")
public class Ownership implements Serializable {

    @Id
    @SequenceGenerator(name = "ownership_id_seq", sequenceName = "ownership_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ownership_id_seq")
    @Column(name = "id")
    @XmlTransient
    @JsonIgnore
    private Long dbId;

    @Column(name = "componentid")
    private String componentId;

    @Column(name = "groupid")
    private long groupId;

    @Column(name = "userid")
    private long userId;

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public long getGroupId() {
        return groupId;
    }

    /**
     *
     * @param groupId 0 denotes no group (user must be set)
     */
    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getUserId() {
        return userId;
    }

    /**
     *
     * @param userId 0 denotes no user (group must be set)
     */
    public void setUserId(long userId) {
        this.userId = userId;
    }
}
