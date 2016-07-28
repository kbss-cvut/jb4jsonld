package cz.cvut.kbss.jsonld.exception;

/**
 * Thrown when an error occurs during deserialization of JSON-LD into POJO(s).
 */
public class JsonLdDeserializationException extends RuntimeException {

    public JsonLdDeserializationException(String message) {
        super(message);
    }

    public JsonLdDeserializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonLdDeserializationException(Throwable cause) {
        super(cause);
    }
}
