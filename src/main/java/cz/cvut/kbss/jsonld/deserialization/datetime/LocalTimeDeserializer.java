package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.ValueDeserializer;

import java.time.LocalTime;
import java.time.OffsetTime;
import java.util.Map;

/**
 * Deserializes values to {@link LocalTime}.
 * <p>
 * The values are expected to be String in the ISO time format.
 */
public class LocalTimeDeserializer implements ValueDeserializer<LocalTime> {

    private final OffsetTimeDeserializer innerDeserializer;

    public LocalTimeDeserializer(OffsetTimeDeserializer innerDeserializer) {
        this.innerDeserializer = innerDeserializer;
    }

    @Override
    public LocalTime deserialize(Map<?, ?> jsonNode, DeserializationContext<LocalTime> ctx) {
        return innerDeserializer.deserialize(jsonNode,
                new DeserializationContext<>(OffsetTime.class, ctx.getClassResolver())).toLocalTime();
    }
}
