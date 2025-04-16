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

import cz.cvut.kbss.jopa.vocabulary.DC;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jsonld.exception.AmbiguousTermMappingException;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;

class EmbeddedTermMappingHolderTest {

    private final EmbeddedTermMappingHolder sut = new EmbeddedTermMappingHolder();

    @Test
    void registerTermMappingThrowsAmbiguousTermMappingExceptionWhenRootHolderAlreadyContainsTermMapping() {
        final String term = "name";
        sut.registerTermMapping(term, JsonNodeFactory.createStringLiteralNode(term, RDFS.LABEL));
        assertThrows(AmbiguousTermMappingException.class,
                     () -> sut.registerTermMapping(term,
                                                   JsonNodeFactory.createStringLiteralNode(term, DC.Terms.TITLE)));
    }

    @Test
    void registerTermMappingDoesNothingWhenParentContextAlreadyHasEquivalentMappingForSpecifiedTerm() {
        final EmbeddedTermMappingHolder child = new EmbeddedTermMappingHolder(sut);
        final String term = "name";
        sut.registerTermMapping(term, JsonNodeFactory.createStringLiteralNode(term, RDFS.LABEL));
        child.registerTermMapping(term, JsonNodeFactory.createStringLiteralNode(term, RDFS.LABEL));
        assertThat(child.getMapping(), not(hasKey(term)));
        assertTrue(child.hasTermMapping(term));
    }

    @Test
    void getMappedTermReturnsMappedValueFromParentWhenItIsNotPresentInCurrentHolder() {
        final EmbeddedTermMappingHolder child = new EmbeddedTermMappingHolder(sut);
        final String term = "name";
        sut.registerTermMapping(term, JsonNodeFactory.createStringLiteralNode(term, RDFS.LABEL));
        final Optional<String> result = child.getMappedTerm(RDFS.LABEL);
        assertTrue(result.isPresent());
        assertEquals(term, result.get());
    }
}