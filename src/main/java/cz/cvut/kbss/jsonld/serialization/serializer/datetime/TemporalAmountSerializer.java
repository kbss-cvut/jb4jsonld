package cz.cvut.kbss.jsonld.serialization.serializer.datetime;

import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.temporal.TemporalAmount;

/**
 * Serializes {@link TemporalAmount} instances ({@link java.time.Duration}, {@link java.time.Period}) to JSON string in
 * the ISO 8601 format.
 */
public class TemporalAmountSerializer implements ValueSerializer<TemporalAmount> {

    @Override
    public JsonNode serialize(TemporalAmount value, SerializationContext<TemporalAmount> ctx) {
        return JsonNodeFactory.createLiteralNode(ctx.getAttributeId(), value.toString());
    }
}
