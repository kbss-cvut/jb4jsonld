package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jsonld.JsonLd;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;

import static cz.cvut.kbss.jsonld.deserialization.datetime.OffsetDateTimeDeserializerTest.deserializationContext;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalDateTimeDeserializerTest {

    private final LocalDateTimeDeserializer sut = new LocalDateTimeDeserializer(new OffsetDateTimeDeserializer());

    @Test
    void deserializeSupportsDeserializationOfEpochMillis() {
        final LocalDateTime value = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        final Map<String, Object> input = Collections.singletonMap(JsonLd.VALUE, value.toInstant(ZoneOffset.UTC).toEpochMilli());

        final LocalDateTime result = sut.deserialize(input, deserializationContext(LocalDateTime.class));
        assertEquals(value, result);
    }

    @Test
    void deserializeSupportsDeserializationOfIsoLocalDatetimeString() {
        final LocalDateTime value = LocalDateTime.now();
        final Map<String, Object> input = Collections.singletonMap(JsonLd.VALUE, value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        final LocalDateTime result = sut.deserialize(input, deserializationContext(LocalDateTime.class));
        assertEquals(value, result);
    }
}
