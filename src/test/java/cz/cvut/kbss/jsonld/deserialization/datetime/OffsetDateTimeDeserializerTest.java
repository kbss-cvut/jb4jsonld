package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.util.TargetClassResolver;
import cz.cvut.kbss.jsonld.deserialization.util.TypeMap;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OffsetDateTimeDeserializerTest {

    private final OffsetDateTimeDeserializer sut = new OffsetDateTimeDeserializer();

    @Test
    void deserializeResolvesValueInEpochMillis() {
        final OffsetDateTime value = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        final Map<String, Object> input = Collections.singletonMap(JsonLd.VALUE, value.toInstant().toEpochMilli());

        final OffsetDateTime result = sut.deserialize(input, deserializationContext(OffsetDateTime.class));
        assertEquals(value.toInstant(), result.toInstant());
    }

    static <T> DeserializationContext<T> deserializationContext(Class<T> forType) {
        return new DeserializationContext<>(forType, new TargetClassResolver(new TypeMap()));
    }

    @Test
    void deserializeResolveValueFromIsoOffsetString() {
        final OffsetDateTime value = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        final Map<String, Object> input = Collections.singletonMap(JsonLd.VALUE, value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        final OffsetDateTime result = sut.deserialize(input, deserializationContext(OffsetDateTime.class));
        assertEquals(value, result);
    }

    @Test
    void deserializeResolvesValueFromCustomFormattedString() {
        final String pattern = "yyyy-dd-MM'T'HH:mm:ssXXX";
        final OffsetDateTime value = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        final Map<String, Object> input = Collections.singletonMap(JsonLd.VALUE, value.format(DateTimeFormatter.ofPattern(pattern)));

        final Configuration configuration = new Configuration();
        configuration.set(ConfigParam.DATE_TIME_FORMAT, pattern);
        sut.configure(configuration);
        final OffsetDateTime result = sut.deserialize(input, deserializationContext(OffsetDateTime.class));
        assertEquals(value, result);
    }

    @Test
    void deserializeThrowsJsonLdDeserializationExceptionWhenInputIsMissingValueAttribute() {
        final Map<String, Object> input = Collections.singletonMap("notValue",
                OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        final JsonLdDeserializationException ex = assertThrows(JsonLdDeserializationException.class,
                () -> sut.deserialize(input, deserializationContext(OffsetDateTime.class)));
        assertThat(ex.getMessage(), containsString(JsonLd.VALUE));
        assertThat(ex.getMessage(), containsString("missing"));
    }
}