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

import cz.cvut.kbss.jopa.datatype.xsd.XsdTimeMapper;
import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.ValueDeserializer;
import cz.cvut.kbss.jsonld.deserialization.util.ValueUtils;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import jakarta.json.JsonValue;

import java.time.OffsetTime;

/**
 * Deserializes values to {@link OffsetTime}.
 * <p>
 * The values are expected to be String in the ISO time format.
 */
public class OffsetTimeDeserializer implements ValueDeserializer<OffsetTime> {

    @Override
    public OffsetTime deserialize(JsonValue jsonNode, DeserializationContext<OffsetTime> ctx) {
        final JsonValue value = ValueUtils.getValue(jsonNode);
        try {
            return XsdTimeMapper.map(ValueUtils.stringValue(value));
        } catch (RuntimeException e) {
            throw new JsonLdDeserializationException("Unable to deserialize time value.", e);
        }
    }
}
