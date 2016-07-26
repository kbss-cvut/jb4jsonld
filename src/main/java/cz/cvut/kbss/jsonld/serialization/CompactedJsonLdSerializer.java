package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.serialization.model.JsonNode;

/**
 * JSON-LD serializer outputting extended JSON.
 * <p>
 * This means that context info is not used and all attributes are mapped by their full URIs.
 */
public class CompactedJsonLdSerializer extends JsonLdSerializer {

    public CompactedJsonLdSerializer(JsonSerializer jsonSerializer) {
        super(jsonSerializer);
    }

    @Override
    JsonNode buildJsonTree(Object root) {
        final JsonLdTreeBuilder treeBuilder = new JsonLdTreeBuilder();
        traverser.addVisitor(treeBuilder);
        traverser.traverse(root);
        traverser.removeVisitor(treeBuilder);
        return treeBuilder.getTreeRoot();
    }
}
