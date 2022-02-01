package cz.cvut.kbss.jsonld.serialization.datetime;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.NumericLiteralNode;
import cz.cvut.kbss.jsonld.serialization.model.StringLiteralNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DateSerializerTest {

    private final DateSerializer sut = new DateSerializer(new TemporalSerializer());

    @Test
    void serializeReturnsDateAsStringNodeWithIsoOffsetDateTimeAtUtcOffset() {
        final Date value = new Date();
        final SerializationContext<Date> ctx = new SerializationContext<>(Generator.generateUri().toString(), value);
        final JsonNode result = sut.serialize(value, ctx);
        assertInstanceOf(StringLiteralNode.class, result);
        assertEquals(ctx.getAttributeId(), result.getName());
        assertEquals(value.toInstant().atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), ((StringLiteralNode) result).getValue());
    }

    @Test
    void serializeAsMillisSinceEpochReturnsDateAsNumberOfMillisSinceEpochAtUtcOffset() {
        final Configuration config = new Configuration();
        config.set(ConfigParam.SERIALIZE_DATETIME_AS_MILLIS, Boolean.toString(true));
        sut.configure(config);
        final Date value = new Date();
        final SerializationContext<Date> ctx = new SerializationContext<>(Generator.generateUri().toString(), value);
        final JsonNode result = sut.serialize(value, ctx);
        assertInstanceOf(NumericLiteralNode.class, result);
        assertEquals(ctx.getAttributeId(), result.getName());
        assertEquals(value.getTime(), ((NumericLiteralNode<Long>) result).getValue());
    }
}
