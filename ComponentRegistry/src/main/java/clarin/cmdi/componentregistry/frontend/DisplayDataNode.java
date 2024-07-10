package clarin.cmdi.componentregistry.frontend;

import clarin.cmdi.componentregistry.RegistrySpace;
import java.io.Serializable;

import clarin.cmdi.componentregistry.model.BaseDescription;
import java.util.Objects;

public class DisplayDataNode implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String name;
    private final boolean isDeleted;
    private BaseDescription desc;
    private final RegistrySpace space;
    private final String identifier;
    private final boolean isItem;
    private int parentHash;

    protected DisplayDataNode(String name, boolean isDeleted, Object parent) {
        this(name, false, isDeleted, parent);
    }

    protected DisplayDataNode(String name, boolean isItem, boolean isDeleted, Object parent) {
        // TODO: what is sensible default status?
        this(name, isItem, isDeleted, null, RegistrySpace.PRIVATE, parent);
    }

    public DisplayDataNode(String name, boolean isItem, boolean isDeleted, BaseDescription desc, RegistrySpace space, Object parent) {
        this.name = name;
        this.isItem = isItem;
        this.isDeleted = isDeleted;
        this.desc = desc;
        this.space = space;
        if (desc == null) {
            this.identifier = null;
        } else {
            this.identifier = desc.getId();
        }
        this.parentHash = Objects.hashCode(parent);
    }

    /**
     * Can be null for non leaves.
     *
     * @return
     */
    public BaseDescription getDescription() {
        return desc;
    }

    public void setDesc(BaseDescription desc) {
        this.desc = desc;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public String toString() {
        return name;
    }

    public RegistrySpace getSpace() {
        return space;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.name);
        hash = 89 * hash + Objects.hashCode(this.space);
        hash = 89 * hash + Objects.hashCode(this.identifier);
        hash = 89 * hash + (this.isItem ? 1 : 0);
        hash = 89 * hash + this.parentHash;
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
        final DisplayDataNode other = (DisplayDataNode) obj;
        if (this.isItem != other.isItem) {
            return false;
        }
        if (this.parentHash != other.parentHash) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.identifier, other.identifier)) {
            return false;
        }
        if (this.space != other.space) {
            return false;
        }
        return true;
    }


    
    

    public static DisplayDataNode newItemNode(String name, String identifier, boolean isDeleted, BaseDescription desc, RegistrySpace space, Object parent) {
        return new DisplayDataNode(name, true, isDeleted, desc, space, parent);
    }

    public static DisplayDataNode newNonItemNode(String name, boolean isDeleted, Object parent) {
        return new DisplayDataNode(name, isDeleted, parent);
    }

}
