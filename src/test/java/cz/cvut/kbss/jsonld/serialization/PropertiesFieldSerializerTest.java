/**
 * Copyright (C) 2020 Czech Technical University in Prague
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

import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.serialization.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PropertiesFieldSerializerTest {

    private Field field;

    private final FieldSerializer serializer = new PropertiesFieldSerializer();

    @BeforeEach
    void setUp() throws Exception {
        this.field = Person.class.getDeclaredField("properties");
    }

    @Test
    void serializeFieldReturnsListOfAttributeNodesWithArrayValuesForCollectionsOfValues() {
        final Map<String, Set<String>> properties = Generator.generateProperties(false);
        final List<JsonNode> result = serializer.serializeField(field, properties);
        assertEquals(properties.size(), result.size());
        for (JsonNode node : result) {
            assertTrue(properties.containsKey(node.getName()));
            assertTrue(node instanceof CollectionNode);
            final CollectionNode arr = (CollectionNode) node;
            arr.getItems().forEach(item -> {
                assertTrue(item instanceof StringLiteralNode);
                assertTrue(properties.get(node.getName()).contains(((StringLiteralNode) item).getValue()));
            });
        }
    }

    @Test
    void serializeFieldReturnsListOfLiteralAttributeNodesForSingletonCollectionPropertyValues() {
        final Map<String, Set<String>> properties = Generator.generateProperties(true);
        final List<JsonNode> result = serializer.serializeField(field, properties);
        assertEquals(properties.size(), result.size());
        for (JsonNode node : result) {
            assertTrue(properties.containsKey(node.getName()));
            assertTrue(node instanceof StringLiteralNode);
            assertEquals(properties.get(node.getName()).iterator().next(), ((StringLiteralNode) node).getValue());
        }
    }

    @Test
    void serializeFieldSkipsEmptyPropertyValues() {
        final Map<String, Set<String>> properties = Generator.generateProperties(false);
        final String property = Generator.generateUri().toString();
        properties.put(property, Collections.emptySet());
        final List<JsonNode> result = serializer.serializeField(field, properties);
        assertEquals(properties.size() - 1, result.size());
        final Optional<JsonNode> found = result.stream().filter(n -> n.getName().equals(property)).findAny();
        assertFalse(found.isPresent());
    }

    @Test
    void serializeFieldSkipsNullValuesInCollections() {
        final Map<String, Set<String>> properties = Generator.generateProperties(false);
        final String property = Generator.generateUri().toString();
        final Set<String> values = new HashSet<>();
        values.add(Generator.generateUri().toString());
        values.add(null);
        properties.put(property, values);
        final List<JsonNode> result = serializer.serializeField(field, properties);
        final Optional<JsonNode> nodeWithNulls = result.stream().filter(n -> n.getName().equals(property)).findFirst();
        assertTrue(nodeWithNulls.isPresent());
        nodeWithNulls.ifPresent(n -> {
            assertTrue(n instanceof CollectionNode);
            final CollectionNode jsonValue = (CollectionNode) n;
            assertEquals(values.size() - 1, jsonValue.getItems().size());
            jsonValue.getItems().forEach(item -> assertTrue(values.contains(((StringLiteralNode) item).getValue())));
        });
    }

    @Test
    void serializeFieldSkipsNullPropertyValue() {
        final Map<String, Set<String>> properties = Generator.generateProperties(false);
        final String property = Generator.generateUri().toString();
        properties.put(property, null);
        final List<JsonNode> result = serializer.serializeField(field, properties);
        assertEquals(properties.size() - 1, result.size());
        final Optional<JsonNode> found = result.stream().filter(n -> n.getName().equals(property)).findAny();
        assertFalse(found.isPresent());
    }

    @Test
    void serializeFieldSerializesPropertiesWithNonCollectionValues() {
        final Map<String, Set<String>> temp = Generator.generateProperties(true);
        final Map<String, String> properties = new HashMap<>();
        temp.forEach((key, value) -> properties.put(key, value.iterator().next()));
        final List<JsonNode> result = serializer.serializeField(field, properties);
        assertEquals(properties.size(), result.size());
        for (JsonNode node : result) {
            assertTrue(properties.containsKey(node.getName()));
            assertTrue(node instanceof StringLiteralNode);
            assertEquals(properties.get(node.getName()), ((StringLiteralNode) node).getValue());
        }
    }

    @Test
    void serializeFieldSerializesTypedPropertiesToCorrectTargetTypes() {
        final Map<URI, Set<Object>> properties = new HashMap<>();
        final URI boolProperty = Generator.generateUri();
        properties.put(boolProperty, Collections.singleton(false));
        final URI intProperty = Generator.generateUri();
        properties.put(intProperty, Collections.singleton(Generator.randomCount(Integer.MAX_VALUE)));
        final URI floatProperty = Generator.generateUri();
        properties.put(floatProperty, Collections.singleton(3.14192F));
        final List<JsonNode> result = serializer.serializeField(field, properties);
        for (JsonNode node : result) {
            if (node.getName().equals(boolProperty.toString())) {
                assertTrue(node instanceof BooleanLiteralNode);
                assertFalse(((BooleanLiteralNode) node).getValue());
            } else if (node.getName().equals(intProperty.toString())) {
                assertTrue(node instanceof NumericLiteralNode);
                assertEquals(properties.get(intProperty).iterator().next(), ((NumericLiteralNode) node).getValue());
            } else {
                assertEquals(floatProperty.toString(), node.getName());
                assertEquals(properties.get(floatProperty).iterator().next(), ((NumericLiteralNode) node).getValue());
            }
        }
    }
}
