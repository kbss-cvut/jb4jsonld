package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jopa.datatype.exception.DatatypeMappingException;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import org.junit.jupiter.api.Test;

import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

import static cz.cvut.kbss.jsonld.deserialization.datetime.OffsetDateTimeDeserializerTest.deserializationContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

class OffsetTimeDeserializerTest {

    private final OffsetTimeDeserializer sut = new OffsetTimeDeserializer();

    @Test
    void deserializeParsesSpecifiedISOOffsetTimeValue() {
        final OffsetTime value = OffsetTime.now();
        final Map<String, Object> input =
                Collections.singletonMap(JsonLd.VALUE, value.format(DateTimeFormatter.ISO_OFFSET_TIME));

        final OffsetTime result = sut.deserialize(input, deserializationContext(OffsetTime.class));
        assertEquals(value, result);
    }

    @Test
    void deserializeThrowsJsonLdDeserializationExceptionWhenInputIsMissingValueAttribute() {
        final Map<String, Object> input =
                Collections.singletonMap("notValue", OffsetTime.now().format(DateTimeFormatter.ISO_OFFSET_TIME));

        final JsonLdDeserializationException ex = assertThrows(JsonLdDeserializationException.class,
                                                               () -> sut.deserialize(input, deserializationContext(
                                                                       OffsetTime.class)));
        assertThat(ex.getMessage(), containsString(JsonLd.VALUE));
        assertThat(ex.getMessage(), containsString("missing"));
    }

    @Test
    void deserializeThrowsJsonLdDeserializationExceptionWhenInputIsInInvalidFormat() {
        final Map<String, Object> input = Collections.singletonMap(JsonLd.VALUE, "invalidValue");
        final JsonLdDeserializationException ex = assertThrows(JsonLdDeserializationException.class,
                                                               () -> sut.deserialize(input, deserializationContext(
                                                                       OffsetTime.class)));
        assertInstanceOf(DatatypeMappingException.class, ex.getCause());
    }
}