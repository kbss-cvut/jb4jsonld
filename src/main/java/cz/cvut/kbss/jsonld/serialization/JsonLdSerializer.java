package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.traversal.ObjectGraphTraverser;

import java.util.Objects;

/**
 * Base class for all JSON-LD serializers.
 * <p>
 * The serializers will mostly differ in the form of the generated JSON. E.g. the output can be expanded, using contexts
 * etc.
 */
public abstract class JsonLdSerializer {

    final ObjectGraphTraverser traverser = new ObjectGraphTraverser();

    final JsonSerializer jsonSerializer;

    protected JsonLdSerializer(JsonSerializer jsonSerializer) {
        this.jsonSerializer = Objects.requireNonNull(jsonSerializer);
    }

    /**
     * Serializes object graph with the specified root.
     * <p>
     * The serialization builds a JSON-LD tree model and then writes it using a {@link JsonSerializer}, which was
     * passed to this instance in constructor.
     *
     * @param root Object graph root
     */
    public void serialize(Object root) {
        Objects.requireNonNull(root);
        final JsonNode jsonRoot = buildJsonTree(root);
        jsonRoot.write(jsonSerializer);
    }

    /**
     * Builds the JSON-LD tree model.
     *
     * @param root Object graph root
     * @return {@link JsonNode} corresponding to the JSON-LD's tree root
     */
    abstract JsonNode buildJsonTree(Object root);
}
