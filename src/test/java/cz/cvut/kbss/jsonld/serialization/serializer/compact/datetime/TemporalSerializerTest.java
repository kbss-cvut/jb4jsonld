package cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime;

import cz.cvut.kbss.jopa.datatype.DateTimeUtil;
import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.exception.UnsupportedTemporalTypeException;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.NumericLiteralNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.chrono.JapaneseEra;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.*;

class TemporalSerializerTest {

    private final TemporalSerializer sut = new TemporalSerializer();

    @Test
    void serializeReturnsLocalDateAsStringNodeWithIsoDate() {
        final LocalDate value = LocalDate.now();
        serializeAndVerifyStringResult(value, value.format(DateTimeFormatter.ISO_DATE), XSD.DATE);
    }

    @Test
    void serializeReturnsOffsetTimeAsStringNodeWithIsoOffsetTime() {
        final OffsetTime value = OffsetTime.now();
        serializeAndVerifyStringResult(value, value.format(DateTimeFormatter.ISO_OFFSET_TIME), XSD.TIME);
    }

    private void serializeAndVerifyStringResult(TemporalAccessor value, String expected, String datatype) {
        final SerializationContext<TemporalAccessor> ctx = Generator.serializationContext(value);
        final JsonNode result = sut.serialize(value, ctx);
        assertInstanceOf(ObjectNode.class, result);
        assertEquals(ctx.getTerm(), result.getName());
        final ObjectNode node = (ObjectNode) result;
        assertThat(node.getItems(), hasItems(JsonNodeFactory.createLiteralNode(JsonLd.VALUE, expected),
                                             JsonNodeFactory.createLiteralNode(JsonLd.TYPE, datatype)));
    }

    @Test
    void serializeReturnsLocalTimeAsStringNodeWithIsoOffsetTimeAttSystemOffset() {
        final LocalTime value = LocalTime.now();
        serializeAndVerifyStringResult(value, value.atOffset(DateTimeUtil.SYSTEM_OFFSET)
                                                   .format(DateTimeFormatter.ISO_OFFSET_TIME), XSD.TIME);
    }

    @Test
    void serializeReturnsOffsetDateTimeAsStringNodeWithIsoOffsetDateTime() {
        final OffsetDateTime value = OffsetDateTime.now();
        serializeAndVerifyStringResult(value, value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), XSD.DATETIME);
    }

    @Test
    void serializeReturnsLocalDateTimeAsStringNodeWithIsoOffsetDateTimeAtSystemOffset() {
        final LocalDateTime value = LocalDateTime.now();
        serializeAndVerifyStringResult(value, value.atOffset(DateTimeUtil.SYSTEM_OFFSET)
                                                   .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), XSD.DATETIME);
    }

    @Test
    void serializeReturnsInstantAsStringNodeWithIsoOffsetDateTimeAtUtcOffset() {
        final Instant value = Instant.now();
        serializeAndVerifyStringResult(value, value.atOffset(ZoneOffset.UTC)
                                                   .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), XSD.DATETIME);
    }

    @Test
    void serializeReturnsZonedDateTimeAsStringNodeWithIsoOffsetDateTimeAtZoneOffset() {
        final ZonedDateTime value = ZonedDateTime.now();
        serializeAndVerifyStringResult(value, value.toOffsetDateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                                       XSD.DATETIME);
    }

    @Test
    void serializeThrowsUnsupportedTemporalTypeExceptionForUnsupportedTemporalAccessorType() {
        final JapaneseEra value = JapaneseEra.TAISHO;
        final SerializationContext<TemporalAccessor> ctx = Generator.serializationContext(value);
        assertThrows(UnsupportedTemporalTypeException.class, () -> sut.serialize(value, ctx));
    }

    @Test
    void serializeAsMillisReturnsOffsetDateTimeAsTimeInMillisSinceEpochAtUtc() {
        serializeDatetimeAsMillisSinceEpoch();
        final OffsetDateTime value = OffsetDateTime.now();
        serializeAndVerifyMillisResult(value, value.toInstant().toEpochMilli());
    }

    private void serializeDatetimeAsMillisSinceEpoch() {
        final Configuration config = new Configuration();
        config.set(ConfigParam.SERIALIZE_DATETIME_AS_MILLIS, Boolean.toString(true));
        sut.configure(config);
    }

    private void serializeAndVerifyMillisResult(TemporalAccessor value, long expected) {
        final SerializationContext<TemporalAccessor> ctx = Generator.serializationContext(value);
        final JsonNode result = sut.serialize(value, ctx);
        assertInstanceOf(NumericLiteralNode.class, result);
        assertEquals(ctx.getTerm(), result.getName());
        assertEquals(expected, ((NumericLiteralNode<Long>) result).getValue());
    }

    @Test
    void serializeAsMillisReturnsLocalDateTimeAsTimeInMillisSinceEpochAtUtc() {
        serializeDatetimeAsMillisSinceEpoch();
        final LocalDateTime value = LocalDateTime.now();
        serializeAndVerifyMillisResult(value, value.atOffset(DateTimeUtil.SYSTEM_OFFSET).toInstant().toEpochMilli());
    }

    @Test
    void serializeAsMillisReturnsZonedDateTimeAsTimeInMillisSinceEpochAtUtc() {
        serializeDatetimeAsMillisSinceEpoch();
        final ZonedDateTime value = ZonedDateTime.now();
        serializeAndVerifyMillisResult(value, value.toInstant().toEpochMilli());
    }

    @Test
    void serializeLocalDateTimeUsesConfiguredDateTimeFormat() {
        final String pattern = "dd/MM/yyyy hh:mm:ss";
        final Configuration config = new Configuration();
        config.set(ConfigParam.DATE_TIME_FORMAT, pattern);
        final LocalDateTime value = LocalDateTime.now();
        sut.configure(config);
        final SerializationContext<TemporalAccessor> ctx = Generator.serializationContext(value);
        final JsonNode result = sut.serialize(value, ctx);
        assertInstanceOf(ObjectNode.class, result);
        assertThat(((ObjectNode) result).getItems(), hasItem(JsonNodeFactory.createLiteralNode(JsonLd.VALUE,
                                                                                               value.format(
                                                                                                       DateTimeFormatter.ofPattern(
                                                                                                               pattern)))));
    }
}
