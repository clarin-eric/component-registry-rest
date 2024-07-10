package clarin.cmdi.componentregistry.frontend;

import clarin.cmdi.componentregistry.IMarshaller;
import java.io.Serializable;

import clarin.cmdi.componentregistry.RegistrySpace;
import clarin.cmdi.componentregistry.BaseDescription;
import clarin.cmdi.componentregistry.model.ComponentStatus;

public class CMDItemInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final IMarshaller marshaller;
    private String description;
    private String id;
    private String content;
    private String name;
    private ComponentStatus status;
    private boolean published;
    private boolean forceUpdate = false;

    private DisplayDataNode displayNode;

    private boolean deletable = false;
    private boolean undeletable = false;

    private boolean editable = false;

    public CMDItemInfo(IMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setDescription(String descriptionText) {
        this.description = descriptionText;
    }

    public void setDescription(BaseDescription desc) {
        this.description = marshaller.marshalToString(desc);
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        if (displayNode != null) {
            name = displayNode.toString();
        }
        return name;
    }

    public ComponentStatus getStatus() {
        return status;
    }

    public void setStatus(ComponentStatus status) {
        this.status = status;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public DisplayDataNode getDataNode() {
        return displayNode;
    }

    public void setDataNode(DisplayDataNode dataNode) {
        this.displayNode = dataNode;
        setUndeletable(false);
        setDeletable(false);
        setEditable(false);
        setDescription("");
        setContent("");
        setId("");
        setPublished(false);
        setStatus(null);
        if (dataNode != null) {
            BaseDescription desc = dataNode.getDescription();
            if (desc != null) {
                String contentDescr = marshaller.marshalToString(desc);
                setDescription(contentDescr);
                setId(desc.getId());
                setEditable(true);
                setStatus(desc.getStatus());
                setPublished(desc.isPublic());
                setName(desc.getName());
                if (dataNode.isDeleted()) {
                    setUndeletable(true);
                } else {
                    setDeletable(true);
                }
            }
        }

    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setUndeletable(boolean undeletable) {
        this.undeletable = undeletable;
    }

    public boolean isUndeletable() {
        return undeletable;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public RegistrySpace getSpace() {
        return displayNode.getSpace();
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

}
