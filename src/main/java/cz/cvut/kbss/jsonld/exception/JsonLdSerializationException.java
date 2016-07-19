package cz.cvut.kbss.jsonld.exception;

/**
 * Represents an error during POJO serialization to JSON-LD.
 */
public class JsonLdSerializationException extends RuntimeException {

    public JsonLdSerializationException(String message) {
        super(message);
    }

    public JsonLdSerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonLdSerializationException(Throwable cause) {
        super(cause);
    }
}
