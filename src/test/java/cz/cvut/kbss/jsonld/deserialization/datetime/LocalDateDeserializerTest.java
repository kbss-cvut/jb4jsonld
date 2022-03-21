package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

import static cz.cvut.kbss.jsonld.deserialization.datetime.OffsetDateTimeDeserializerTest.deserializationContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

class LocalDateDeserializerTest {

    private final LocalDateDeserializer sut = new LocalDateDeserializer();

    @Test
    void deserializeDeserializesSpecifiedIsoFormattedDateString() {
        final LocalDate value = LocalDate.now();
        final Map<String, Object> input = Collections.singletonMap(JsonLd.VALUE, value.format(DateTimeFormatter.ISO_DATE));

        final LocalDate result = sut.deserialize(input, deserializationContext(LocalDate.class));
        assertEquals(value, result);
    }

    @Test
    void deserializeThrowsJsonLdDeserializationExceptionWhenInputIsMissingValueAttribute() {
        final Map<String, Object> input = Collections.singletonMap("notValue", LocalDate.now().format(DateTimeFormatter.ISO_DATE));

        final JsonLdDeserializationException ex = assertThrows(JsonLdDeserializationException.class,
                () -> sut.deserialize(input, deserializationContext(LocalDate.class)));
        assertThat(ex.getMessage(), containsString(JsonLd.VALUE));
        assertThat(ex.getMessage(), containsString("missing"));
    }
}