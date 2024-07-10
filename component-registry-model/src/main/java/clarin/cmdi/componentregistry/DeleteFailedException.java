package clarin.cmdi.componentregistry;

public class DeleteFailedException extends ComponentRegistryException {

    public DeleteFailedException(String message) {
        super(message);
    }

    public DeleteFailedException(String message, Throwable t) {
        super(message, t);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

}
