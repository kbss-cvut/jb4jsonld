package cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime;

import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.datetime.DateTimeSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

/**
 * Serializes datetime as the amount of milliseconds since epoch at UTC time zone.
 */
public class EpochBasedDateTimeSerializer extends DateTimeSerializer {

    @Override
    public JsonNode serialize(OffsetDateTime value, SerializationContext<TemporalAccessor> ctx) {
        return JsonNodeFactory.createLiteralNode(ctx.getTerm(), value.toInstant().toEpochMilli());
    }
}
