package cz.cvut.kbss.jsonld.serialization.serializer.context;

import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.serializer.SerializerUtils;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

public class ContextBuildingBooleanSerializer implements ValueSerializer<Boolean> {

    @Override
    public JsonNode serialize(Boolean value, SerializationContext<Boolean> ctx) {
        if (ctx.getTerm() != null && ctx.getFieldName() != null) {
            final ObjectNode termDef =
                    SerializerUtils.createTypedTermDefinition(ctx.getFieldName(), ctx.getTerm(), XSD.BOOLEAN);
            ctx.registerTermMapping(ctx.getFieldName(), termDef);
            return JsonNodeFactory.createBooleanLiteralNode(ctx.getTerm(), value);
        } else {
            return SerializerUtils.createdTypedValueNode(ctx.getTerm(), value, XSD.BOOLEAN);
        }
    }
}
