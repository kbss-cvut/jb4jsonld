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

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.ValueDeserializer;
import jakarta.json.JsonValue;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

/**
 * Deserializes values to {@link ZonedDateTime}.
 */
public class ZonedDateTimeDeserializer implements ValueDeserializer<ZonedDateTime> {

    private final OffsetDateTimeDeserializer innerDeserializer;

    public ZonedDateTimeDeserializer(OffsetDateTimeDeserializer innerDeserializer) {
        this.innerDeserializer = innerDeserializer;
    }

    @Override
    public ZonedDateTime deserialize(JsonValue jsonNode, DeserializationContext<ZonedDateTime> ctx) {
        return innerDeserializer.deserialize(jsonNode, new DeserializationContext<>(OffsetDateTime.class, ctx.getClassResolver()))
                .toZonedDateTime();
    }

    @Override
    public void configure(Configuration config) {
        innerDeserializer.configure(config);
    }
}
