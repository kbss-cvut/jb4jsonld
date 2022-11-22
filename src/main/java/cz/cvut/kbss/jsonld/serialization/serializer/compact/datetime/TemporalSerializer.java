package cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.exception.UnsupportedTemporalTypeException;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime.EpochBasedDateTimeSerializer;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime.IsoDateTimeSerializer;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime.LocalDateSerializer;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime.TimeSerializer;
import cz.cvut.kbss.jsonld.serialization.serializer.datetime.DateTimeSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.*;
import java.time.temporal.TemporalAccessor;

/**
 * Serializes Java 8 date/time values represented by the {@link TemporalAccessor} interface.
 */
public class TemporalSerializer implements ValueSerializer<TemporalAccessor> {

    private DateTimeSerializer dateTimeSerializer = new IsoDateTimeSerializer();

    @Override
    public JsonNode serialize(TemporalAccessor value, SerializationContext<TemporalAccessor> ctx) {
        if (value instanceof LocalDate) {
            return LocalDateSerializer.serialize((LocalDate) value, ctx);
        } else if (value instanceof OffsetTime) {
            return TimeSerializer.serialize((OffsetTime) value, ctx);
        } else if (value instanceof LocalTime) {
            return TimeSerializer.serialize((LocalTime) value, ctx);
        } else if (value instanceof OffsetDateTime) {
            return dateTimeSerializer.serialize((OffsetDateTime) value, ctx);
        } else if (value instanceof LocalDateTime) {
            return dateTimeSerializer.serialize((LocalDateTime) value, ctx);
        } else if (value instanceof Instant) {
            return dateTimeSerializer.serialize((Instant) value, ctx);
        } else if (value instanceof ZonedDateTime) {
            return dateTimeSerializer.serialize(((ZonedDateTime) value).toOffsetDateTime(), ctx);
        }
        throw new UnsupportedTemporalTypeException("Temporal type " + value.getClass() + " serialization is not supported.");
    }

    @Override
    public void configure(Configuration config) {
        assert config != null;
        this.dateTimeSerializer = config.is(ConfigParam.SERIALIZE_DATETIME_AS_MILLIS) ?
                new EpochBasedDateTimeSerializer() : new IsoDateTimeSerializer();
        dateTimeSerializer.configure(config);
    }
}
