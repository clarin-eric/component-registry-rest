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


@XmlRootElement(name = "groupmembership")
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "groupmembership")
public class GroupMembership implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long groupId;
    private long userId;

    public long getId() {
	return id;
    }

    public void setId(long id) {
	this.id = id;
    }

    public long getGroupId() {
	return groupId;
    }

    public void setGroupId(long groupId) {
	this.groupId = groupId;
    }

    public long getUserId() {
	return userId;
    }

    public void setUserId(long userId) {
	this.userId = userId;
    }
}
