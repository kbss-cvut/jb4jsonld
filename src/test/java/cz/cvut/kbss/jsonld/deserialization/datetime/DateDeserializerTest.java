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
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static cz.cvut.kbss.jsonld.deserialization.datetime.OffsetDateTimeDeserializerTest.deserializationContext;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DateDeserializerTest {

    private final DateDeserializer sut = new DateDeserializer(new OffsetDateTimeDeserializer());

    @Test
    void deserializeSupportsDeserializationOfEpochMillis() {
        final Date value = new Date();
        final JsonObject input = Json.createObjectBuilder().add(JsonLd.VALUE, value.getTime()).build();

        final Date result = sut.deserialize(input, deserializationContext(Date.class));
        assertEquals(value, result);
    }

    @Test
    void deserializeSupportsDeserializationOfIsoUTCDatetimeString() {
        final Date value = new Date();
        final JsonObject input = Json.createObjectBuilder().add(JsonLd.VALUE, value.toInstant().toString()).build();

        final Date result = sut.deserialize(input, deserializationContext(Date.class));
        assertEquals(value, result);
    }
}