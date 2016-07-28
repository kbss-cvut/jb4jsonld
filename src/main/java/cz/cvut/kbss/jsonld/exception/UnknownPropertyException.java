package cz.cvut.kbss.jsonld.exception;

/**
 * Thrown when no JSON-LD serializable field matching a property IRI is found in a class.
 */
public class UnknownPropertyException extends JsonLdDeserializationException {

    public UnknownPropertyException(String message) {
        super(message);
    }

    public static UnknownPropertyException create(String property, Class<?> cls) {
        return new UnknownPropertyException(
                "No field matching property " + property + " was found in class " + cls + " or its ancestors.");
    }
}
