package clarin.cmdi.componentregistry.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
@XmlAccessorType(value = XmlAccessType.FIELD)
@Entity
@Table(name = "registry_user")
public class RegistryUser implements Serializable {

    @Column(name = "name")
    private String name;

    @Column(name = "principal_name", unique = true)
    private String principalName;

    @Id
    @SequenceGenerator(name = "registry_user_id_seq", sequenceName = "registry_user_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "registry_user_id_seq")
    @Column(name = "id")
    private Long id;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        if (getName() == null) {
            return getPrincipalName();
        } else {
            return String.format("%s [%s]", getName(), getPrincipalName());
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RegistryUser other = (RegistryUser) obj;
        return Objects.equals(this.id, other.id);
    }

}
