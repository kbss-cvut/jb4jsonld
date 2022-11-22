package cz.cvut.kbss.jsonld.serialization.serializer.datetime;

import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.StringLiteralNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class TemporalAmountSerializerTest {

    private final TemporalAmountSerializer sut = new TemporalAmountSerializer();

    @Test
    void serializeReturnsIsoStringForPeriod() {
        final Period value =
                Period.of(Generator.randomCount(2, 5), Generator.randomCount(1, 12), Generator.randomCount(1, 28));
        final SerializationContext<TemporalAmount> ctx = Generator.serializationContext(value);
        final JsonNode result = sut.serialize(value, ctx);
        assertInstanceOf(StringLiteralNode.class, result);
        assertEquals(ctx.getTerm(), result.getName());
        assertEquals(value.toString(), ((StringLiteralNode) result).getValue());
    }

    @Test
    void serializeReturnsIsoStringForDuration() {
        final Duration value = Duration.ofSeconds(Generator.randomCount(10000));
        final SerializationContext<TemporalAmount> ctx = Generator.serializationContext(value);
        final JsonNode result = sut.serialize(value, ctx);
        assertInstanceOf(StringLiteralNode.class, result);
        assertEquals(ctx.getTerm(), result.getName());
        assertEquals(value.toString(), ((StringLiteralNode) result).getValue());
    }
}