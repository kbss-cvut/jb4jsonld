package cz.cvut.kbss.jsonld.deserialization.util;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
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
        this.typeMap = initDefaultTypeMap();
        this.resolver = new TargetClassResolver(typeMap);
    }

    private TypeMap initDefaultTypeMap() {
        final TypeMap tm = new TypeMap();
        tm.register(Vocabulary.EMPLOYEE, Employee.class);
        tm.register(Vocabulary.ORGANIZATION, Organization.class);
        tm.register(Vocabulary.PERSON, Person.class);
        tm.register(Vocabulary.STUDY, Study.class);
        tm.register(Vocabulary.USER, User.class);
        return tm;
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