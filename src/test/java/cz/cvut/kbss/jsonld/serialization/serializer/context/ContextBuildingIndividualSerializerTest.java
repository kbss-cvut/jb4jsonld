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

import cz.cvut.kbss.jopa.vocabulary.OWL;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.OwlPropertyType;
import cz.cvut.kbss.jsonld.serialization.context.DummyJsonLdContext;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.StringLiteralNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class ContextBuildingIndividualSerializerTest {

    private final ContextBuildingIndividualSerializer sut = new ContextBuildingIndividualSerializer();

    @Test
    void serializeSerializesUriValueAsStringWhenSerializationWithExtendedTermDefinitionInContextIsEnabled() {
        final URI individual = Generator.generateUri();
        final SerializationContext<URI> ctx =
                new SerializationContext<>(Vocabulary.ORIGIN, individual, DummyJsonLdContext.INSTANCE);
        final Configuration config = new Configuration();
        config.set(ConfigParam.SERIALIZE_INDIVIDUALS_USING_EXPANDED_DEFINITION, Boolean.TRUE.toString());

        sut.configure(config);
        final JsonNode result = sut.serialize(individual, ctx);
        assertInstanceOf(StringLiteralNode.class, result);
        final StringLiteralNode node = (StringLiteralNode) result;
        assertEquals(individual.toString(), node.getValue());
    }

    @Test
    void serializeSerializesEnumValueMappedToIndividualAsStringWhenSerializationWithExtendedTermDefinitionInContextIsEnabled() {
        final OwlPropertyType value = OwlPropertyType.OBJECT_PROPERTY;
        final SerializationContext<OwlPropertyType> ctx =
                new SerializationContext<>(Vocabulary.HAS_PROPERTY_TYPE, value, DummyJsonLdContext.INSTANCE);
        final Configuration config = new Configuration();
        config.set(ConfigParam.SERIALIZE_INDIVIDUALS_USING_EXPANDED_DEFINITION, Boolean.TRUE.toString());

        sut.configure(config);
        final JsonNode result = sut.serialize(value, ctx);
        assertInstanceOf(StringLiteralNode.class, result);
        final StringLiteralNode node = (StringLiteralNode) result;
        assertEquals(OWL.OBJECT_PROPERTY, node.getValue());
    }
}