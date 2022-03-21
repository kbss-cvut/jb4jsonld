package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.ValueDeserializer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Deserializes values to {@link LocalDateTime}.
 */
public class LocalDateTimeDeserializer implements ValueDeserializer<LocalDateTime> {

    private final OffsetDateTimeDeserializer innerDeserializer;

    public LocalDateTimeDeserializer(OffsetDateTimeDeserializer innerDeserializer) {
        this.innerDeserializer = innerDeserializer;
    }

    @Override
    public LocalDateTime deserialize(Map<?, ?> jsonNode, DeserializationContext<LocalDateTime> ctx) {
        return innerDeserializer.deserialize(jsonNode, new DeserializationContext<>(OffsetDateTime.class, ctx.getClassResolver())).
                toLocalDateTime();
    }

    @Override
    public void configure(Configuration config) {
        innerDeserializer.configure(config);
    }
}
