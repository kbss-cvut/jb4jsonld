/**
 * Copyright (C) 2017 Czech Technical University in Prague
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

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.Types;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.environment.model.User;
import cz.cvut.kbss.jsonld.exception.BeanProcessingException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InstanceTypeResolverTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final InstanceTypeResolver resolver = new InstanceTypeResolver();

    @Test
    public void resolveTypesGetsOWLClassAnnotationValue() {
        final Person p = Generator.generatePerson();
        final Set<String> types = resolver.resolveTypes(p);
        assertEquals(1, types.size());
        assertTrue(types.contains(Vocabulary.PERSON));
    }

    @Test
    public void resolveTypesGetsOWLClassValuesFromAncestorsAsWell() {
        final User u = Generator.generateUser();
        final Set<String> types = resolver.resolveTypes(u);
        assertEquals(2, types.size());
        assertTrue(types.contains(Vocabulary.PERSON));
        assertTrue(types.contains(Vocabulary.USER));
    }

    @Test
    public void resolveTypesExtractsValueOfTypesField() {
        final User u = Generator.generateUser();
        u.setTypes(Collections.singleton(Vocabulary.ORGANIZATION));
        final Set<String> types = resolver.resolveTypes(u);
        assertTrue(types.contains(Vocabulary.ORGANIZATION));
    }

    @Test
    public void resolveTypesExtractsTypesFieldValueFromSuperclass() {
        final Employee employee = Generator.generateEmployee();
        employee.setTypes(Collections.singleton(Vocabulary.ORGANIZATION));
        final Set<String> types = resolver.resolveTypes(employee);
        assertTrue(types.contains(Vocabulary.ORGANIZATION));
    }

    @Test
    public void resolveTypesCombinesDeclaredTypesWithTypesFieldValue() {
        final Employee employee = Generator.generateEmployee();
        employee.setTypes(Collections.singleton(Vocabulary.ORGANIZATION));
        final Set<String> types = resolver.resolveTypes(employee);
        assertTrue(types.contains(Vocabulary.ORGANIZATION));
        assertTrue(types.contains(Vocabulary.PERSON));
        assertTrue(types.contains(Vocabulary.USER));
        assertTrue(types.contains(Vocabulary.EMPLOYEE));
    }

    @Test
    public void resolveTypesHandlesNonStringTypesValue() {
        final UriTypes u = new UriTypes();
        u.types = Collections.singleton(URI.create(Vocabulary.USER));
        final Set<String> types = resolver.resolveTypes(u);
        assertTrue(types.contains(Vocabulary.USER));
        assertTrue(types.contains(Vocabulary.PERSON));
    }

    @OWLClass(iri = Vocabulary.PERSON)
    private static class UriTypes {

        @Types
        private Set<URI> types;
    }

    @Test
    public void throwsBeanProcessingExceptionWhenTypesFieldIsNotCollection() {
        thrown.expect(BeanProcessingException.class);
        thrown.expectMessage(containsString("@Types field"));
        thrown.expectMessage(containsString("collection"));
        final InvalidTypes instance = new InvalidTypes();
        resolver.resolveTypes(instance);
    }

    @OWLClass(iri = Vocabulary.PERSON)
    private static class InvalidTypes {

        @Types
        private String types;
    }
}