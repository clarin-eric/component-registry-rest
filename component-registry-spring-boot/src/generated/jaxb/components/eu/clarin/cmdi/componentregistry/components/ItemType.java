//
// This file was generated by the Eclipse Implementation of JAXB, v4.0.5 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
//


package eu.clarin.cmdi.componentregistry.components;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for item_type complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="item_type">
 *   <simpleContent>
 *     <extension base="<http://www.w3.org/2001/XMLSchema>string">
 *       <attribute name="ConceptLink" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       <attribute name="AppInfo" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <anyAttribute processContents='lax' namespace='http://www.clarin.eu/cmd/cues/1'/>
 *     </extension>
 *   </simpleContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "item_type", propOrder = {
    "value"
})
public class ItemType {

    @XmlValue
    protected String value;
    /**
     * A link to the ISOcat data category registry (or any other concept registry) related to this controllec vocabulary item.
     * 
     */
    @XmlAttribute(name = "ConceptLink")
    @XmlSchemaType(name = "anyURI")
    protected String conceptLink;
    /**
     * End-user guidance about the value of this controlled vocabulary item.
     * 
     */
    @XmlAttribute(name = "AppInfo")
    protected String appInfo;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<>();

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * A link to the ISOcat data category registry (or any other concept registry) related to this controllec vocabulary item.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConceptLink() {
        return conceptLink;
    }

    /**
     * Sets the value of the conceptLink property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getConceptLink()
     */
    public void setConceptLink(String value) {
        this.conceptLink = value;
    }

    /**
     * End-user guidance about the value of this controlled vocabulary item.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAppInfo() {
        return appInfo;
    }

    /**
     * Sets the value of the appInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getAppInfo()
     */
    public void setAppInfo(String value) {
        this.appInfo = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}