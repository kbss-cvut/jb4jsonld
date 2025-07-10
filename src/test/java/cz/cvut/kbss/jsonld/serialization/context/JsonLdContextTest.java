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
package cz.cvut.kbss.jsonld.serialization.context;

import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.CompositeNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.*;

class JsonLdContextTest {

    private final MappingJsonLdContext sut = new MappingJsonLdContext();

    @Test
    void registerTermMappingWithIriAddsTermMappingToLiteralJsonNode() throws Exception {
        final String term = Person.getFirstNameField().getName();
        sut.registerTermMapping(term, Vocabulary.FIRST_NAME);
        assertTrue(sut.hasTermMapping(term));
        final Optional<JsonNode> result = sut.getTermMapping(term);
        assertTrue(result.isPresent());
        assertEquals(result.get(), JsonNodeFactory.createStringLiteralNode(term, Vocabulary.FIRST_NAME));
    }

    @Test
    void registerTermMappingWithIriTwiceDoesNotThrowException() throws Exception {
        final String term = Person.getFirstNameField().getName();
        sut.registerTermMapping(term, Vocabulary.FIRST_NAME);
        assertDoesNotThrow(() -> sut.registerTermMapping(term, Vocabulary.FIRST_NAME));
    }

    @Test
    void getContextNodeReturnsCompositeNodeWithRegisteredMappings() throws Exception {
        final String firstName = Person.getFirstNameField().getName();
        sut.registerTermMapping(firstName, Vocabulary.FIRST_NAME);
        final String lastName = Person.getLastNameField().getName();
        sut.registerTermMapping(lastName, Vocabulary.LAST_NAME);

        final JsonNode result = sut.getContextNode();
        assertThat(result, instanceOf(CompositeNode.class));
        final CompositeNode<?> compositeResult = (CompositeNode<?>) result;
        assertThat(compositeResult.getItems(),
                   hasItems(JsonNodeFactory.createStringLiteralNode(firstName, Vocabulary.FIRST_NAME),
                            JsonNodeFactory.createStringLiteralNode(lastName, Vocabulary.LAST_NAME)));
    }
}