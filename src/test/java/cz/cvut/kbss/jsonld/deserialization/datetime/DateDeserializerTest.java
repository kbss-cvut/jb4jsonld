package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jsonld.JsonLd;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static cz.cvut.kbss.jsonld.deserialization.datetime.OffsetDateTimeDeserializerTest.deserializationContext;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DateDeserializerTest {

    private final DateDeserializer sut = new DateDeserializer(new OffsetDateTimeDeserializer());

    @Test
    void deserializeSupportsDeserializationOfEpochMillis() {
        final Date value = new Date();
        final JsonObject input = Json.createObjectBuilder().add(JsonLd.VALUE, value.getTime()).build();

        final Date result = sut.deserialize(input, deserializationContext(Date.class));
        assertEquals(value, result);
    }

    @Test
    void deserializeSupportsDeserializationOfIsoUTCDatetimeString() {
        final Date value = new Date();
        final JsonObject input = Json.createObjectBuilder().add(JsonLd.VALUE, value.toInstant().toString()).build();

        final Date result = sut.deserialize(input, deserializationContext(Date.class));
        assertEquals(value, result);
    }
}