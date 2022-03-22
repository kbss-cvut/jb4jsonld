package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jsonld.JsonLd;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;

import static cz.cvut.kbss.jsonld.deserialization.datetime.OffsetDateTimeDeserializerTest.deserializationContext;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InstantDeserializerTest {

    private final InstantDeserializer sut = new InstantDeserializer(new OffsetDateTimeDeserializer());

    @Test
    void deserializeSupportsDeserializationOfEpochMillis() {
        final Instant value = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        final Map<String, Object> input = Collections.singletonMap(JsonLd.VALUE, value.toEpochMilli());

        final Instant result = sut.deserialize(input, deserializationContext(Instant.class));
        assertEquals(value, result);
    }

    @Test
    void deserializeSupportsDeserializationOfIsoUTCDatetimeString() {
        final Instant value = Instant.now();
        final Map<String, Object> input = Collections.singletonMap(JsonLd.VALUE, value.toString());

        final Instant result = sut.deserialize(input, deserializationContext(Instant.class));
        assertEquals(value, result);
    }
}