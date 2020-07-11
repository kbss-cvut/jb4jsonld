package cz.cvut.kbss.jsonld.exception;

/**
 * Indicates that there still exist unresolved pending references after deserialization finish.
 */
public class UnresolvedReferenceException extends JsonLdDeserializationException {

    public UnresolvedReferenceException(String message) {
        super(message);
    }
}
