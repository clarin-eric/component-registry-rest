package clarin.cmdi.componentregistry;

public class AuthenticationRequiredException extends Exception {

    public AuthenticationRequiredException(String message) {
        super(message);
    }

    public AuthenticationRequiredException(String message, Throwable t) {
        super(message, t);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

}
