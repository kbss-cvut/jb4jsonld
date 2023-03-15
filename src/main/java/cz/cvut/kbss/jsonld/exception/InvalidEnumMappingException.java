package cz.cvut.kbss.jsonld.exception;

/**
 * Indicates that enum constant cannot be mapped to/from JSON-LD.
 */
public class InvalidEnumMappingException extends JsonLdException {

    public InvalidEnumMappingException(String message) {
        super(message);
    }
}
