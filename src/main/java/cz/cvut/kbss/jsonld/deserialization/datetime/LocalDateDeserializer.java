package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jopa.datatype.xsd.XsdDateMapper;
import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.ValueDeserializer;
import cz.cvut.kbss.jsonld.deserialization.util.ValueUtils;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import jakarta.json.JsonValue;

import java.time.LocalDate;

/**
 * Deserializes values to {@link LocalDate}.
 * <p>
 * The values are expected to be String in the ISO date format.
 */
public class LocalDateDeserializer implements ValueDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonValue jsonNode, DeserializationContext<LocalDate> ctx) {
        final JsonValue value = ValueUtils.getValue(jsonNode);
        try {
            return XsdDateMapper.map(ValueUtils.stringValue(value));
        } catch (RuntimeException e) {
            throw new JsonLdDeserializationException("Unable to deserialize date value.", e);
        }
    }
}
