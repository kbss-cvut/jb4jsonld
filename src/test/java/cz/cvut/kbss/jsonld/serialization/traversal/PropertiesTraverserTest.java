/**
 * Copyright (C) 2020 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.serialization.traversal;

import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PropertiesTraverserTest {

    private Field field;

    @Mock
    private ObjectGraphTraverser traverser;

    @InjectMocks
    private PropertiesTraverser sut;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        this.field = Person.class.getDeclaredField("properties");
    }

    @Test
    void traversePropertiesInvokesVisitAttributeForSingularPropertyValues() {
        final Map<String, Set<String>> properties = Generator.generateProperties(true);
        sut.traverseProperties(new SerializationContext<>(null, field, properties));
        for (Map.Entry<String, Set<String>> e : properties.entrySet()) {
            verify(traverser).visitAttribute(
                    new SerializationContext<>(e.getKey(), null, e.getValue().iterator().next()));
        }
    }

    @Test
    void traversePropertiesOpensCollectionsAndAddsPropertyValues() {
        final Map<String, Set<String>> properties = Generator.generateProperties(false);
        sut.traverseProperties(new SerializationContext<>(null, field, properties));
        for (Map.Entry<String, Set<String>> e : properties.entrySet()) {
            if (e.getValue().size() > 0) {
                verify(traverser).openCollection(new SerializationContext<>(e.getKey(), null, e.getValue()));
                for (String v : e.getValue()) {
                    verify(traverser).visitAttribute(new SerializationContext<>(e.getKey(), null, v));
                }
                verify(traverser).closeCollection(new SerializationContext<>(e.getKey(), null, e.getValue()));
            } else {
                verify(traverser).visitAttribute(
                        new SerializationContext<>(e.getKey(), null, e.getValue().iterator().next()));
            }
        }
    }

    @Test
    void traversePropertiesSkipsEmptyPropertyValues() {
        final Map<String, Set<String>> properties = Generator.generateProperties(false);
        final String property = Generator.generateUri().toString();
        properties.put(property, Collections.emptySet());
        sut.traverseProperties(new SerializationContext<>(null, field, properties));
        verify(traverser, never()).openCollection(new SerializationContext<>(property, null, properties.get(property)));
    }

    @Test
    void traversePropertiesSkipsNullValuesInCollections() {
        final Map<String, Set<String>> properties = Generator.generateProperties(false);
        final String property = Generator.generateUri().toString();
        final Set<String> values = new HashSet<>();
        values.add(Generator.generateUri().toString());
        values.add(null);
        properties.put(property, values);
        sut.traverseProperties(new SerializationContext<>(null, field, properties));
        final ArgumentCaptor<SerializationContext<?>> captor = ArgumentCaptor.forClass(SerializationContext.class);
        verify(traverser, atLeastOnce()).visitAttribute(captor.capture());
        assertEquals(values.stream().filter(Objects::nonNull).count(),
                captor.getAllValues().stream().filter(sc -> sc.attributeId.equals(property)).count());
    }

    @Test
    void traversePropertiesSkipsNullPropertyValue() {
        final Map<String, Set<String>> properties = Generator.generateProperties(false);
        final String property = Generator.generateUri().toString();
        properties.put(property, null);
        sut.traverseProperties(new SerializationContext<>(null, field, properties));
        verify(traverser, never()).openCollection(new SerializationContext<>(property, null, null));
    }

    @Test
    void traversePropertiesSupportsPropertiesWithNonCollectionValues() {
        final Map<String, Set<String>> temp = Generator.generateProperties(true);
        final Map<String, String> properties = new HashMap<>();
        temp.forEach((key, value) -> properties.put(key, value.iterator().next()));
        sut.traverseProperties(new SerializationContext<>(null, field, properties));
        for (Map.Entry<String, String> e : properties.entrySet()) {
            verify(traverser).visitAttribute(new SerializationContext<>(e.getKey(), null, e.getValue()));
        }
    }
}
