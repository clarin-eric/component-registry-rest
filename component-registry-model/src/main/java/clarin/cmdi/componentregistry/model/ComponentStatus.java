package clarin.cmdi.componentregistry.model;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public enum ComponentStatus {

    // Please DO NOT change the order of these values as they are mapped to
    // integer values in the database ('status' column of 'basedescription' 
    // table)! Any new values should be appended, or the database values need
    // to be refactored.
    DEVELOPMENT("development"),
    PRODUCTION("production"),
    DEPRECATED("deprecated");

    private final String stringValue;

    ComponentStatus(String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public String toString() {
        return stringValue;
    }

}
