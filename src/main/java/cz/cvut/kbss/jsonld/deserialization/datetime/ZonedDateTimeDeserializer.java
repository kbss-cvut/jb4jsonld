package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.ValueDeserializer;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Deserializes values to {@link ZonedDateTime}.
 */
public class ZonedDateTimeDeserializer implements ValueDeserializer<ZonedDateTime> {

    private final OffsetDateTimeDeserializer innerDeserializer;

    public ZonedDateTimeDeserializer(OffsetDateTimeDeserializer innerDeserializer) {
        this.innerDeserializer = innerDeserializer;
    }

    @Override
    public ZonedDateTime deserialize(Map<?, ?> jsonNode, DeserializationContext<ZonedDateTime> ctx) {
        return innerDeserializer.deserialize(jsonNode, new DeserializationContext<>(OffsetDateTime.class, ctx.getClassResolver()))
                .toZonedDateTime();
    }

    @Override
    public void configure(Configuration config) {
        innerDeserializer.configure(config);
    }
}
