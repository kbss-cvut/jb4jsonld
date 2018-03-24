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
package cz.cvut.kbss.jsonld.deserialization.util;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jsonld.environment.TestUtil;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.*;
import cz.cvut.kbss.jsonld.exception.AmbiguousTargetTypeException;
import cz.cvut.kbss.jsonld.exception.TargetTypeException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;

public class TargetClassResolverTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private TypeMap typeMap;

    private TargetClassResolver resolver;

    @Before
    public void setUp() {
        this.typeMap = TestUtil.getDefaultTypeMap();
        this.resolver = new TargetClassResolver(typeMap);
    }

    @Test
    public void getTargetClassReturnsExpectedClassWhenSingleTypeMatchesOneClass() {
        assertEquals(Employee.class,
                resolver.getTargetClass(Employee.class, Collections.singletonList(Vocabulary.EMPLOYEE)));
    }

    @Test
    public void getTargetClassFiltersOutNonSubtypesOfExpectedClass() {
        typeMap.register(Vocabulary.PERSON, Organization.class);
        final Class<?> result = resolver.getTargetClass(Person.class, Collections.singletonList(Vocabulary.PERSON));
        assertEquals(Person.class, result);
    }

    @Test
    public void getTargetClassThrowsTargetTypeExceptionWhenMatchingTargetCandidateIsNotFound() {
        final List<String> types = Collections.singletonList(Vocabulary.ORGANIZATION);
        thrown.expect(TargetTypeException.class);
        thrown.expectMessage("Neither " + Person.class + " nor any of its subclasses matches the types " + types + ".");
        resolver.getTargetClass(Person.class, types);
    }

    @Test
    public void getTargetClassReturnsExpectedClassWhenTypeMatchesAndItIsNotInTypeMap() {
        final Class<?> result = resolver
                .getTargetClass(NonMappedPerson.class, Collections.singletonList(Vocabulary.PERSON));
        assertEquals(NonMappedPerson.class, result);
    }

    @OWLClass(iri = Vocabulary.PERSON)
    private static class NonMappedPerson {
    }

    @Test
    public void getTargetClassResolvesMostSpecificMatchingSubClass() {
        final Class<?> result = resolver
                .getTargetClass(Person.class, Arrays.asList(Vocabulary.PERSON, Vocabulary.EMPLOYEE, Vocabulary.USER));
        assertEquals(Employee.class, result);
    }

    @Test
    public void getTargetClassThrowsAmbiguousTypeExceptionWhenMultipleTargetClassCandidatesAreFound() {
        typeMap.register(Vocabulary.AGENT, MostSpecific.class);
        thrown.expect(AmbiguousTargetTypeException.class);
        final List<String> types = Arrays.asList(Vocabulary.PERSON, Vocabulary.USER, Vocabulary.AGENT);
        thrown.expectMessage(
                containsString("Object with types " + types + " matches multiple equivalent target classes: "));
        resolver.getTargetClass(Person.class, types);
    }

    @OWLClass(iri = Vocabulary.AGENT)
    private static class MostSpecific extends Person {
    }
}