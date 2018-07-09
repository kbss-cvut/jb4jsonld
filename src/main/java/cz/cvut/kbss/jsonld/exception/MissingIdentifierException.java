package cz.cvut.kbss.jsonld.exception;

/**
 * Indicates that the serializer encountered an instance without an identifier and it was configured to require identifier presence.
 *
 * @see cz.cvut.kbss.jsonld.ConfigParam#REQUIRE_ID
 */
public class MissingIdentifierException extends JsonLdSerializationException {

    public MissingIdentifierException(String message) {
        super(message);
    }

    public static MissingIdentifierException create(Object instance) {
        return new MissingIdentifierException(
                "Instance " + instance + " is missing an identifier. Either it has no @Id field or its value is null.");
    }
}
