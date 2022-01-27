package cz.cvut.kbss.jsonld.serialization.datetime;

import cz.cvut.kbss.jopa.datatype.DateTimeUtil;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

class TimeSerializer {

    static JsonNode serialize(OffsetTime value, SerializationContext<TemporalAccessor> ctx) {
        return JsonNodeFactory.createLiteralNode(ctx.getAttributeId(), value.format(DateTimeFormatter.ISO_OFFSET_TIME));
    }

    static JsonNode serialize(LocalTime value, SerializationContext<TemporalAccessor> ctx) {
        return serialize(value.atOffset(DateTimeUtil.SYSTEM_OFFSET), ctx);
    }
}
