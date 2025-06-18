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
package cz.cvut.kbss.jsonld.serialization.serializer.compact;

import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.SerializerUtils;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * Serializes numeric values.
 */
public class NumberSerializer implements ValueSerializer<Number> {

    @Override
    public JsonNode serialize(Number value, SerializationContext<Number> ctx) {
        Objects.requireNonNull(value);
        return SerializerUtils.createdTypedValueNode(ctx.getTerm(), value.toString(), getDatatype(value));
    }

    protected String getDatatype(Number value) {
        if (value instanceof Integer) {
            return XSD.INT;
        } else if (value instanceof Long) {
            return XSD.LONG;
        } else if (value instanceof Double) {
            return XSD.DOUBLE;
        } else if (value instanceof Float) {
            return XSD.FLOAT;
        } else if (value instanceof Short) {
            return XSD.SHORT;
        } else if (value instanceof BigInteger) {
            return XSD.INTEGER;
        } else if (value instanceof BigDecimal) {
            return XSD.DECIMAL;
        } else {
            throw new IllegalArgumentException("Unsupported numeric literal type " + value.getClass());
        }
    }

    /**
     * Gets a list of Java types supported by this serializer.
     *
     * @return List of Java classes
     */
    public static List<Class<? extends Number>> getSupportedTypes() {
        return List.of(Integer.class, Long.class, Double.class, Float.class, Short.class, BigInteger.class,
                       BigDecimal.class);
    }
}
