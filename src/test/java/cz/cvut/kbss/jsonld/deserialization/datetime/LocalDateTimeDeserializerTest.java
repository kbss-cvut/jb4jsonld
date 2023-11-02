package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jsonld.JsonLd;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static cz.cvut.kbss.jsonld.deserialization.datetime.OffsetDateTimeDeserializerTest.deserializationContext;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalDateTimeDeserializerTest {

    private final LocalDateTimeDeserializer sut = new LocalDateTimeDeserializer(new OffsetDateTimeDeserializer());

    @Test
    void deserializeSupportsDeserializationOfEpochMillis() {
        final LocalDateTime value = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        final JsonObject input =
                Json.createObjectBuilder().add(JsonLd.VALUE, value.toInstant(ZoneOffset.UTC).toEpochMilli()).build();

        final LocalDateTime result = sut.deserialize(input, deserializationContext(LocalDateTime.class));
        assertEquals(value, result);
    }

    @Test
    void deserializeSupportsDeserializationOfIsoLocalDatetimeString() {
        final LocalDateTime value = LocalDateTime.now();
        final JsonObject input =
                Json.createObjectBuilder().add(JsonLd.VALUE, value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();

        final LocalDateTime result = sut.deserialize(input, deserializationContext(LocalDateTime.class));
        assertEquals(value, result);
    }
}
