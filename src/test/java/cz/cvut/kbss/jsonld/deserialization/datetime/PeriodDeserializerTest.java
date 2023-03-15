package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import org.junit.jupiter.api.Test;

import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Map;

import static cz.cvut.kbss.jsonld.deserialization.datetime.OffsetDateTimeDeserializerTest.deserializationContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

class PeriodDeserializerTest {

    private final PeriodDeserializer sut = new PeriodDeserializer();

    @Test
    void deserializeDeserializesIsoStringToPeriod() {
        final Period value =
                Period.of(Generator.randomInt(5, 100), Generator.randomInt(1, 12), Generator.randomInt(1, 28));
        final Map<String, Object> input = Collections.singletonMap(JsonLd.VALUE, value.toString());
        final Period result = sut.deserialize(input, deserializationContext(Period.class));
        assertEquals(value, result);
    }

    @Test
    void deserializeThrowsJsonLdDeserializationExceptionWhenInputIsMissingValueAttribute() {
        final Map<String, Object> input = Collections.singletonMap("notValue", Period.ofMonths(8));
        final JsonLdDeserializationException ex = assertThrows(JsonLdDeserializationException.class,
                                                               () -> sut.deserialize(input, deserializationContext(
                                                                       Period.class)));
        assertThat(ex.getMessage(), containsString(JsonLd.VALUE));
        assertThat(ex.getMessage(), containsString("missing"));
    }

    @Test
    void deserializeThrowsJsonLdDeserializationExceptionWhenInputIsNotInIsoFormat() {
        final Map<String, Object> input = Collections.singletonMap(JsonLd.VALUE, "NotValid");
        final JsonLdDeserializationException ex = assertThrows(JsonLdDeserializationException.class,
                                                               () -> sut.deserialize(input, deserializationContext(
                                                                       Period.class)));
        assertInstanceOf(DateTimeParseException.class, ex.getCause());
    }
}