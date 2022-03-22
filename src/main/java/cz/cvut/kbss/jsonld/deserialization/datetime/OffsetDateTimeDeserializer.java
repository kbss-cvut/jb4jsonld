package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.ValueDeserializer;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Deserializes values to {@link OffsetDateTime}.
 * <p>
 * If the value a number, it is taken as the number of milliseconds since the Unix Epoch. Otherwise, it is parsed as a
 * string.
 * <p>
 * If a datetime pattern is configured ({@link cz.cvut.kbss.jsonld.ConfigParam#DATE_TIME_FORMAT}), it is used to parse
 * the value. Otherwise, the default ISO-based pattern is used.
 */
public class OffsetDateTimeDeserializer implements ValueDeserializer<OffsetDateTime> {

    private final StringBasedDateTimeResolver stringResolver = new StringBasedDateTimeResolver();

    private final EpochBasedDateTimeResolver epochResolver = new EpochBasedDateTimeResolver();

    @Override
    public OffsetDateTime deserialize(Map<?, ?> jsonNode, DeserializationContext<OffsetDateTime> ctx) {
        final Object value = getLiteralValue(jsonNode);
        try {
            return value instanceof Long ? epochResolver.resolve((Long) value) :
                   stringResolver.resolve(value.toString());
        } catch (RuntimeException e) {
            throw new JsonLdDeserializationException("Unable to deserialize datetime value.", e);
        }
    }

    static Object getLiteralValue(Map<?, ?> jsonNode) {
        final Object value = jsonNode.get(JsonLd.VALUE);
        if (value == null) {
            throw new JsonLdDeserializationException("Cannot deserialize node " + jsonNode + "as literal. " +
                                                             "It is missing attribute '" + JsonLd.VALUE + "'.");
        }
        return value;
    }

    @Override
    public void configure(Configuration config) {
        stringResolver.configure(config);
    }
}
