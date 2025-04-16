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
package cz.cvut.kbss.jsonld.deserialization.util;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.Properties;
import cz.cvut.kbss.jsonld.environment.TestUtil;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.*;
import cz.cvut.kbss.jsonld.exception.AmbiguousTargetTypeException;
import cz.cvut.kbss.jsonld.exception.TargetTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TargetClassResolverTest {

    private TypeMap typeMap;

    private TargetClassResolver sut;

    @BeforeEach
    void setUp() {
        this.typeMap = TestUtil.getDefaultTypeMap();
        this.sut = new TargetClassResolver(typeMap);
    }

    @Test
    void getTargetClassReturnsExpectedClassWhenSingleTypeMatchesOneClass() {
        assertEquals(Employee.class,
                sut.getTargetClass(Employee.class, Collections.singletonList(Vocabulary.EMPLOYEE)));
    }

    @Test
    void getTargetClassFiltersOutNonSubtypesOfExpectedClass() {
        typeMap.register(Vocabulary.PERSON, Organization.class);
        final Class<?> result = sut.getTargetClass(Person.class, Collections.singletonList(Vocabulary.PERSON));
        assertEquals(Person.class, result);
    }

    @Test
    void getTargetClassThrowsTargetTypeExceptionWhenMatchingTargetCandidateIsNotFound() {
        final List<String> types = Collections.singletonList(Vocabulary.ORGANIZATION);
        final TargetTypeException result = assertThrows(TargetTypeException.class,
                () -> sut.getTargetClass(Person.class, types));
        assertThat(result.getMessage(),
                containsString("Neither " + Person.class + " nor any of its subclasses matches the types " + types));
    }

    @Test
    void getTargetClassReturnsExpectedClassWhenTypeMatchesAndItIsNotInTypeMap() {
        final Class<?> result = sut
                .getTargetClass(NonMappedPerson.class, Collections.singletonList(Vocabulary.PERSON));
        assertEquals(NonMappedPerson.class, result);
    }

    @OWLClass(iri = Vocabulary.PERSON)
    private static class NonMappedPerson {
    }

    @Test
    void getTargetClassResolvesMostSpecificMatchingSubClass() {
        final Class<?> result = sut
                .getTargetClass(Person.class, Arrays.asList(Vocabulary.PERSON, Vocabulary.EMPLOYEE, Vocabulary.USER));
        assertEquals(Employee.class, result);
    }

    @Test
    void getTargetClassThrowsAmbiguousTypeExceptionWhenMultipleTargetClassCandidatesAreFound() {
        typeMap.register(Vocabulary.AGENT, MostSpecific.class);
        final List<String> types = Arrays.asList(Vocabulary.PERSON, Vocabulary.USER, Vocabulary.AGENT);
        final AmbiguousTargetTypeException result = assertThrows(AmbiguousTargetTypeException.class,
                () -> sut.getTargetClass(Person.class, types));
        assertThat(result.getMessage(),
                containsString("Object with types " + types + " matches multiple equivalent target classes: "));
    }

    @OWLClass(iri = Vocabulary.AGENT)
    private static class MostSpecific extends Person {
    }

    @Test
    void getTargetClassReturnsProvidedJavaTypeWhenNoTypesAreSpecifiedAndAssumingTargetTypeIsEnabled() {
        this.sut = new TargetClassResolver(typeMap, new TargetClassResolverConfig(true, false, false));
        final Class<?> result = sut.getTargetClass(Person.class, Collections.emptySet());
        assertEquals(Person.class, result);
    }

    @Test
    void getTargetClassReturnsOneOfMostSpecificTypesWhenOptimisticTargetTypeResolutionIsEnabled() {
        typeMap.register(Vocabulary.AGENT, MostSpecific.class);
        final List<String> types = Arrays.asList(Vocabulary.PERSON, Vocabulary.USER, Vocabulary.AGENT);
        this.sut = new TargetClassResolver(typeMap, new TargetClassResolverConfig(false, true, false));
        final Class<?> result = sut.getTargetClass(Person.class, types);
        assertThat(result, anyOf(equalTo(MostSpecific.class), equalTo(User.class)));
        assertTrue(Person.class.isAssignableFrom(result));
    }

    @Test
    void getTargetClassReturnsMatchingParentClassWhenSuperclassIsPreferredWithOptimisticTypeResolution() {
        typeMap.register(Vocabulary.AGENT, MostSpecific.class);
        final List<String> types = Arrays.asList(Vocabulary.PERSON, Vocabulary.USER, Vocabulary.AGENT);
        this.sut = new TargetClassResolver(typeMap, new TargetClassResolverConfig(false, true, true));
        final Class<?> result = sut.getTargetClass(Person.class, types);
        assertEquals(Person.class, result);
    }

    @Test
    void getTargetClassSkipsAbstractClasses() {
        typeMap.register(Vocabulary.AGENT, AbstractClass.class);
        final List<String> types = Arrays.asList(Vocabulary.PERSON, Vocabulary.USER, Vocabulary.AGENT);
        this.sut = new TargetClassResolver(typeMap, new TargetClassResolverConfig(false, false, false));
        final Class<?> result = sut.getTargetClass(Person.class, types);
        assertEquals(User.class, result);
    }

    @OWLClass(iri = Vocabulary.AGENT)
    private static abstract class AbstractClass extends Person {
    }

    @Test
    void getTargetClassReturnsClassWithPropertiesWhenOptimisticTargetTypeResolutionIsEnabled() {
        typeMap.register(Vocabulary.STUDY, StudyWithProperties.class);
        final List<String> types = Collections.singletonList(Vocabulary.STUDY);
        this.sut = new TargetClassResolver(typeMap, new TargetClassResolverConfig(false, true, false));
        final Class<?> result = sut.getTargetClass(Object.class, types);
        assertEquals(StudyWithProperties.class, result);
    }

    @SuppressWarnings("unused")
    @OWLClass(iri = Vocabulary.STUDY)
    private static class StudyWithProperties {

        @Id
        private URI id;

        @Properties
        private Map<String, Set<String>> properties;
    }
}
