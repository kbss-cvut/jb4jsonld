package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.ValueDeserializer;
import cz.cvut.kbss.jsonld.deserialization.util.ValueUtils;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import jakarta.json.JsonValue;

import java.time.Duration;

/**
 * Deserializes JSON values to {@link Duration}.
 * <p>
 * The value is expected to be an ISO 8601-formatted string.
 */
public class DurationDeserializer implements ValueDeserializer<Duration> {

    @Override
    public Duration deserialize(JsonValue jsonNode, DeserializationContext<Duration> ctx) {
        final JsonValue value = ValueUtils.getValue(jsonNode);
        try {
            return Duration.parse(ValueUtils.stringValue(value));
        } catch (RuntimeException e) {
            throw new JsonLdDeserializationException("Unable to deserialize duration.", e);
        }
    }
}
