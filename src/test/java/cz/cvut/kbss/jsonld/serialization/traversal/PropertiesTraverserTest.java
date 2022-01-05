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
package cz.cvut.kbss.jsonld.serialization.traversal;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.Types;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.environment.model.PersonWithTypedProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PropertiesTraverserTest {

    private Field field;

    private Field typedField;

    private String property;

    @Mock
    private ObjectGraphTraverser traverser;

    @InjectMocks
    private PropertiesTraverser sut;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        this.field = Person.class.getDeclaredField("properties");
        this.typedField = PersonWithTypedProperties.class.getDeclaredField("properties");
        this.property = Generator.generateUri().toString();
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
                    verify(traverser).visitAttribute(new SerializationContext<>(null, null, v));
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
        properties.put(property, Collections.emptySet());
        sut.traverseProperties(new SerializationContext<>(null, field, properties));
        verify(traverser, never()).openCollection(new SerializationContext<>(property, null, properties.get(property)));
    }

    @Test
    void traversePropertiesSkipsNullValuesInCollections() {
        final Map<String, Set<String>> properties = Generator.generateProperties(false);
        final Set<String> values = new HashSet<>();
        values.add(Generator.generateUri().toString());
        values.add(null);
        properties.put(property, values);
        sut.traverseProperties(new SerializationContext<>(null, field, properties));
        final ArgumentCaptor<SerializationContext<?>> captor = ArgumentCaptor.forClass(SerializationContext.class);
        verify(traverser, atLeastOnce()).visitAttribute(captor.capture());
        assertEquals(values.stream().filter(Objects::nonNull).count(),
                captor.getAllValues().stream().filter(sc -> values.contains(sc.getValue())).count());
    }

    @Test
    void traversePropertiesSkipsNullPropertyValue() {
        final Map<String, Set<String>> properties = Generator.generateProperties(false);
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

    @Test
    void traversePropertiesDoesNothingForSingleNullPropertyValue() {
        final Map<String, Set<String>> properties = new HashMap<>();
        properties.put(property, Collections.singleton(null));
        sut.traverseProperties(new SerializationContext<>(null, field, properties));
        verify(traverser, never()).openCollection(any());
        verify(traverser, never()).visitAttribute(any());
    }

    @Test
    void traverseTypedPropertiesProcessesSingleInstanceIdentifierAsObject() throws Exception {
        final Map<URI, Set<Object>> properties = new HashMap<>();
        final URI value = Generator.generateUri();
        properties.put(URI.create(property), Collections.singleton(value));
        sut.traverseProperties(new SerializationContext<>(null, typedField, properties));
        verify(traverser).traverseSingular(new SerializationContext<>(property, null, value));
    }

    @Test
    void traverseTypedPropertiesProcessesInstanceIdentifiersAsObjects() throws Exception {
        final Map<URI, Set<Object>> properties = new HashMap<>();
        final URI valueOne = Generator.generateUri();
        final URL valueTwo = Generator.generateUri().toURL();
        properties.put(URI.create(property), new HashSet<>(Arrays.asList(valueOne, valueTwo)));
        sut.traverseProperties(new SerializationContext<>(null, typedField, properties));
        verify(traverser)
                .openCollection(new SerializationContext<>(property, null, properties.get(URI.create(property))));
        verify(traverser).traverseSingular(new SerializationContext<>(null, null, valueOne));
        verify(traverser).traverseSingular(new SerializationContext<>(null, null, valueTwo));
        verify(traverser)
                .closeCollection(new SerializationContext<>(property, null, properties.get(URI.create(property))));
    }

    @Test
    void traverseTypedPropertiesProcessesObjectInPropertiesAsObject() throws Exception {
        final Map<URI, Set<Object>> properties = new HashMap<>();
        final Person value = Generator.generatePerson();
        properties.put(URI.create(property), Collections.singleton(value));
        sut.traverseProperties(new SerializationContext<>(null, typedField, properties));
        verify(traverser).traverseSingular(new SerializationContext<>(property, null, value));
    }

    @Test
    void traverseTypedPropertiesProcessesObjectsInPropertiesAsObjects() throws Exception {
        final Map<URI, Set<Object>> properties = new HashMap<>();
        final Person valueOne = Generator.generatePerson();
        final Organization valueTwo = Generator.generateOrganization();
        properties.put(URI.create(property), new HashSet<>(Arrays.asList(valueOne, valueTwo)));
        sut.traverseProperties(new SerializationContext<>(null, typedField, properties));
        verify(traverser)
                .openCollection(new SerializationContext<>(property, null, properties.get(URI.create(property))));
        verify(traverser).traverseSingular(new SerializationContext<>(null, null, valueOne));
        verify(traverser).traverseSingular(new SerializationContext<>(null, null, valueTwo));
        verify(traverser)
                .closeCollection(new SerializationContext<>(property, null, properties.get(URI.create(property))));
    }

    @Test
    void traverseTypedPropertiesProcessesObjectWithTypesAsObject() throws Exception {
        final Map<URI, Set<Object>> properties = new HashMap<>();
        final ClassWithTypes value = new ClassWithTypes();
        value.id = Generator.generateUri();
        value.types = Collections.singleton(Vocabulary.AGENT);
        properties.put(URI.create(property), Collections.singleton(value));
        sut.traverseProperties(new SerializationContext<>(null, typedField, properties));
        verify(traverser).traverseSingular(new SerializationContext<>(property, null, value));
    }

    static class ClassWithTypes {
        @Id
        private URI id;

        @Types
        private Set<String> types;
    }
}
