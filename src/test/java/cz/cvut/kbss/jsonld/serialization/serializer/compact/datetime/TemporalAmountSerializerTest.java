package cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime;

import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class TemporalAmountSerializerTest {

    private final TemporalAmountSerializer sut = new TemporalAmountSerializer();

    @Test
    void serializeReturnsIsoStringForPeriodWithXsdDurationAsDatatype() {
        final Period value =
                Period.of(Generator.randomInt(2, 5), Generator.randomInt(1, 12), Generator.randomInt(1, 28));
        final SerializationContext<TemporalAmount> ctx = Generator.serializationContext(value);
        final JsonNode result = sut.serialize(value, ctx);
        assertInstanceOf(ObjectNode.class, result);
        assertEquals(ctx.getTerm(), result.getName());
        final ObjectNode node = (ObjectNode) result;
        assertThat(node.getItems(), hasItems(JsonNodeFactory.createLiteralNode(JsonLd.VALUE, value.toString()),
                JsonNodeFactory.createLiteralNode(JsonLd.TYPE, XSD.DURATION)));
    }

    @Test
    void serializeReturnsIsoStringForDuration() {
        final Duration value = Duration.ofSeconds(Generator.randomInt(10000));
        final SerializationContext<TemporalAmount> ctx = Generator.serializationContext(value);
        final JsonNode result = sut.serialize(value, ctx);
        assertInstanceOf(ObjectNode.class, result);
        assertEquals(ctx.getTerm(), result.getName());
        final ObjectNode node = (ObjectNode) result;
        assertThat(node.getItems(), hasItems(JsonNodeFactory.createLiteralNode(JsonLd.VALUE, value.toString()),
                JsonNodeFactory.createLiteralNode(JsonLd.TYPE, XSD.DURATION)));
    }
}