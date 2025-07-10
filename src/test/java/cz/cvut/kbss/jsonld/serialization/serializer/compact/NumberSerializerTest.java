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
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.context.DummyJsonLdContext;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NumberSerializerTest {

    private final NumberSerializer sut = new NumberSerializer();

    @ParameterizedTest
    @MethodSource("serializationData")
    void serializeSerializesNumberAsTypedValueNode(Number value, JsonNode expected) {
        assertEquals(expected, sut.serialize(value, new SerializationContext<>(value, DummyJsonLdContext.INSTANCE)));
    }

    static Stream<Arguments> serializationData() {
        return Stream.of(
                Arguments.of((short) 1, typedNode((short) 1, XSD.SHORT)),
                Arguments.of(1, typedNode(1, XSD.INT)),
                Arguments.of(1L, typedNode(1L, XSD.LONG)),
                Arguments.of(1.0f, typedNode(1.0f, XSD.FLOAT)),
                Arguments.of(1.0, typedNode(1.0, XSD.DOUBLE)),
                Arguments.of(BigInteger.valueOf(1), typedNode(BigInteger.valueOf(1), XSD.INTEGER)),
                Arguments.of(BigDecimal.valueOf(1.1), typedNode(BigDecimal.valueOf(1.1), XSD.DECIMAL))
        );
    }

    private static JsonNode typedNode(Number value, String datatype) {
        final ObjectNode node = JsonNodeFactory.createObjectNode();
        node.addItem(JsonNodeFactory.createStringLiteralNode(JsonLd.TYPE, datatype));
        node.addItem(JsonNodeFactory.createNumericLiteralNode(JsonLd.VALUE, value));
        return node;
    }
}