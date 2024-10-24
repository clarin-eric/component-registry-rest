//
// This file was generated by the Eclipse Implementation of JAXB, v4.0.5 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
//


package eu.clarin.cmdi.componentregistry.components;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ValueScheme_type complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="ValueScheme_type">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <choice>
 *         <element name="pattern" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         <element name="Vocabulary" type="{}Vocabulary_type"/>
 *       </choice>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ValueScheme_type", propOrder = {
    "pattern",
    "vocabulary"
})
public class ValueSchemeType {

    /**
     * Specification of a regular expression the element should comply with.
     * 
     */
    protected String pattern;
    /**
     * Specification of an open or closed vocabulary
     * 
     */
    @XmlElement(name = "Vocabulary")
    protected VocabularyType vocabulary;

    /**
     * Specification of a regular expression the element should comply with.
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
     * @see #getPattern()
     */
    public void setPattern(String value) {
        this.pattern = value;
    }

    /**
     * Specification of an open or closed vocabulary
     * 
     * @return
     *     possible object is
     *     {@link VocabularyType }
     *     
     */
    public VocabularyType getVocabulary() {
        return vocabulary;
    }

    /**
     * Sets the value of the vocabulary property.
     * 
     * @param value
     *     allowed object is
     *     {@link VocabularyType }
     *     
     * @see #getVocabulary()
     */
    public void setVocabulary(VocabularyType value) {
        this.vocabulary = value;
    }

}
