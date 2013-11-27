package clarin.cmdi.componentregistry.frontend;

import clarin.cmdi.componentregistry.ComponentStatus;
import java.io.Serializable;

import clarin.cmdi.componentregistry.model.BaseDescription;

public class DisplayDataNode implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String name;
    private final boolean isDeleted;
    private BaseDescription desc;
    private final ComponentStatus status;

    public DisplayDataNode(String name, boolean isDeleted) {
	// TODO: what is sensible default status?
	this(name, isDeleted, null, ComponentStatus.PRIVATE);
    }

    public DisplayDataNode(String name, boolean isDeleted, BaseDescription desc, ComponentStatus status) {
	this.name = name;
	this.isDeleted = isDeleted;
	this.desc = desc;
	this.status = status;
    }

    /**
     * Can be null for non leaves.
     *
     * @return
     */
    public BaseDescription getDescription() {
	return desc;
    }

    public boolean isDeleted() {
	return isDeleted;
    }

    @Override
    public String toString() {
	return name;
    }

    public ComponentStatus getStatus() {
	return status;
    }
}
