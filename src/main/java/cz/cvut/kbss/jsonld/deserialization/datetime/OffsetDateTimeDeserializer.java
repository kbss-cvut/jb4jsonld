package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.ValueDeserializer;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import jakarta.json.JsonNumber;
import jakarta.json.JsonValue;

import java.time.OffsetDateTime;

/**
 * Deserializes values to {@link OffsetDateTime}.
 * <p>
 * If the value is a number, it is taken as the number of milliseconds since the Unix Epoch. Otherwise, it is parsed as a
 * string.
 * <p>
 * If a datetime pattern is configured ({@link cz.cvut.kbss.jsonld.ConfigParam#DATE_TIME_FORMAT}), it is used to parse
 * the value. Otherwise, the default ISO-based pattern is used.
 */
public class OffsetDateTimeDeserializer implements ValueDeserializer<OffsetDateTime> {

    private final StringBasedDateTimeResolver stringResolver = new StringBasedDateTimeResolver();

    private final EpochBasedDateTimeResolver epochResolver = new EpochBasedDateTimeResolver();

    @Override
    public OffsetDateTime deserialize(JsonValue jsonNode, DeserializationContext<OffsetDateTime> ctx) {
        final JsonValue value = getLiteralValue(jsonNode);
        try {
            return value.getValueType() == JsonValue.ValueType.NUMBER ? epochResolver.resolve((JsonNumber) value) :
                   stringResolver.resolve(value.toString());
        } catch (RuntimeException e) {
            throw new JsonLdDeserializationException("Unable to deserialize datetime value.", e);
        }
    }

    static JsonValue getLiteralValue(JsonValue jsonNode) {
        if (jsonNode.getValueType() != JsonValue.ValueType.OBJECT || !jsonNode.asJsonObject().containsKey(JsonLd.VALUE)) {
            throw new JsonLdDeserializationException("Cannot deserialize node " + jsonNode + "as literal. " +
                                                             "It is missing attribute '" + JsonLd.VALUE + "'.");
        }
        return jsonNode.asJsonObject().get(JsonLd.VALUE);
    }

    @Override
    public void configure(Configuration config) {
        stringResolver.configure(config);
    }
}
