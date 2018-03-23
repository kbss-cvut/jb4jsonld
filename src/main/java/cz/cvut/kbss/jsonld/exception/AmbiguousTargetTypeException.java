package cz.cvut.kbss.jsonld.exception;

/**
 * Thrown when polymorphic deserialization encounters a JSON-LD object which can be deserialized as multiple target Java
 * classes and we are unable to unambiguously decide which to use.
 */
public class AmbiguousTargetTypeException extends TargetTypeException {

    public AmbiguousTargetTypeException(String message) {
        super(message);
    }
}
