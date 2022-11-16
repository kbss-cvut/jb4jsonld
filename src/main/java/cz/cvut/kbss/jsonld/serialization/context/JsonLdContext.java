package cz.cvut.kbss.jsonld.serialization.context;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.exception.AmbiguousTermMappingException;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the {@literal @context} JSON-LD attribute.
 * <p>
 * Note that this represents the JSON-LD 1.0 context, so no <a href="https://www.w3.org/TR/json-ld/#scoped-contexts">context
 * scoping</a> is supported and attempting to register a different mapping for an already existing term will result in
 * an {@link AmbiguousTermMappingException}.
 */
public class JsonLdContext {

    private final Map<String, JsonNode> mapping = new HashMap<>();

    /**
     * Registers the specified term mapping in this context.
     * <p>
     * Typically, the {@code term} would be Java attribute (field) name and {@code iri} would be the IRI to which this
     * field is mapped.
     *
     * @param term Mapped term
     * @param iri  IRI to which the term is mapped
     * @throws AmbiguousTermMappingException When term is already mapped to a different IRI
     */
    public void registerTermMapping(String term, String iri) {
        Objects.requireNonNull(term);
        Objects.requireNonNull(iri);
        final JsonNode value = JsonNodeFactory.createLiteralNode(term, iri);
        verifyMappingUnique(term, value);
        mapping.put(term, value);
    }

    private void verifyMappingUnique(String term, JsonNode value) {
        if (mapping.containsKey(term) && !Objects.equals(mapping.get(term), value)) {
            throw new AmbiguousTermMappingException("Context already contains mapping for term '" + term + "'.");
        }
    }

    /**
     * Registers the specified term mapping in this context.
     * <p>
     * Compared to {@link #registerTermMapping(String, String)}, this method allows registering more complex mapping
     * like language container.
     *
     * @param term Mapped term
     * @param mappedNode Object node to which the term is mapped
     * @throws AmbiguousTermMappingException When term is already mapped to a different IRI
     */
    public void registerTermMapping(String term, JsonNode mappedNode) {
        Objects.requireNonNull(term);
        Objects.requireNonNull(mappedNode);
        verifyMappingUnique(term, mappedNode);
        mapping.put(term, mappedNode);
    }

    Map<String, JsonNode> getMapping() {
        return Collections.unmodifiableMap(mapping);
    }

    /**
     * Returns a {@link JsonNode} representing this context.
     *
     * The result can thus be added to serialization output.
     * @return {@code JsonNode} with registered mappings
     */
    public JsonNode getContextNode() {
        final ObjectNode node = new ObjectNode(JsonLd.CONTEXT);
        mapping.values().forEach(node::addItem);
        return node;
    }
}
