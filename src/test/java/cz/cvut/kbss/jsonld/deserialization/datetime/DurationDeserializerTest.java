package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Map;

import static cz.cvut.kbss.jsonld.deserialization.datetime.OffsetDateTimeDeserializerTest.deserializationContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

class DurationDeserializerTest {

    private final DurationDeserializer sut = new DurationDeserializer();

    @Test
    void deserializeDeserializesIsoStringToDuration() {
        final Duration value = Duration.ofSeconds(Generator.randomCount(5, 10000));
        final Map<String, Object> input = Collections.singletonMap(JsonLd.VALUE, value.toString());
        final Duration result = sut.deserialize(input, deserializationContext(Duration.class));
        assertEquals(value, result);
    }

    @Test
    void deserializeThrowsJsonLdDeserializationExceptionWhenInputIsMissingValueAttribute() {
        final Map<String, Object> input = Collections.singletonMap("notValue", Duration.ofSeconds(100));
        final JsonLdDeserializationException ex = assertThrows(JsonLdDeserializationException.class,
                                                               () -> sut.deserialize(input, deserializationContext(
                                                                       Duration.class)));
        assertThat(ex.getMessage(), containsString(JsonLd.VALUE));
        assertThat(ex.getMessage(), containsString("missing"));
    }

    @Test
    void deserializeThrowsJsonLdDeserializationExceptionWhenInputIsNotInIsoFormat() {
        final Map<String, Object> input = Collections.singletonMap(JsonLd.VALUE, "NotValid");
        final JsonLdDeserializationException ex = assertThrows(JsonLdDeserializationException.class,
                                                               () -> sut.deserialize(input, deserializationContext(
                                                                       Duration.class)));
        assertInstanceOf(DateTimeParseException.class, ex.getCause());
    }
}