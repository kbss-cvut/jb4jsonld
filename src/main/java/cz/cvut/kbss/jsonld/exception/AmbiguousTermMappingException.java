package cz.cvut.kbss.jsonld.exception;

/**
 * Indicates that a term with the same name is already mapped by a JSON-LD context.
 */
public class AmbiguousTermMappingException extends JsonLdSerializationException {

    public AmbiguousTermMappingException(String message) {
        super(message);
    }
}
