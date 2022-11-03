/**
 * Copyright (C) 2022 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.LangStringNode;
import cz.cvut.kbss.jsonld.serialization.model.LiteralNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultilingualStringSerializerTest {

    private static final String ATTRIBUTE_NAME = "label";

    private MultilingualString value;

    private MultilingualStringSerializer sut;

    @BeforeEach
    void setUp() {
        this.value = new MultilingualString();
        this.sut = new MultilingualStringSerializer();
    }

    @Test
    void serializeWithAttributeAndValuesReturnsCollectionNodeWithTranslations() {
        value.set("en", "construction");
        value.set("cs", "stavba");
        final JsonNode result = sut.serialize(ATTRIBUTE_NAME, value);
        assertThat(result, instanceOf(CollectionNode.class));
        final CollectionNode<?> colNode = (CollectionNode<?>) result;
        assertEquals(ATTRIBUTE_NAME, result.getName());
        assertEquals(2, colNode.getItems().size());
        colNode.getItems().forEach(item -> assertThat(item, instanceOf(LangStringNode.class)));
    }

    @Test
    void serializeWithAttributeAndSingleTranslationReturnsLangStringNode() {
        value.set("en", "construction");
        final JsonNode result = sut.serialize(ATTRIBUTE_NAME, value);
        assertThat(result, instanceOf(LangStringNode.class));
        assertEquals(ATTRIBUTE_NAME, result.getName());
    }

    @Test
    void serializeWithAttributeAndValuesHandlesSimpleLiteral() {
        value.set("construction");
        value.set("cs", "stavba");
        final JsonNode result = sut.serialize(ATTRIBUTE_NAME, value);
        assertThat(result, instanceOf(CollectionNode.class));
        final CollectionNode<?> colNode = (CollectionNode<?>) result;
        assertEquals(ATTRIBUTE_NAME, result.getName());
        assertEquals(2, colNode.getItems().size());
        colNode.getItems().forEach(item -> assertThat(item, instanceOf(LangStringNode.class)));
    }

    @Test
    void serializeReturnsCollectionNodeWithTranslations() {
        value.set("en", "construction");
        value.set("cs", "stavba");
        final JsonNode result = sut.serialize(value);
        assertThat(result, instanceOf(CollectionNode.class));
        final CollectionNode<?> colNode = (CollectionNode<?>) result;
        assertEquals(2, colNode.getItems().size());
        colNode.getItems().forEach(item -> assertThat(item, instanceOf(LangStringNode.class)));
    }

    @Test
    void serializeWithSingleTranslationReturnsLangStringNode() {
        value.set("en", "construction");
        final JsonNode result = sut.serialize(value);
        assertThat(result, instanceOf(LangStringNode.class));
    }

    @Test
    void serializeReturnsLangStringNodeWithNoneKeyForLanguageLessValue() {
        value.set("language-less");
        final JsonNode result = sut.serialize(value);
        assertThat(result, instanceOf(LangStringNode.class));
        final LangStringNode lsResult = (LangStringNode) result;
        assertTrue(lsResult.getItems().stream().anyMatch(n -> {
            assertThat(n, instanceOf(LiteralNode.class));
            return n.getName().equals(JsonLd.LANGUAGE) && ((LiteralNode<?>) n).getValue().equals(JsonLd.NONE);
        }));
        assertTrue(lsResult.getItems().stream().anyMatch(n -> {
            assertThat(n, instanceOf(LiteralNode.class));
            return n.getName().equals(JsonLd.VALUE) && ((LiteralNode<?>) n).getValue().equals(value.get());
        }));
    }
}
