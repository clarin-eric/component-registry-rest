package clarin.cmdi.componentregistry.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author twagoo
 */
@XmlRootElement(name = "itemlock")
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "itemlock")
public class ItemLock implements Serializable {

    @Id
    @SequenceGenerator(name = "itemlock_id_seq", sequenceName = "itemlock_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "itemlock_id_seq")
    private Integer id;
    @Column(nullable = false, unique = false)
    private Long userId;
    @Column(nullable = false, unique = true)
    private Integer itemId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    /**
     * Get the value of id
     *
     * @return the value of id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Set the value of id
     *
     * @param id new value of id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Get the value of itemId
     *
     * @return the value of itemId
     */
    public Integer getItemId() {
        return itemId;
    }

    /**
     * Set the value of itemId
     *
     * @param itemId new value of itemId
     */
    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    /**
     * Get the value of userId
     *
     * @return the value of userId
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Set the value of userId
     *
     * @param userId new value of userId
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Get the value of lockTime
     *
     * @return the value of lockTime
     */
    public Date getTimestamp() {
        return creationDate;
    }

    /**
     * Set the value of lockTime
     *
     * @param timestamp new value of lockTime
     */
    public void setTimestamp(Date timestamp) {
        this.creationDate = timestamp;
    }

}
