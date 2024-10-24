//
// This file was generated by the Eclipse Implementation of JAXB, v4.0.5 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
//


package eu.clarin.cmdi.componentregistry.components;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * controlled vocabularies
 * 
 * <p>Java class for enumeration_type complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="enumeration_type">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="appinfo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="item" type="{}item_type" maxOccurs="unbounded"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "enumeration_type", propOrder = {
    "appinfo",
    "item"
})
public class EnumerationType {

    /**
     * End-user guidance about the value of the controlled vocabulary as a whole. Currently not used.
     * 
     */
    protected String appinfo;
    /**
     * An item from a controlled vocabulary.
     * 
     */
    @XmlElement(required = true)
    protected List<ItemType> item;

    /**
     * End-user guidance about the value of the controlled vocabulary as a whole. Currently not used.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAppinfo() {
        return appinfo;
    }

    /**
     * Sets the value of the appinfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getAppinfo()
     */
    public void setAppinfo(String value) {
        this.appinfo = value;
    }

    /**
     * An item from a controlled vocabulary.
     * 
     * Gets the value of the item property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the item property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ItemType }
     * </p>
     * 
     * 
     * @return
     *     The value of the item property.
     */
    public List<ItemType> getItem() {
        if (item == null) {
            item = new ArrayList<>();
        }
        return this.item;
    }

}
