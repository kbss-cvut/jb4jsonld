package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jopa.datatype.exception.DatatypeMappingException;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static cz.cvut.kbss.jsonld.deserialization.datetime.OffsetDateTimeDeserializerTest.deserializationContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalDateDeserializerTest {

    private final LocalDateDeserializer sut = new LocalDateDeserializer();

    @Test
    void deserializeDeserializesSpecifiedIsoFormattedDateString() {
        final LocalDate value = LocalDate.now();
        final JsonObject input = Json.createObjectBuilder().add(JsonLd.VALUE, value.format(DateTimeFormatter.ISO_DATE)).build();

        final LocalDate result = sut.deserialize(input, deserializationContext(LocalDate.class));
        assertEquals(value, result);
    }

    @Test
    void deserializeThrowsJsonLdDeserializationExceptionWhenInputIsMissingValueAttribute() {
        final JsonObject input = Json.createObjectBuilder().add("notValue", LocalDate.now().format(DateTimeFormatter.ISO_DATE)).build();

        final JsonLdDeserializationException ex = assertThrows(JsonLdDeserializationException.class,
                                                               () -> sut.deserialize(input, deserializationContext(
                                                                       LocalDate.class)));
        assertThat(ex.getMessage(), containsString(JsonLd.VALUE));
        assertThat(ex.getMessage(), containsString("missing"));
    }

    @Test
    void deserializeThrowsJsonLdDeserializationExceptionWhenInputIsInInvalidFormat() {
        final JsonObject input = Json.createObjectBuilder().add(JsonLd.VALUE, "invalidValue").build();
        final JsonLdDeserializationException ex = assertThrows(JsonLdDeserializationException.class,
                                                               () -> sut.deserialize(input, deserializationContext(
                                                                       LocalDate.class)));
        assertInstanceOf(DatatypeMappingException.class, ex.getCause());
    }
}