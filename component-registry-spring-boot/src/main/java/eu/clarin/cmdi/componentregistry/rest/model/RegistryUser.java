package eu.clarin.cmdi.componentregistry.rest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;


/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
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
