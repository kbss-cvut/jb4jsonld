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
package cz.cvut.kbss.jsonld.serialization.serializer.compact;

import cz.cvut.kbss.jopa.model.annotations.Individual;
import cz.cvut.kbss.jopa.vocabulary.OWL;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.OwlPropertyType;
import cz.cvut.kbss.jsonld.exception.InvalidEnumMappingException;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.context.DummyJsonLdContext;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;

class IndividualSerializerTest {

    private final IndividualSerializer sut = new IndividualSerializer();

    @Test
    void serializeReturnsObjectNodeWithIdForSpecifiedIndividual() {
        final URI individual = Generator.generateUri();
        final SerializationContext<URI> ctx =
                new SerializationContext<>(Vocabulary.ORIGIN, individual, DummyJsonLdContext.INSTANCE);

        final JsonNode result = sut.serialize(individual, ctx);
        assertInstanceOf(ObjectNode.class, result);
        final ObjectNode objectNode = (ObjectNode) result;
        assertEquals(1, objectNode.getItems().size());
        assertThat(objectNode.getItems(),
                   hasItem(JsonNodeFactory.createObjectIdNode(JsonLd.ID, individual.toString())));
    }

    @Test
    void serializeReturnsObjectNodeWithIdForSpecifiedEnumConstantMappedToIndividual() {
        final OwlPropertyType instance = OwlPropertyType.OBJECT_PROPERTY;
        final SerializationContext<OwlPropertyType> ctx =
                new SerializationContext<>(Vocabulary.HAS_PROPERTY_TYPE, instance, DummyJsonLdContext.INSTANCE);

        final JsonNode result = sut.serialize(instance, ctx);
        assertInstanceOf(ObjectNode.class, result);
        final ObjectNode objectNode = (ObjectNode) result;
        assertEquals(1, objectNode.getItems().size());
        assertThat(objectNode.getItems(), hasItem(JsonNodeFactory.createObjectIdNode(JsonLd.ID, OWL.OBJECT_PROPERTY)));
    }

    @Test
    void serializeThrowsInvalidEnumMappingExceptionForEnumConstantWithoutMatchingIndividualMapping() {
        final InvalidEnum instance = InvalidEnum.OBJECT_PROPERTY;
        final SerializationContext<InvalidEnum> ctx =
                new SerializationContext<>(Vocabulary.HAS_PROPERTY_TYPE, instance, DummyJsonLdContext.INSTANCE);

        assertThrows(InvalidEnumMappingException.class, () -> sut.serialize(instance, ctx));
    }

    public enum InvalidEnum {
        @Individual(iri = OWL.DATATYPE_PROPERTY)
        DATATYPE_PROPERTY,
        // Missing individual mapping here
        OBJECT_PROPERTY
    }
}