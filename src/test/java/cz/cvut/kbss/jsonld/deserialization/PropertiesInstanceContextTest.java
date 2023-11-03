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
package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jopa.model.annotations.Properties;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PropertiesInstanceContextTest {

    private static final String VALUE = "halsey@unsc.org";

    private Field personProperties;

    @BeforeEach
    void setUp() throws Exception {
        this.personProperties = Person.class.getDeclaredField("properties");
    }

    @Test
    void addItemInsertsCollectionIntoPropertiesMapAndAddsValueToIt() {
        final Map<String, Set<String>> map = new HashMap<>();
        final InstanceContext<Map> ctx = new PropertiesInstanceContext(map, Vocabulary.USERNAME, personProperties);
        ctx.addItem(VALUE);
        assertTrue(map.containsKey(Vocabulary.USERNAME));
        assertEquals(1, map.get(Vocabulary.USERNAME).size());
        assertTrue(map.get(Vocabulary.USERNAME).contains(VALUE));
    }

    @Test
    void addItemReusesPropertyCollectionWhenItIsAlreadyPresentInPropertiesMap() {
        final Map<String, Set<String>> map = new HashMap<>();
        map.put(Vocabulary.USERNAME, new HashSet<>());
        map.get(Vocabulary.USERNAME).add(VALUE);
        final InstanceContext<Map> ctx = new PropertiesInstanceContext(map, Vocabulary.USERNAME, personProperties);
        final String newValue = "halsey@oni.org";
        ctx.addItem(newValue);
        assertEquals(2, map.get(Vocabulary.USERNAME).size());
        assertTrue(map.get(Vocabulary.USERNAME).contains(VALUE));
        assertTrue(map.get(Vocabulary.USERNAME).contains(newValue));
    }

    @Test
    void addItemPutsSingleValueIntoPropertiesMapWhenMapIsConfiguredAsSingleValued() throws Exception {
        final Map<?, ?> map = new HashMap<>();
        final InstanceContext<Map> ctx = new PropertiesInstanceContext(map, Vocabulary.USERNAME,
                SingleValued.class.getDeclaredField("properties"));
        ctx.addItem(VALUE);
        assertEquals(VALUE, map.get(Vocabulary.USERNAME));
    }

    private static class SingleValued {
        @Properties
        private Map<String, String> properties;
    }

    @Test
    void addItemConvertsPropertyIdentifierToCorrectType() throws Exception {
        final Map<URI, Set<?>> map = new HashMap<>();
        final InstanceContext<Map> ctx = new PropertiesInstanceContext(map, Vocabulary.USERNAME,
                TypedProperties.class.getDeclaredField("properties"));
        ctx.addItem(VALUE);
        assertTrue(map.containsKey(URI.create(Vocabulary.USERNAME)));
    }

    private static class TypedProperties {
        @Properties
        private Map<URI, Set<?>> properties;
    }

    @Test
    void addItemInsertsValuesOfCorrectTypes() throws Exception {
        final Map<URI, Set<?>> map = new HashMap<>();
        final InstanceContext<Map> ctx = new PropertiesInstanceContext(map, Vocabulary.IS_ADMIN,
                TypedProperties.class.getDeclaredField("properties"));
        ctx.addItem(true);
        assertEquals(Boolean.TRUE, map.get(URI.create(Vocabulary.IS_ADMIN)).iterator().next());
    }

    @Test
    void addItemConvertsValuesToCorrectType() {
        final Map<String, Set<String>> map = new HashMap<>();
        final InstanceContext<Map> ctx = new PropertiesInstanceContext(map, Vocabulary.IS_ADMIN, personProperties);
        ctx.addItem(true);
        assertEquals(Boolean.TRUE.toString(), map.get(Vocabulary.IS_ADMIN).iterator().next());
    }

    @Test
    void addItemThrowsExceptionWhenMultipleItemsForSingularPropertyAreAdded() throws Exception {
        final Map<String, String> map = new HashMap<>();
        final InstanceContext<Map> ctx = new PropertiesInstanceContext(map, Vocabulary.USERNAME,
                SingleValued.class.getDeclaredField("properties"));
        ctx.addItem(VALUE);
        JsonLdDeserializationException result = assertThrows(JsonLdDeserializationException.class,
                () -> ctx.addItem("halsey@oni.org"));
        assertThat(result.getMessage(),
                containsString("Encountered multiple values of property " + Vocabulary.USERNAME));
    }
}
