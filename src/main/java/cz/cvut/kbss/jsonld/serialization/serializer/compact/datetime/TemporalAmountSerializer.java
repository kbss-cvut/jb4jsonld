package cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime;

import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.LiteralValueSerializers;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.temporal.TemporalAmount;

/**
 * Serializes {@link TemporalAmount} instances ({@link java.time.Duration}, {@link java.time.Period}) to JSON object
 * with {@literal xsd:duration} datatype and value in the ISO 8601 format.
 */
public class TemporalAmountSerializer implements ValueSerializer<TemporalAmount> {

    @Override
    public JsonNode serialize(TemporalAmount value, SerializationContext<TemporalAmount> ctx) {
        return LiteralValueSerializers.serializeValueWithType(ctx.getTerm(), value.toString(), XSD.DURATION);
    }
}
