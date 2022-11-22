package cz.cvut.kbss.jsonld.serialization.serializer.datetime;

import cz.cvut.kbss.jopa.datatype.DateTimeUtil;
import cz.cvut.kbss.jsonld.common.Configurable;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

public abstract class DateTimeSerializer implements Configurable {

    public abstract JsonNode serialize(OffsetDateTime value, SerializationContext<TemporalAccessor> ctx);

    public JsonNode serialize(LocalDateTime value, SerializationContext<TemporalAccessor> ctx) {
        return serialize(DateTimeUtil.toDateTime(value), ctx);
    }

    public JsonNode serialize(Instant value, SerializationContext<TemporalAccessor> ctx) {
        return serialize(DateTimeUtil.toDateTime(value), ctx);
    }
}
