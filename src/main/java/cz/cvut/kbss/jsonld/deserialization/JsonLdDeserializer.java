package cz.cvut.kbss.jsonld.deserialization;

/**
 * Takes a pre-processed JSON-LD structure and deserializes it.
 */
public abstract class JsonLdDeserializer {

    /**
     * Deserializes the specified JSON-LD data.
     *
     * @param jsonLd      JSON-LD structure
     * @param resultClass Type of the result instance
     * @return Deserialized Java instance
     */
    public abstract <T> T deserialize(Object jsonLd, Class<T> resultClass);

    /**
     * Creates deserializer for expanded JSON-LD.
     *
     * @return New deserializer
     */
    public static JsonLdDeserializer createExpandedDeserializer() {
        return new ExpandedJsonLdDeserializer();
    }
}
