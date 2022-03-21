package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jopa.datatype.xsd.XsdDateMapper;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.ValueDeserializer;

import java.time.LocalDate;
import java.util.Map;

import static cz.cvut.kbss.jsonld.deserialization.datetime.OffsetDateTimeDeserializer.missingValueException;

/**
 * Deserializes values to {@link LocalDate}.
 * <p>
 * The values are expected to be String in the ISO date format.
 */
public class LocalDateDeserializer implements ValueDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(Map<?, ?> jsonNode, DeserializationContext<LocalDate> ctx) {
        final Object value = jsonNode.get(JsonLd.VALUE);
        if (value == null) {
            throw missingValueException(jsonNode);
        }
        return XsdDateMapper.map(value.toString());
    }
}
