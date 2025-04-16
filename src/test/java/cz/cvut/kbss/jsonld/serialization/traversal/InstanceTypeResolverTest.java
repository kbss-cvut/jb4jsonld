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
package cz.cvut.kbss.jsonld.serialization.traversal;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.Types;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.environment.model.User;
import cz.cvut.kbss.jsonld.exception.BeanProcessingException;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class InstanceTypeResolverTest {

    private final InstanceTypeResolver resolver = new InstanceTypeResolver();

    @Test
    void resolveTypesGetsOWLClassAnnotationValue() {
        final Person p = Generator.generatePerson();
        final Set<String> types = resolver.resolveTypes(p);
        assertEquals(1, types.size());
        assertTrue(types.contains(Vocabulary.PERSON));
    }

    @Test
    void resolveTypesGetsOWLClassValuesFromAncestorsAsWell() {
        final User u = Generator.generateUser();
        final Set<String> types = resolver.resolveTypes(u);
        assertEquals(2, types.size());
        assertTrue(types.contains(Vocabulary.PERSON));
        assertTrue(types.contains(Vocabulary.USER));
    }

    @Test
    void resolveTypesExtractsValueOfTypesField() {
        final User u = Generator.generateUser();
        u.setTypes(Collections.singleton(Vocabulary.ORGANIZATION));
        final Set<String> types = resolver.resolveTypes(u);
        assertTrue(types.contains(Vocabulary.ORGANIZATION));
    }

    @Test
    void resolveTypesExtractsTypesFieldValueFromSuperclass() {
        final Employee employee = Generator.generateEmployee();
        employee.setTypes(Collections.singleton(Vocabulary.ORGANIZATION));
        final Set<String> types = resolver.resolveTypes(employee);
        assertTrue(types.contains(Vocabulary.ORGANIZATION));
    }

    @Test
    void resolveTypesCombinesDeclaredTypesWithTypesFieldValue() {
        final Employee employee = Generator.generateEmployee();
        employee.setTypes(Collections.singleton(Vocabulary.ORGANIZATION));
        final Set<String> types = resolver.resolveTypes(employee);
        assertTrue(types.contains(Vocabulary.ORGANIZATION));
        assertTrue(types.contains(Vocabulary.PERSON));
        assertTrue(types.contains(Vocabulary.USER));
        assertTrue(types.contains(Vocabulary.EMPLOYEE));
    }

    @Test
    void resolveTypesHandlesNonStringTypesValue() {
        final UriTypes u = new UriTypes();
        u.types = Collections.singleton(URI.create(Vocabulary.USER));
        final Set<String> types = resolver.resolveTypes(u);
        assertTrue(types.contains(Vocabulary.USER));
        assertTrue(types.contains(Vocabulary.PERSON));
    }

    @SuppressWarnings("unused")
    @OWLClass(iri = Vocabulary.PERSON)
    private static class UriTypes {

        @Types
        private Set<URI> types;
    }

    @Test
    void throwsBeanProcessingExceptionWhenTypesFieldIsNotCollection() {
        final InvalidTypes instance = new InvalidTypes();
        final BeanProcessingException result = assertThrows(BeanProcessingException.class,
                () -> resolver.resolveTypes(instance));
        assertThat(result.getMessage(), containsString("@Types field"));
        assertThat(result.getMessage(), containsString("collection"));
    }

    @SuppressWarnings("unused")
    @OWLClass(iri = Vocabulary.PERSON)
    private static class InvalidTypes {

        @Types
        private String types;
    }
}