package cz.cvut.kbss.jsonld.serialization.serializer.context;

import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.serializer.SerializerUtils;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.NumberSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

/**
 * Serializes numeric values and builds a corresponding context item for them.
 */
public class ContextBuildingNumberSerializer extends NumberSerializer {

    @Override
    public JsonNode serialize(Number value, SerializationContext<Number> ctx) {
        if (ctx.getTerm() != null && ctx.getFieldName() != null) {
            final ObjectNode termDef =
                    SerializerUtils.createTypedTermDefinition(ctx.getFieldName(), ctx.getTerm(), getDatatype(value));
            ctx.registerTermMapping(ctx.getFieldName(), termDef);
            return JsonNodeFactory.createLiteralNode(ctx.getTerm(), value);
        } else {
            return super.serialize(value, ctx);
        }
    }
}
