package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.ValueDeserializer;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Deserializes values to {@link Instant}.
 */
public class InstantDeserializer implements ValueDeserializer<Instant> {

    private final OffsetDateTimeDeserializer innerDeserializer;

    public InstantDeserializer(OffsetDateTimeDeserializer innerDeserializer) {
        this.innerDeserializer = innerDeserializer;
    }

    @Override
    public Instant deserialize(Map<?, ?> jsonNode, DeserializationContext<Instant> ctx) {
        return innerDeserializer.deserialize(jsonNode,
                                             new DeserializationContext<>(OffsetDateTime.class, ctx.getClassResolver()))
                                .toInstant();
    }

    @Override
    public void configure(Configuration config) {
        innerDeserializer.configure(config);
    }
}
