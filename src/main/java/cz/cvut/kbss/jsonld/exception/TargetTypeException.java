package cz.cvut.kbss.jsonld.exception;

/**
 * Thrown when an issue occurs when deserializing JSON-LD input into the target Java type.
 */
public class TargetTypeException extends JsonLdDeserializationException {

    public TargetTypeException(String message) {
        super(message);
    }
}
