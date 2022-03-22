package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.ValueDeserializer;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;

import java.time.Duration;
import java.util.Map;

import static cz.cvut.kbss.jsonld.deserialization.datetime.OffsetDateTimeDeserializer.getLiteralValue;

/**
 * Deserializes JSON values to {@link Duration}.
 * <p>
 * The value is expected to be an ISO 8601-formatted string.
 */
public class DurationDeserializer implements ValueDeserializer<Duration> {

    @Override
    public Duration deserialize(Map<?, ?> jsonNode, DeserializationContext<Duration> ctx) {
        final Object value = getLiteralValue(jsonNode);
        try {
            return Duration.parse(value.toString());
        } catch (RuntimeException e) {
            throw new JsonLdDeserializationException("Unable to deserialize duration.", e);
        }
    }
}
