package cz.cvut.kbss.jsonld.serialization.serializer.context;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.ObjectIdNode;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

public class ContextBuildingIdentifierSerializer implements ValueSerializer<String> {

    @Override
    public ObjectIdNode serialize(String value, SerializationContext<String> ctx) {
        if (ctx.getField() != null) {
            ctx.registerTermMapping(ctx.getFieldName(), JsonLd.ID);
            return JsonNodeFactory.createObjectIdNode(ctx.getTerm(), value);
        }
        return JsonNodeFactory.createObjectIdNode(ctx.getMappedTerm(JsonLd.ID).orElse(ctx.getTerm()), value);
    }
}
