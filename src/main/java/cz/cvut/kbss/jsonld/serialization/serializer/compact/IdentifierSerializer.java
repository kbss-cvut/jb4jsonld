package cz.cvut.kbss.jsonld.serialization.serializer.compact;

import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

public class IdentifierSerializer implements ValueSerializer<String> {

    @Override
    public JsonNode serialize(String value, SerializationContext<String> ctx) {
        return JsonNodeFactory.createObjectIdNode(ctx.getTerm(), value);
    }
}
