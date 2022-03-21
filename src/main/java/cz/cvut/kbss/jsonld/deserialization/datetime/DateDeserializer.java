package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.ValueDeserializer;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Map;

/**
 * Deserializes values to {@link Date}.
 */
public class DateDeserializer implements ValueDeserializer<Date> {

    private final OffsetDateTimeDeserializer innerDeserializer;

    public DateDeserializer(OffsetDateTimeDeserializer innerDeserializer) {
        this.innerDeserializer = innerDeserializer;
    }

    @Override
    public Date deserialize(Map<?, ?> jsonNode, DeserializationContext<Date> ctx) {
        return Date.from(innerDeserializer.deserialize(jsonNode,
                new DeserializationContext<>(OffsetDateTime.class, ctx.getClassResolver())).toInstant());
    }

    @Override
    public void configure(Configuration config) {
        innerDeserializer.configure(config);
    }
}
