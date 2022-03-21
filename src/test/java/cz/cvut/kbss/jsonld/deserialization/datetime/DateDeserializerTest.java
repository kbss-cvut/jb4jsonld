package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jsonld.JsonLd;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static cz.cvut.kbss.jsonld.deserialization.datetime.OffsetDateTimeDeserializerTest.deserializationContext;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DateDeserializerTest {

    private final DateDeserializer sut = new DateDeserializer(new OffsetDateTimeDeserializer());

    @Test
    void deserializeSupportsDeserializationOfEpochMillis() {
        final Date value = new Date();
        final Map<String, Object> input = Collections.singletonMap(JsonLd.VALUE, value.getTime());

        final Date result = sut.deserialize(input, deserializationContext(Date.class));
        assertEquals(value, result);
    }

    @Test
    void deserializeSupportsDeserializationOfIsoUTCDatetimeString() {
        final Date value = new Date();
        final Map<String, Object> input = Collections.singletonMap(JsonLd.VALUE, value.toInstant().toString());

        final Date result = sut.deserialize(input, deserializationContext(Date.class));
        assertEquals(value, result);
    }
}