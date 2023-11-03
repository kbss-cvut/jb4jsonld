/*
 * JB4JSON-LD
 * Copyright (C) 2023 Czech Technical University in Prague
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jopa.datatype.exception.DatatypeMappingException;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.util.TargetClassResolver;
import cz.cvut.kbss.jsonld.deserialization.util.TypeMap;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OffsetDateTimeDeserializerTest {

    private final OffsetDateTimeDeserializer sut = new OffsetDateTimeDeserializer();

    @Test
    void deserializeResolvesValueInEpochMillis() {
        final OffsetDateTime value = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        final JsonObject input = Json.createObjectBuilder().add(JsonLd.VALUE, value.toInstant().toEpochMilli()).build();

        final OffsetDateTime result = sut.deserialize(input, deserializationContext(OffsetDateTime.class));
        assertEquals(value.toInstant(), result.toInstant());
    }

    static <T> DeserializationContext<T> deserializationContext(Class<T> forType) {
        return new DeserializationContext<>(forType, new TargetClassResolver(new TypeMap()));
    }

    @Test
    void deserializeResolveValueFromIsoOffsetString() {
        final OffsetDateTime value = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        final JsonObject input =
                Json.createObjectBuilder().add(JsonLd.VALUE, value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                    .build();

        final OffsetDateTime result = sut.deserialize(input, deserializationContext(OffsetDateTime.class));
        assertEquals(value, result);
    }

    @Test
    void deserializeResolvesValueFromCustomFormattedString() {
        final String pattern = "yyyy-dd-MM'T'HH:mm:ssXXX";
        final OffsetDateTime value = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        final JsonObject input =
                Json.createObjectBuilder().add(JsonLd.VALUE, value.format(DateTimeFormatter.ofPattern(pattern)))
                    .build();

        final Configuration configuration = new Configuration();
        configuration.set(ConfigParam.DATE_TIME_FORMAT, pattern);
        sut.configure(configuration);
        final OffsetDateTime result = sut.deserialize(input, deserializationContext(OffsetDateTime.class));
        assertEquals(value, result);
    }

    @Test
    void deserializeThrowsJsonLdDeserializationExceptionWhenInputIsMissingValueAttribute() {
        final JsonObject input = Json.createObjectBuilder().add("notValue", OffsetDateTime.now()
                                                                                          .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                                     .build();
        final JsonLdDeserializationException ex = assertThrows(JsonLdDeserializationException.class,
                                                               () -> sut.deserialize(input, deserializationContext(
                                                                       OffsetDateTime.class)));
        assertThat(ex.getMessage(), containsString(JsonLd.VALUE));
        assertThat(ex.getMessage(), containsString("missing"));
    }

    @Test
    void deserializeThrowsJsonLdDeserializationExceptionWhenInputIsInInvalidFormat() {
        final JsonObject input = Json.createObjectBuilder().add(JsonLd.VALUE, "invalidValue").build();
        final JsonLdDeserializationException ex = assertThrows(JsonLdDeserializationException.class,
                                                               () -> sut.deserialize(input, deserializationContext(
                                                                       OffsetDateTime.class)));
        assertInstanceOf(DatatypeMappingException.class, ex.getCause());
    }
}