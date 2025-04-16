/*
 * JB4JSON-LD
 * Copyright (C) 2025 Czech Technical University in Prague
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

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.time.Period;
import java.time.format.DateTimeParseException;

import static cz.cvut.kbss.jsonld.deserialization.datetime.OffsetDateTimeDeserializerTest.deserializationContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PeriodDeserializerTest {

    private final PeriodDeserializer sut = new PeriodDeserializer();

    @Test
    void deserializeDeserializesIsoStringToPeriod() {
        final Period value =
                Period.of(Generator.randomInt(5, 100), Generator.randomInt(1, 12), Generator.randomInt(1, 28));
        final JsonObject
                input = Json.createObjectBuilder().add(JsonLd.VALUE, value.toString()).build();
        final Period result = sut.deserialize(input, deserializationContext(Period.class));
        assertEquals(value, result);
    }

    @Test
    void deserializeThrowsJsonLdDeserializationExceptionWhenInputIsMissingValueAttribute() {
        final JsonObject input = Json.createObjectBuilder().add("notValue", Period.ofMonths(8).toString()).build();
        final JsonLdDeserializationException ex = assertThrows(JsonLdDeserializationException.class,
                                                               () -> sut.deserialize(input, deserializationContext(
                                                                       Period.class)));
        assertThat(ex.getMessage(), containsString(JsonLd.VALUE));
        assertThat(ex.getMessage(), containsString("missing"));
    }

    @Test
    void deserializeThrowsJsonLdDeserializationExceptionWhenInputIsNotInIsoFormat() {
        final JsonObject input = Json.createObjectBuilder().add(JsonLd.VALUE, "invalid").build();
        final JsonLdDeserializationException ex = assertThrows(JsonLdDeserializationException.class,
                                                               () -> sut.deserialize(input, deserializationContext(
                                                                       Period.class)));
        assertInstanceOf(DateTimeParseException.class, ex.getCause());
    }
}