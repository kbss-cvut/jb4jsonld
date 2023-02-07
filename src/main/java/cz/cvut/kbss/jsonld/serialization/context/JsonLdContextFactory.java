package cz.cvut.kbss.jsonld.serialization.context;

/**
 * Factory for building JSON-LD context.
 */
public interface JsonLdContextFactory {

    /**
     * Creates a root JSON-LD context.
     *
     * @return New JSON-LD context
     */
    JsonLdContext createJsonLdContext();

    /**
     * Creates a JSON-LD context with the specified parent context.
     *
     * @param parent Parent JSON-LD context from which term mapping in inherited
     * @return New JSON-LD context
     */
    JsonLdContext createJsonLdContext(JsonLdContext parent);
}
