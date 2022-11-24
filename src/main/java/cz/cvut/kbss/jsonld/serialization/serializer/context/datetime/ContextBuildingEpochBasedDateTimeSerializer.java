package cz.cvut.kbss.jsonld.serialization.serializer.context.datetime;

import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime.EpochBasedDateTimeSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

public class ContextBuildingEpochBasedDateTimeSerializer extends EpochBasedDateTimeSerializer {

    @Override
    public JsonNode serialize(OffsetDateTime value, SerializationContext<TemporalAccessor> ctx) {
        if (ctx.getTerm() != null) {
            ctx.registerTermMapping(ctx.getFieldName(), ctx.getTerm());
        }
        return super.serialize(value, ctx);
    }
}
