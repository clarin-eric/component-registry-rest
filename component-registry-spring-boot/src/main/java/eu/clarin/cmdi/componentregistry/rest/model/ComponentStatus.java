package eu.clarin.cmdi.componentregistry.rest.model;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.xml.bind.annotation.XmlEnumValue;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public enum ComponentStatus {

    // Please DO NOT change the order of these values as they are mapped to
    // integer values in the database ('status' column of 'basedescription' 
    // table)! Any new values should be appended, or the database values need
    // to be refactored.
    @XmlEnumValue("development")
    DEVELOPMENT("development"),
    @XmlEnumValue("production")
    PRODUCTION("production"),
    @XmlEnumValue("deprecated")
    DEPRECATED("deprecated");

    private final String stringValue;

    ComponentStatus(String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    @JsonValue
    public String toString() {
        return stringValue;
    }

    public static class StringToComponentStatusConverter implements Converter<String, ComponentStatus> {

        @Override
        public ComponentStatus convert(String source) {
            if (source == null) {
                return null;
            } else try {
                return ComponentStatus.valueOf(source.toUpperCase());
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }

}
