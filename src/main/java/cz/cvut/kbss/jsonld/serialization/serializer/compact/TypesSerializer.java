package cz.cvut.kbss.jsonld.serialization.serializer.compact;

import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.util.Set;

public class TypesSerializer implements ValueSerializer<Set<String>> {
    @Override
    public JsonNode serialize(Set<String> value, SerializationContext<Set<String>> ctx) {
        final CollectionNode<?> typesNode = JsonNodeFactory.createCollectionNode(ctx.getTerm(), value);
        value.forEach(type -> typesNode.addItem(JsonNodeFactory.createLiteralNode(type)));
        return typesNode;
    }
}
