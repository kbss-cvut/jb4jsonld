package cz.cvut.kbss.jsonld.serialization.serializer.datetime;

import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.NumericLiteralNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DateSerializerTest {

    private final DateSerializer sut = new DateSerializer(new TemporalSerializer());

    @Test
    void serializeReturnsDateAsObjectNodeWithIsoOffsetDateTimeAtUtcOffsetValueAndDateTimeType() {
        final Date value = new Date();
        final SerializationContext<Date> ctx = Generator.serializationContext(value);
        final JsonNode result = sut.serialize(value, ctx);
        assertInstanceOf(ObjectNode.class, result);
        assertEquals(ctx.getTerm(), result.getName());
        assertThat(((ObjectNode) result).getItems(), hasItems(
                JsonNodeFactory.createLiteralNode(JsonLd.VALUE, value.toInstant().atOffset(ZoneOffset.UTC)
                                                                     .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                JsonNodeFactory.createLiteralNode(JsonLd.TYPE, XSD.DATETIME)
        ));
    }

    @Test
    void serializeAsMillisSinceEpochReturnsDateAsNumberOfMillisSinceEpochAtUtcOffset() {
        final Configuration config = new Configuration();
        config.set(ConfigParam.SERIALIZE_DATETIME_AS_MILLIS, Boolean.toString(true));
        sut.configure(config);
        final Date value = new Date();
        final SerializationContext<Date> ctx = Generator.serializationContext(value);
        final JsonNode result = sut.serialize(value, ctx);
        assertInstanceOf(NumericLiteralNode.class, result);
        assertEquals(ctx.getTerm(), result.getName());
        assertEquals(value.getTime(), ((NumericLiteralNode<Long>) result).getValue());
    }
}
