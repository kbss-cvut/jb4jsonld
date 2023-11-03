package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.ValueDeserializer;
import cz.cvut.kbss.jsonld.deserialization.util.ValueUtils;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import jakarta.json.JsonValue;

import java.time.Period;

/**
 * Deserializes JSON values to {@link Period}.
 * <p>
 * The value is expected to be an ISO 8601-formatted string.
 */
public class PeriodDeserializer implements ValueDeserializer<Period> {

    @Override
    public Period deserialize(JsonValue jsonNode, DeserializationContext<Period> ctx) {
        final JsonValue value = ValueUtils.getValue(jsonNode);
        try {
            return Period.parse(ValueUtils.stringValue(value));
        } catch (RuntimeException e) {
            throw new JsonLdDeserializationException("Unable to deserialize duration.", e);
        }
    }
}
