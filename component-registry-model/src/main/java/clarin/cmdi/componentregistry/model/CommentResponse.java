package clarin.cmdi.componentregistry.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author jean-charles Ferrières <jean-charles.ferrieres@mpi.nl>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "commentResponse")

public class CommentResponse extends ComponentRegistryResponse {

    @XmlElement
    private Comment comment;

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public Comment getComment() {
        return comment;
    }
}
