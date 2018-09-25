package cz.cvut.kbss.jsonld.exception;

/**
 * Indicates that no type info was found when serializing an object.
 */
public class MissingTypeInfoException extends JsonLdSerializationException {

    public MissingTypeInfoException(String message) {
        super(message);
    }
}
