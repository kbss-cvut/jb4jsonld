package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jopa.datatype.xsd.XsdTimeMapper;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.ValueDeserializer;

import java.time.OffsetTime;
import java.util.Map;

import static cz.cvut.kbss.jsonld.deserialization.datetime.OffsetDateTimeDeserializer.missingValueException;

/**
 * Deserializes values to {@link OffsetTime}.
 * <p>
 * The values are expected to be String in the ISO time format.
 */
public class OffsetTimeDeserializer implements ValueDeserializer<OffsetTime> {

    @Override
    public OffsetTime deserialize(Map<?, ?> jsonNode, DeserializationContext<OffsetTime> ctx) {
        final Object value = jsonNode.get(JsonLd.VALUE);
        if (value == null) {
            throw missingValueException(jsonNode);
        }
        return XsdTimeMapper.map(value.toString());
    }
}
