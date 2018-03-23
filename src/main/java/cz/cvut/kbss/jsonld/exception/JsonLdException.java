package cz.cvut.kbss.jsonld.exception;

/**
 * Generic exception for issues with JB4JSON-LD.
 *
 * Subclasses indicate particular problems.
 */
public class JsonLdException extends RuntimeException {

    public JsonLdException() {
    }

    public JsonLdException(String message) {
        super(message);
    }

    public JsonLdException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonLdException(Throwable cause) {
        super(cause);
    }
}
