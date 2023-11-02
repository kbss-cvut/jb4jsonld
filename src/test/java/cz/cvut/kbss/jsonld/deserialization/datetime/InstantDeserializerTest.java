package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jsonld.JsonLd;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static cz.cvut.kbss.jsonld.deserialization.datetime.OffsetDateTimeDeserializerTest.deserializationContext;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InstantDeserializerTest {

    private final InstantDeserializer sut = new InstantDeserializer(new OffsetDateTimeDeserializer());

    @Test
    void deserializeSupportsDeserializationOfEpochMillis() {
        final Instant value = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        final JsonObject input = Json.createObjectBuilder().add(JsonLd.VALUE, value.toEpochMilli()).build();

        final Instant result = sut.deserialize(input, deserializationContext(Instant.class));
        assertEquals(value, result);
    }

    @Test
    void deserializeSupportsDeserializationOfIsoUTCDatetimeString() {
        final Instant value = Instant.now();
        final JsonObject input = Json.createObjectBuilder().add(JsonLd.VALUE, value.toString()).build();

        final Instant result = sut.deserialize(input, deserializationContext(Instant.class));
        assertEquals(value, result);
    }
}