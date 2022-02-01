package cz.cvut.kbss.jsonld.serialization.datetime;

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.serialization.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

public class DateSerializer implements ValueSerializer<Date> {

    private final TemporalSerializer temporalSerializer;

    public DateSerializer(TemporalSerializer temporalSerializer) {
        this.temporalSerializer = temporalSerializer;
    }

    @Override
    public JsonNode serialize(Date value, SerializationContext<Date> ctx) {
        final Instant instant = value.toInstant();
        final SerializationContext<TemporalAccessor> newCtx = new SerializationContext<>(ctx.getAttributeId(), ctx.getField(), instant);
        return temporalSerializer.serialize(value.toInstant(), newCtx);
    }

    @Override
    public void configure(Configuration config) {
        temporalSerializer.configure(config);
    }
}
