package cz.cvut.kbss.jsonld.serialization.datetime;

import cz.cvut.kbss.jopa.datatype.DateTimeUtil;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.exception.UnsupportedTemporalTypeException;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.NumericLiteralNode;
import cz.cvut.kbss.jsonld.serialization.model.StringLiteralNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.chrono.JapaneseEra;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import static org.junit.jupiter.api.Assertions.*;

class TemporalSerializerTest {

    private final TemporalSerializer sut = new TemporalSerializer();

    @Test
    void serializeReturnsLocalDateAsStringNodeWithIsoDate() {
        final LocalDate value = LocalDate.now();
        serializeAndVerifyStringResult(value, value.format(DateTimeFormatter.ISO_DATE));
    }

    @Test
    void serializeReturnsOffsetTimeAsStringNodeWithIsoOffsetTime() {
        final OffsetTime value = OffsetTime.now();
        serializeAndVerifyStringResult(value, value.format(DateTimeFormatter.ISO_OFFSET_TIME));
    }

    private void serializeAndVerifyStringResult(TemporalAccessor value, String expected) {
        final SerializationContext<TemporalAccessor> ctx = new SerializationContext<>(Generator.generateUri()
                .toString(), value);
        final JsonNode result = sut.serialize(value, ctx);
        assertInstanceOf(StringLiteralNode.class, result);
        assertEquals(ctx.getAttributeId(), result.getName());
        assertEquals(expected, ((StringLiteralNode) result).getValue());
    }

    @Test
    void serializeReturnsLocalTimeAsStringNodeWithIsoOffsetTimeAttSystemOffset() {
        final LocalTime value = LocalTime.now();
        serializeAndVerifyStringResult(value, value.atOffset(DateTimeUtil.SYSTEM_OFFSET)
                .format(DateTimeFormatter.ISO_OFFSET_TIME));
    }

    @Test
    void serializeReturnsOffsetDateTimeAsStringNodeWithIsoOffsetDateTime() {
        final OffsetDateTime value = OffsetDateTime.now();
        serializeAndVerifyStringResult(value, value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    @Test
    void serializeReturnsLocalDateTimeAsStringNodeWithIsoOffsetDateTimeAtSystemOffset() {
        final LocalDateTime value = LocalDateTime.now();
        serializeAndVerifyStringResult(value, value.atOffset(DateTimeUtil.SYSTEM_OFFSET)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    @Test
    void serializeReturnsInstantAsStringNodeWithIsoOffsetDateTimeAtUtcOffset() {
        final Instant value = Instant.now();
        serializeAndVerifyStringResult(value, value.atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    @Test
    void serializeReturnsZonedDateTimeAsStringNodeWithIsoOffsetDateTimeAtZoneOffset() {
        final ZonedDateTime value = ZonedDateTime.now();
        serializeAndVerifyStringResult(value, value.toOffsetDateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    @Test
    void serializeThrowsUnsupportedTemporalTypeExceptionForUnsupportedTemporalAccessorType() {
        final JapaneseEra value = JapaneseEra.TAISHO;
        final SerializationContext<TemporalAccessor> ctx = new SerializationContext<>(Generator.generateUri()
                .toString(), value);
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
        sut.applyConfiguration(config);
    }

    private void serializeAndVerifyMillisResult(TemporalAccessor value, long expected) {
        final SerializationContext<TemporalAccessor> ctx = new SerializationContext<>(Generator.generateUri()
                .toString(), value);
        final JsonNode result = sut.serialize(value, ctx);
        assertInstanceOf(NumericLiteralNode.class, result);
        assertEquals(ctx.getAttributeId(), result.getName());
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
}
