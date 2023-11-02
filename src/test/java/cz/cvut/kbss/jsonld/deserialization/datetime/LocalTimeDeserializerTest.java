package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jsonld.JsonLd;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static cz.cvut.kbss.jsonld.deserialization.datetime.OffsetDateTimeDeserializerTest.deserializationContext;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalTimeDeserializerTest {

    private final LocalTimeDeserializer sut = new LocalTimeDeserializer(new OffsetTimeDeserializer());

    @Test
    void deserializeDeserializesSpecifiedIsoFormattedStringValue() {
        final LocalTime value = LocalTime.now();
        final JsonObject input =
                Json.createObjectBuilder().add(JsonLd.VALUE, value.format(DateTimeFormatter.ISO_LOCAL_TIME)).build();

        final LocalTime result = sut.deserialize(input, deserializationContext(LocalTime.class));
        assertEquals(value, result);
    }
}