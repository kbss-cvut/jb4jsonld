package cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime;

import cz.cvut.kbss.jopa.datatype.DateTimeUtil;
import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.SerializerUtils;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * Serializes time values ({@link LocalTime} and {@link OffsetTime}) to ISO-based string.
 */
public class TimeSerializer {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_TIME;

    public JsonNode serialize(OffsetTime value, SerializationContext<TemporalAccessor> ctx) {
        return SerializerUtils.createdTypedValueNode(ctx.getTerm(), FORMATTER.format(value), XSD.TIME);
    }

    public JsonNode serialize(LocalTime value, SerializationContext<TemporalAccessor> ctx) {
        return serialize(value.atOffset(DateTimeUtil.SYSTEM_OFFSET), ctx);
    }
}
