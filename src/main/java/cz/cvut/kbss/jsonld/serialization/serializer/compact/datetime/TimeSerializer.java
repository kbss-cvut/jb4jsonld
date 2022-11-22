package cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime;

import cz.cvut.kbss.jopa.datatype.DateTimeUtil;
import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.LiteralValueSerializers;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * Serializes time values ({@link LocalTime} and {@link OffsetTime}) to ISO-based string.
 */
class TimeSerializer {

    static JsonNode serialize(OffsetTime value, SerializationContext<TemporalAccessor> ctx) {
        return LiteralValueSerializers.serializeValueWithType(ctx.getTerm(), DateTimeFormatter.ISO_OFFSET_TIME.format(value), XSD.TIME);
    }

    static JsonNode serialize(LocalTime value, SerializationContext<TemporalAccessor> ctx) {
        return serialize(value.atOffset(DateTimeUtil.SYSTEM_OFFSET), ctx);
    }
}
