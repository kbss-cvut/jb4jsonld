package cz.cvut.kbss.jsonld.exception;

/**
 * Indicates that an unsupported temporal type has been passed to this library.
 */
public class UnsupportedTemporalTypeException extends JsonLdException {

    public UnsupportedTemporalTypeException(String message) {
        super(message);
    }
}
