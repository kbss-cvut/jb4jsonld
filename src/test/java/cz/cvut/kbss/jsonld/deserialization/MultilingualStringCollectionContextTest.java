/*
 * JB4JSON-LD
 * Copyright (C) 2024 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jsonld.deserialization.util.LangString;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.*;

class MultilingualStringCollectionContextTest {

    private static final String TEST_VALUE = "Test";

    private final MultilingualStringCollectionContext<Set<MultilingualString>> sut = new MultilingualStringCollectionContext<>(new HashSet<>(), Collections.emptyMap());

    @Test
    void addItemWithLangStringAddsNewMatchingMultilingualStringToCollection() {
        final String language = "en";
        sut.addItem(new LangString(TEST_VALUE, language));
        assertEquals(Collections.singleton(MultilingualString.create(TEST_VALUE, language)), sut.getInstance());
    }

    @Test
    void addItemWithStringAddsNewLanguageLessMultilingualStringToCollection() {
        sut.addItem(TEST_VALUE);
        final MultilingualString expected = new MultilingualString();
        expected.set(TEST_VALUE);
        assertEquals(Collections.singleton(expected), sut.getInstance());
    }

    @Test
    void addItemWithDifferentTypeAddsNewLanguageLessMultilingualStringToCollection() {
        final Integer value = 117;
        sut.addItem(value);
        final MultilingualString expected = new MultilingualString();
        expected.set(value.toString());
        assertEquals(Collections.singleton(expected), sut.getInstance());
    }

    @Test
    void addItemWithLangStringSetsValueOnExistingMultilingualStringInCollection() {
        final String language = "en";
        sut.addItem(new LangString(TEST_VALUE, language));
        final String valueCs = "test";
        final String languageCs = "cs";
        sut.addItem(new LangString(valueCs, languageCs));
        final MultilingualString expected = new MultilingualString();
        expected.set(language, TEST_VALUE);
        expected.set(languageCs, valueCs);
        assertEquals(Collections.singleton(expected), sut.getInstance());
    }

    @Test
    void addItemWithLangStringSetsValueOnFirstAvailableExistingMultilingualStringInCollection() {
        final String language = "en";
        final String languageCs = "cs";
        final int all = 5;
        final int cs = 3;
        IntStream.range(0, all).forEach(i -> sut.addItem(new LangString(TEST_VALUE + i, language)));
        IntStream.range(0, cs).forEach(i -> sut.addItem(new LangString(TEST_VALUE + i, languageCs)));
        final String testValue = "testValue";
        sut.addItem(new LangString(testValue, languageCs));
        assertEquals(all, sut.getInstance().size());
        IntStream.range(0, all).forEach(i -> assertTrue(sut.getInstance().stream().anyMatch(ms -> ms.contains(language) && Objects.equals(ms.get(language), TEST_VALUE + i))));
        IntStream.range(0, cs).forEach(i -> assertTrue(sut.getInstance().stream().anyMatch(ms -> ms.contains(languageCs) && Objects.equals(ms.get(languageCs), TEST_VALUE + i))));
        assertTrue(sut.getInstance().stream().anyMatch(ms -> ms.contains(languageCs) && Objects.equals(ms.get(languageCs), testValue)));
    }

    @Test
    void addItemWithStringAddsNewMultilingualStringToCollectionContainingExistingMultilingualStrings() {
        final String language = "en";
        sut.addItem(new LangString(TEST_VALUE, language));
        final String valueCs = "test";
        sut.addItem(valueCs);
        assertEquals(2, sut.getInstance().size());
        final MultilingualString expected = new MultilingualString();
        expected.set(valueCs);
        assertThat(sut.getInstance(), hasItems(MultilingualString.create(TEST_VALUE, language), expected));
    }
}
