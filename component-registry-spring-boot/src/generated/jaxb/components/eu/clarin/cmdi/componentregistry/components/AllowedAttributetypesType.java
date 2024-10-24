//
// This file was generated by the Eclipse Implementation of JAXB, v4.0.5 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
//


package eu.clarin.cmdi.componentregistry.components;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Subset of XSD types that are allowed as CMD type
 * 
 * <p>Java class for allowed_attributetypes_type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="allowed_attributetypes_type">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     <enumeration value="boolean"/>
 *     <enumeration value="decimal"/>
 *     <enumeration value="float"/>
 *     <enumeration value="int"/>
 *     <enumeration value="string"/>
 *     <enumeration value="anyURI"/>
 *     <enumeration value="date"/>
 *     <enumeration value="gDay"/>
 *     <enumeration value="gMonth"/>
 *     <enumeration value="gYear"/>
 *     <enumeration value="time"/>
 *     <enumeration value="dateTime"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "allowed_attributetypes_type")
@XmlEnum
public enum AllowedAttributetypesType {

    @XmlEnumValue("boolean")
    BOOLEAN("boolean"),
    @XmlEnumValue("decimal")
    DECIMAL("decimal"),
    @XmlEnumValue("float")
    FLOAT("float"),
    @XmlEnumValue("int")
    INT("int"),
    @XmlEnumValue("string")
    STRING("string"),
    @XmlEnumValue("anyURI")
    ANY_URI("anyURI"),
    @XmlEnumValue("date")
    DATE("date"),
    @XmlEnumValue("gDay")
    G_DAY("gDay"),
    @XmlEnumValue("gMonth")
    G_MONTH("gMonth"),
    @XmlEnumValue("gYear")
    G_YEAR("gYear"),
    @XmlEnumValue("time")
    TIME("time"),
    @XmlEnumValue("dateTime")
    DATE_TIME("dateTime");
    private final String value;

    AllowedAttributetypesType(String v) {
        value = v;
    }

    /**
     * Gets the value associated to the enum constant.
     * 
     * @return
     *     The value linked to the enum.
     */
    public String value() {
        return value;
    }

    /**
     * Gets the enum associated to the value passed as parameter.
     * 
     * @param v
     *     The value to get the enum from.
     * @return
     *     The enum which corresponds to the value, if it exists.
     * @throws IllegalArgumentException
     *     If no value matches in the enum declaration.
     */
    public static AllowedAttributetypesType fromValue(String v) {
        for (AllowedAttributetypesType c: AllowedAttributetypesType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}