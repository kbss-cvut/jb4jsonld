package cz.cvut.kbss.jsonld.serialization.context;

import cz.cvut.kbss.jsonld.serialization.model.JsonNode;

import java.util.Optional;

/**
 * JSON-LD that does nothing.
 * <p>
 * It can be used in serialization that does not create a JSON-LD context (e.g., expanded, context-less compacted).
 */
public class DummyJsonLdContext implements JsonLdContext {

    public static final DummyJsonLdContext INSTANCE = new DummyJsonLdContext();

    @Override
    public void registerTermMapping(String term, String iri) {
        // Do nothing
    }

    @Override
    public void registerTermMapping(String term, JsonNode mappedNode) {
        // Do nothing
    }

    @Override
    public Optional<JsonNode> getTermMapping(String term) {
        return Optional.empty();
    }

    @Override
    public boolean hasTermMapping(String term) {
        return false;
    }
}
