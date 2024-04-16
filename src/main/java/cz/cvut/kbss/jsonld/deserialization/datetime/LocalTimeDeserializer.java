/*
 * JB4JSON-LD
 * Copyright (C) 2024 Czech Technical University in Prague
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

import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.ValueDeserializer;
import jakarta.json.JsonValue;

import java.time.LocalTime;
import java.time.OffsetTime;

/**
 * Deserializes values to {@link LocalTime}.
 * <p>
 * The values are expected to be String in the ISO time format.
 */
public class LocalTimeDeserializer implements ValueDeserializer<LocalTime> {

    private final OffsetTimeDeserializer innerDeserializer;

    public LocalTimeDeserializer(OffsetTimeDeserializer innerDeserializer) {
        this.innerDeserializer = innerDeserializer;
    }

    @Override
    public LocalTime deserialize(JsonValue jsonNode, DeserializationContext<LocalTime> ctx) {
        return innerDeserializer.deserialize(jsonNode,
                new DeserializationContext<>(OffsetTime.class, ctx.getClassResolver())).toLocalTime();
    }
}
