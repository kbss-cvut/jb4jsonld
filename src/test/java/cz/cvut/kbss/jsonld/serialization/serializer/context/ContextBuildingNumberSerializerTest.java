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
package cz.cvut.kbss.jsonld.serialization.serializer.context;

import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.context.JsonLdContext;
import cz.cvut.kbss.jsonld.serialization.context.MappingJsonLdContext;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextBuildingNumberSerializerTest {

    private final ContextBuildingNumberSerializer sut = new ContextBuildingNumberSerializer();

    @ParameterizedTest
    @MethodSource("serializationData")
    void serializeSerializesNumberAsTypedValueNode(Number value, String expectedDatatype) throws Exception {
        final JsonLdContext ctx = new MappingJsonLdContext();
        sut.serialize(value,
                      new SerializationContext<>("http://example.com/number",
                                                 TestEntity.class.getDeclaredField("numberField"), value,
                                                 ctx));
        final Optional<JsonNode> mapping = ctx.getTermMapping("numberField");
        assertTrue(mapping.isPresent());
        assertInstanceOf(ObjectNode.class, mapping.get());
        final ObjectNode objectNode = (ObjectNode) mapping.get();
        assertThat(objectNode.getItems(),
                   hasItems(JsonNodeFactory.createLiteralNode(JsonLd.ID, "http://example.com/number"),
                            JsonNodeFactory.createLiteralNode(JsonLd.TYPE, expectedDatatype)));
    }

    static Stream<Arguments> serializationData() {
        return Stream.of(
                Arguments.of((short) 1, XSD.SHORT),
                Arguments.of(1, XSD.INT),
                Arguments.of(1L, XSD.LONG),
                Arguments.of(1.0f, XSD.FLOAT),
                Arguments.of(1.0, XSD.DOUBLE),
                Arguments.of(BigInteger.valueOf(1), XSD.INTEGER),
                Arguments.of(BigDecimal.valueOf(1.1), XSD.DECIMAL)
        );
    }

    private static class TestEntity {

        private Number numberField;
    }
}