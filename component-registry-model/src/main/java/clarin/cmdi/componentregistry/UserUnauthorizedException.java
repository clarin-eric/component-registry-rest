package clarin.cmdi.componentregistry;

public class UserUnauthorizedException extends Exception {

    public UserUnauthorizedException(String message) {
        super(message);
    }

    public UserUnauthorizedException(String message, Throwable t) {
        super(message, t);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

}
