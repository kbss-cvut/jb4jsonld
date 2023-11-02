package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jopa.datatype.exception.DatatypeMappingException;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;

import static cz.cvut.kbss.jsonld.deserialization.datetime.OffsetDateTimeDeserializerTest.deserializationContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OffsetTimeDeserializerTest {

    private final OffsetTimeDeserializer sut = new OffsetTimeDeserializer();

    @Test
    void deserializeParsesSpecifiedISOOffsetTimeValue() {
        final OffsetTime value = OffsetTime.now();
        final JsonObject input =
                Json.createObjectBuilder().add(JsonLd.VALUE, value.format(DateTimeFormatter.ISO_OFFSET_TIME)).build();

        final OffsetTime result = sut.deserialize(input, deserializationContext(OffsetTime.class));
        assertEquals(value, result);
    }

    @Test
    void deserializeThrowsJsonLdDeserializationExceptionWhenInputIsMissingValueAttribute() {
        final JsonObject input =
                Json.createObjectBuilder().add("notValue", OffsetTime.now().format(DateTimeFormatter.ISO_OFFSET_TIME))
                    .build();

        final JsonLdDeserializationException ex = assertThrows(JsonLdDeserializationException.class,
                                                               () -> sut.deserialize(input, deserializationContext(
                                                                       OffsetTime.class)));
        assertThat(ex.getMessage(), containsString(JsonLd.VALUE));
        assertThat(ex.getMessage(), containsString("missing"));
    }

    @Test
    void deserializeThrowsJsonLdDeserializationExceptionWhenInputIsInInvalidFormat() {
        final JsonObject input = Json.createObjectBuilder().add(JsonLd.VALUE, "invalidValue").build();
        final JsonLdDeserializationException ex = assertThrows(JsonLdDeserializationException.class,
                                                               () -> sut.deserialize(input, deserializationContext(
                                                                       OffsetTime.class)));
        assertInstanceOf(DatatypeMappingException.class, ex.getCause());
    }
}