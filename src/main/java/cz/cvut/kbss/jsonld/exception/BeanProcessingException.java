package cz.cvut.kbss.jsonld.exception;

/**
 * Thrown when an error occurs during instance processing.
 */
public class BeanProcessingException extends RuntimeException {

    public BeanProcessingException(String message) {
        super(message);
    }

    public BeanProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanProcessingException(Throwable cause) {
        super(cause);
    }
}
