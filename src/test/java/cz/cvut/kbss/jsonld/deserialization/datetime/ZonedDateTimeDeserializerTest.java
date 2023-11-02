package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jsonld.JsonLd;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static cz.cvut.kbss.jsonld.deserialization.datetime.OffsetDateTimeDeserializerTest.deserializationContext;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ZonedDateTimeDeserializerTest {

    private final ZonedDateTimeDeserializer sut = new ZonedDateTimeDeserializer(new OffsetDateTimeDeserializer());

    @Test
    void deserializeSupportsDeserializationOfEpochMillis() {
        final ZonedDateTime value = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        final JsonObject input = Json.createObjectBuilder().add(JsonLd.VALUE, value.toInstant().toEpochMilli()).build();

        final ZonedDateTime result = sut.deserialize(input, deserializationContext(ZonedDateTime.class));
        assertEquals(value.toInstant(), result.toInstant());
    }

    @Test
    void deserializeSupportsDeserializationOfIsoOffsetDatetimeString() {
        final ZonedDateTime value = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        final JsonObject input =
                Json.createObjectBuilder().add(JsonLd.VALUE, value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                    .build();

        final ZonedDateTime result = sut.deserialize(input, deserializationContext(ZonedDateTime.class));
        assertEquals(value.toInstant(), result.toInstant());
    }

}