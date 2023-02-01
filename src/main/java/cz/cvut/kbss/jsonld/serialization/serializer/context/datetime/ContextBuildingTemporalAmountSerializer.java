package cz.cvut.kbss.jsonld.serialization.serializer.context.datetime;

import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.serializer.SerializerUtils;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.temporal.TemporalAmount;

public class ContextBuildingTemporalAmountSerializer implements ValueSerializer<TemporalAmount> {

    @Override
    public JsonNode serialize(TemporalAmount value, SerializationContext<TemporalAmount> ctx) {
        if (ctx.getTerm() != null) {
            final ObjectNode termDef = SerializerUtils.createTypedTermDefinition(ctx.getFieldName(), ctx.getTerm(),
                                                                                 XSD.DURATION);
            ctx.registerTermMapping(ctx.getFieldName(), termDef);
        }
        return JsonNodeFactory.createLiteralNode(ctx.getTerm(), value.toString());
    }
}
