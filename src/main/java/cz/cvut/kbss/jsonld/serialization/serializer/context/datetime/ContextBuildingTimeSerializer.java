package cz.cvut.kbss.jsonld.serialization.serializer.context.datetime;

import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.serializer.SerializerUtils;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.OffsetTime;
import java.time.temporal.TemporalAccessor;

public class ContextBuildingTimeSerializer extends cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime.TimeSerializer {

    @Override
    public JsonNode serialize(OffsetTime value, SerializationContext<TemporalAccessor> ctx) {
        if (ctx.getTerm() != null) {
            final ObjectNode termDef =
                    SerializerUtils.createTypedTermDefinition(ctx.getFieldName(), ctx.getTerm(), XSD.TIME);
            ctx.registerTermMapping(ctx.getFieldName(), termDef);
        }
        return JsonNodeFactory.createLiteralNode(ctx.getTerm(), FORMATTER.format(value));
    }
}
