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
package cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime;

import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.SerializerUtils;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.temporal.TemporalAmount;

/**
 * Serializes {@link TemporalAmount} instances ({@link java.time.Duration}, {@link java.time.Period}) to JSON object
 * with {@literal xsd:duration} datatype and value in the ISO 8601 format.
 */
public class TemporalAmountSerializer implements ValueSerializer<TemporalAmount> {

    @Override
    public JsonNode serialize(TemporalAmount value, SerializationContext<TemporalAmount> ctx) {
        return SerializerUtils.createdTypedValueNode(ctx.getTerm(), value.toString(), XSD.DURATION);
    }
}
