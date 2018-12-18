/**
 * Copyright (C) 2017 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.deserialization.util;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jsonld.environment.TestUtil;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.exception.AmbiguousTargetTypeException;
import cz.cvut.kbss.jsonld.exception.TargetTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TargetClassResolverTest {

    private TypeMap typeMap;

    private TargetClassResolver resolver;

    @BeforeEach
    void setUp() {
        this.typeMap = TestUtil.getDefaultTypeMap();
        this.resolver = new TargetClassResolver(typeMap);
    }

    @Test
    void getTargetClassReturnsExpectedClassWhenSingleTypeMatchesOneClass() {
        assertEquals(Employee.class,
                resolver.getTargetClass(Employee.class, Collections.singletonList(Vocabulary.EMPLOYEE)));
    }

    @Test
    void getTargetClassFiltersOutNonSubtypesOfExpectedClass() {
        typeMap.register(Vocabulary.PERSON, Organization.class);
        final Class<?> result = resolver.getTargetClass(Person.class, Collections.singletonList(Vocabulary.PERSON));
        assertEquals(Person.class, result);
    }

    @Test
    void getTargetClassThrowsTargetTypeExceptionWhenMatchingTargetCandidateIsNotFound() {
        final List<String> types = Collections.singletonList(Vocabulary.ORGANIZATION);
        final TargetTypeException result = assertThrows(TargetTypeException.class,
                () -> resolver.getTargetClass(Person.class, types));
        assertThat(result.getMessage(),
                containsString("Neither " + Person.class + " nor any of its subclasses matches the types " + types));
    }

    @Test
    void getTargetClassReturnsExpectedClassWhenTypeMatchesAndItIsNotInTypeMap() {
        final Class<?> result = resolver
                .getTargetClass(NonMappedPerson.class, Collections.singletonList(Vocabulary.PERSON));
        assertEquals(NonMappedPerson.class, result);
    }

    @OWLClass(iri = Vocabulary.PERSON)
    private static class NonMappedPerson {
    }

    @Test
    void getTargetClassResolvesMostSpecificMatchingSubClass() {
        final Class<?> result = resolver
                .getTargetClass(Person.class, Arrays.asList(Vocabulary.PERSON, Vocabulary.EMPLOYEE, Vocabulary.USER));
        assertEquals(Employee.class, result);
    }

    @Test
    void getTargetClassThrowsAmbiguousTypeExceptionWhenMultipleTargetClassCandidatesAreFound() {
        typeMap.register(Vocabulary.AGENT, MostSpecific.class);
        final List<String> types = Arrays.asList(Vocabulary.PERSON, Vocabulary.USER, Vocabulary.AGENT);
        final AmbiguousTargetTypeException result = assertThrows(AmbiguousTargetTypeException.class,
                () -> resolver.getTargetClass(Person.class, types));
        assertThat(result.getMessage(),
                containsString("Object with types " + types + " matches multiple equivalent target classes: "));
    }

    @OWLClass(iri = Vocabulary.AGENT)
    private static class MostSpecific extends Person {
    }
}