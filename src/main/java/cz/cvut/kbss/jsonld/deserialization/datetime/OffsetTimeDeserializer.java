package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jopa.datatype.xsd.XsdTimeMapper;
import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.ValueDeserializer;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import jakarta.json.JsonValue;

import java.time.OffsetTime;

import static cz.cvut.kbss.jsonld.deserialization.datetime.OffsetDateTimeDeserializer.getLiteralValue;

/**
 * Deserializes values to {@link OffsetTime}.
 * <p>
 * The values are expected to be String in the ISO time format.
 */
public class OffsetTimeDeserializer implements ValueDeserializer<OffsetTime> {

    @Override
    public OffsetTime deserialize(JsonValue jsonNode, DeserializationContext<OffsetTime> ctx) {
        final JsonValue value = getLiteralValue(jsonNode);
        try {
            return XsdTimeMapper.map(value.toString());
        } catch (RuntimeException e) {
            throw new JsonLdDeserializationException("Unable to deserialize time value.", e);
        }
    }
}
