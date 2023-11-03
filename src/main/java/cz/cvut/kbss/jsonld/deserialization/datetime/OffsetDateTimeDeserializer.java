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

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.ValueDeserializer;
import cz.cvut.kbss.jsonld.deserialization.util.ValueUtils;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import jakarta.json.JsonNumber;
import jakarta.json.JsonValue;

import java.time.OffsetDateTime;

/**
 * Deserializes values to {@link OffsetDateTime}.
 * <p>
 * If the value is a number, it is taken as the number of milliseconds since the Unix Epoch. Otherwise, it is parsed as
 * a string.
 * <p>
 * If a datetime pattern is configured ({@link cz.cvut.kbss.jsonld.ConfigParam#DATE_TIME_FORMAT}), it is used to parse
 * the value. Otherwise, the default ISO-based pattern is used.
 */
public class OffsetDateTimeDeserializer implements ValueDeserializer<OffsetDateTime> {

    private final StringBasedDateTimeResolver stringResolver = new StringBasedDateTimeResolver();

    private final EpochBasedDateTimeResolver epochResolver = new EpochBasedDateTimeResolver();

    @Override
    public OffsetDateTime deserialize(JsonValue jsonNode, DeserializationContext<OffsetDateTime> ctx) {
        final JsonValue value = ValueUtils.getValue(jsonNode);
        try {
            return value.getValueType() == JsonValue.ValueType.NUMBER ? epochResolver.resolve((JsonNumber) value) :
                   stringResolver.resolve(ValueUtils.stringValue(value));
        } catch (RuntimeException e) {
            throw new JsonLdDeserializationException("Unable to deserialize datetime value.", e);
        }
    }

    @Override
    public void configure(Configuration config) {
        stringResolver.configure(config);
    }
}
