//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.09.04 at 02:51:29 PM CEST 
//


package clarin.cmdi.componentregistry.components;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for CMD_Element_type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CMD_Element_type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AttributeList" type="{}AttributeList_type" minOccurs="0"/>
 *         &lt;element name="ValueScheme" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice>
 *                   &lt;element name="pattern" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="enumeration" type="{}enumeration_type"/>
 *                 &lt;/choice>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{}clarin_element_attributes"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CMD_Element_type", propOrder = {
    "attributeList",
    "valueScheme"
})
public class CMDElementType {

    @XmlElement(name = "AttributeList")
    protected AttributeListType attributeList;
    @XmlElement(name = "ValueScheme")
    protected CMDElementType.ValueScheme valueScheme;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String name;
    @XmlAttribute(name = "ConceptLink")
    @XmlSchemaType(name = "anyURI")
    protected String conceptLink;
    @XmlAttribute(name = "ValueScheme")
    protected AllowedAttributetypesType valueScheme2;
    @XmlAttribute(name = "CardinalityMin")
    protected List<String> cardinalityMin;
    @XmlAttribute(name = "CardinalityMax")
    protected List<String> cardinalityMax;

    /**
     * Gets the value of the attributeList property.
     * 
     * @return
     *     possible object is
     *     {@link AttributeListType }
     *     
     */
    public AttributeListType getAttributeList() {
        return attributeList;
    }

    /**
     * Sets the value of the attributeList property.
     * 
     * @param value
     *     allowed object is
     *     {@link AttributeListType }
     *     
     */
    public void setAttributeList(AttributeListType value) {
        this.attributeList = value;
    }

    /**
     * Gets the value of the valueScheme property.
     * 
     * @return
     *     possible object is
     *     {@link CMDElementType.ValueScheme }
     *     
     */
    public CMDElementType.ValueScheme getValueScheme() {
        return valueScheme;
    }

    /**
     * Sets the value of the valueScheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link CMDElementType.ValueScheme }
     *     
     */
    public void setValueScheme(CMDElementType.ValueScheme value) {
        this.valueScheme = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the conceptLink property.
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
     */
    public void setConceptLink(String value) {
        this.conceptLink = value;
    }

    /**
     * Gets the value of the valueScheme2 property.
     * 
     * @return
     *     possible object is
     *     {@link AllowedAttributetypesType }
     *     
     */
    public AllowedAttributetypesType getValueScheme2() {
        return valueScheme2;
    }

    /**
     * Sets the value of the valueScheme2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link AllowedAttributetypesType }
     *     
     */
    public void setValueScheme2(AllowedAttributetypesType value) {
        this.valueScheme2 = value;
    }

    /**
     * Gets the value of the cardinalityMin property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cardinalityMin property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCardinalityMin().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getCardinalityMin() {
        if (cardinalityMin == null) {
            cardinalityMin = new ArrayList<String>();
        }
        return this.cardinalityMin;
    }

    /**
     * Gets the value of the cardinalityMax property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cardinalityMax property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCardinalityMax().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getCardinalityMax() {
        if (cardinalityMax == null) {
            cardinalityMax = new ArrayList<String>();
        }
        return this.cardinalityMax;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;choice>
     *         &lt;element name="pattern" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="enumeration" type="{}enumeration_type"/>
     *       &lt;/choice>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "pattern",
        "enumeration"
    })
    public static class ValueScheme {

        protected String pattern;
        protected EnumerationType enumeration;

        /**
         * Gets the value of the pattern property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getPattern() {
            return pattern;
        }

        /**
         * Sets the value of the pattern property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setPattern(String value) {
            this.pattern = value;
        }

        /**
         * Gets the value of the enumeration property.
         * 
         * @return
         *     possible object is
         *     {@link EnumerationType }
         *     
         */
        public EnumerationType getEnumeration() {
            return enumeration;
        }

        /**
         * Sets the value of the enumeration property.
         * 
         * @param value
         *     allowed object is
         *     {@link EnumerationType }
         *     
         */
        public void setEnumeration(EnumerationType value) {
            this.enumeration = value;
        }

    }

}
