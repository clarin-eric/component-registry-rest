package clarin.cmdi.componentregistry.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * Models ownership of a profile or component by a user or group. A valid {@link Ownership} has either the {@link #profileId} or the {@link #componentId} filled out
 * and either the {@link #groupId} or the {@link #userId}.
 * 
 * @author george.georgovassilis@mpi.nl
 * 
 */
@XmlRootElement(name = "ownership")
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "ownership")
public class Ownership implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String componentId;
    private long groupId;
    private long userId;

    public String getComponentRef() {
	return componentId;
    }

    public void setComponentRef(String componentId) {
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
