package cz.cvut.kbss.jsonld.serialization.serializer.context;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.util.Set;

public class ContextBuildingTypesSerializer implements ValueSerializer<Set<String>> {

    @Override
    public JsonNode serialize(Set<String> value, SerializationContext<Set<String>> ctx) {
        final CollectionNode<?> typesNode;
        if (ctx.getField() != null) {
            ctx.registerTermMapping(ctx.getFieldName(), JsonLd.TYPE);
            typesNode = JsonNodeFactory.createCollectionNode(ctx.getTerm(), value);
        } else {
            typesNode = JsonNodeFactory.createCollectionNode(JsonLd.TYPE, value);
        }
        value.forEach(type -> typesNode.addItem(JsonNodeFactory.createLiteralNode(type)));
        return typesNode;
    }
}
