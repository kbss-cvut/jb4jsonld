package cz.cvut.kbss.jsonld.serialization.serializer.context;

import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.ObjectPropertyValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.ObjectGraphTraverser;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

public class ContextBuildingObjectPropertyValueSerializer extends ObjectPropertyValueSerializer {

    public ContextBuildingObjectPropertyValueSerializer(ObjectGraphTraverser graphTraverser) {
        super(graphTraverser);
    }

    @Override
    public JsonNode serialize(Object value, SerializationContext ctx) {
        if (ctx.getTerm() != null) {
            ctx.registerTermMapping(ctx.getFieldName(), ctx.getTerm());
        }
        return super.serialize(value, ctx);
    }
}
