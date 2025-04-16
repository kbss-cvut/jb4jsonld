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

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.Types;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.annotation.JsonLdAttributeOrder;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.*;
import cz.cvut.kbss.jsonld.exception.MissingIdentifierException;
import cz.cvut.kbss.jsonld.exception.MissingTypeInfoException;
import cz.cvut.kbss.jsonld.serialization.context.DummyJsonLdContext;
import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObjectGraphTraverserTest {

    @Mock
    private InstanceVisitor visitor;

    private ObjectGraphTraverser traverser;

    @BeforeEach
    void setUp() {
        this.traverser = new ObjectGraphTraverser(new SerializationContextFactory(DummyJsonLdContext.INSTANCE));
        traverser.setVisitor(visitor);
        when(visitor.visitObject(any())).thenReturn(true);
    }

    @Test
    void traverseVisitsSerializableFieldsOfInstanceWithDataPropertiesOnly() throws Exception {
        final User user = Generator.generateUser();
        traverser.traverse(user);
        final InOrder inOrder = inOrder(visitor);
        // Verify that openInstance was called before visitAttribute
        inOrder.verify(visitor).openObject(ctx(null, null, user));
        inOrder.verify(visitor, atLeastOnce()).visitAttribute(any(SerializationContext.class));
        inOrder.verify(visitor).closeObject(ctx(null, null, user));

        verifyUserFieldsVisited(user);
    }

    private static <T> SerializationContext<T> ctx(String attId, Field field, T value) {
        return new SerializationContext<>(attId, field, value, DummyJsonLdContext.INSTANCE);
    }

    private void verifyUserFieldsVisited(User user) throws NoSuchFieldException {
        verify(visitor).visitIdentifier(
                new SerializationContext<>(JsonLd.ID, Person.class.getDeclaredField("uri"), user.getUri().toString(),
                                           DummyJsonLdContext.INSTANCE));
        verify(visitor).visitAttribute(ctx(Vocabulary.FIRST_NAME, Person.getFirstNameField(), user.getFirstName()));
        verify(visitor).visitAttribute(ctx(Vocabulary.LAST_NAME, Person.getLastNameField(), user.getLastName()));
        verify(visitor).visitAttribute(ctx(Vocabulary.USERNAME, User.getUsernameField(), user.getUsername()));
    }

    private void generateEmployees(Organization org) {
        org.setEmployees(new HashSet<>());
        for (int i = 0; i < Generator.randomInt(10); i++) {
            final Employee emp = new Employee();
            emp.setUri(Generator.generateUri());
            org.getEmployees().add(emp);
        }
    }

    @Test
    void traverseRecognizesAlreadyVisitedInstances() throws Exception {
        final Employee employee = Generator.generateEmployee();
        // Backward reference
        employee.getEmployer().setEmployees(Collections.singleton(employee));
        traverser.traverse(employee);
        traverser.traverse(ctx(Vocabulary.HAS_MEMBER, Organization.getEmployeesField(), employee));

        verify(visitor).openObject(ctx(null, null, employee));
        verify(visitor).openObject(ctx(Vocabulary.HAS_MEMBER, Organization.getEmployeesField(), employee));
        verify(visitor).visitTypes(new SerializationContext<>(JsonLd.TYPE, User.class.getDeclaredField("types"),
                                                              new HashSet<>(
                                                                      Arrays.asList(Vocabulary.PERSON, Vocabulary.USER,
                                                                                    Vocabulary.EMPLOYEE)),
                                                              DummyJsonLdContext.INSTANCE));
    }

    @Test
    void traverseOnCollectionOpensCollectionAddsInstancesAndClosesCollection() {
        final Set<User> users = Generator.generateUsers();
        traverser.traverse(users);

        final InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).openCollection(ctx(null, null, users));
        for (User u : users) {
            inOrder.verify(visitor).openObject(ctx(null, null, u));
            inOrder.verify(visitor).closeObject(ctx(null, null, u));
        }
        inOrder.verify(visitor).closeCollection(ctx(null, null, users));
    }

    @Test
    void traverseDoesNotPutPlainIdentifierObjectPropertyValueIntoKnownInstances() throws Exception {
        final EmployeeWithUriEmployer employee = new EmployeeWithUriEmployer();
        employee.setUri(Generator.generateUri());
        employee.employer = Generator.generateUri();

        traverser.traverse(employee);
        final Map<Object, String> knownInstances = getKnownInstances();
        assertFalse(knownInstances.containsKey(employee.employer));
    }

    private Map<Object, String> getKnownInstances() throws NoSuchFieldException, IllegalAccessException {
        final Field knownField = ObjectGraphTraverser.class.getDeclaredField("knownInstances");
        knownField.setAccessible(true);
        return (Map<Object, String>) knownField.get(traverser);
    }

    @OWLClass(iri = Vocabulary.EMPLOYEE)
    private static class EmployeeWithUriEmployer extends User {

        @OWLObjectProperty(iri = Vocabulary.IS_MEMBER_OF)
        private URI employer;
    }

    @Test
    void traverseUsesAttributePropertyOrderingInformationWhenSerializingObject() throws Exception {
        final Study study = generateStudy();

        traverser.traverse(study);
        final InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor)
               .visitIdentifier(ctx(JsonLd.ID, Study.class.getDeclaredField("uri"), study.getUri().toString()));
        inOrder.verify(visitor).visitAttribute(ctx(RDFS.LABEL, Study.class.getDeclaredField("name"), study.getName()));
        inOrder.verify(visitor).visitAttribute(
                ctx(Vocabulary.HAS_PARTICIPANT, Study.class.getDeclaredField("participants"), study.getParticipants()));
        inOrder.verify(visitor)
               .visitAttribute(ctx(Vocabulary.HAS_MEMBER, Study.class.getDeclaredField("members"), study.getMembers()));
    }

    private Study generateStudy() {
        final Study study = new Study();
        study.setUri(Generator.generateUri());
        study.setName("TestStudy");
        study.setMembers(Collections.singleton(Generator.generateEmployee()));
        study.setParticipants(Collections.singleton(Generator.generateEmployee()));
        return study;
    }

    @Test
    void traverseUsesPartialAttributePropertyOrderingWhenSerializingObject() throws Exception {
        final PartiallyOrderedStudy ps = new PartiallyOrderedStudy();
        ps.setUri(Generator.generateUri());
        ps.setName("TestStudy");
        ps.setMembers(Collections.singleton(Generator.generateEmployee()));
        ps.setParticipants(Collections.singleton(Generator.generateEmployee()));

        traverser.traverse(ps);
        final InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).visitAttribute(
                ctx(Vocabulary.HAS_PARTICIPANT, Study.class.getDeclaredField("participants"), ps.getParticipants()));
        inOrder.verify(visitor)
               .visitAttribute(ctx(Vocabulary.HAS_MEMBER, Study.class.getDeclaredField("members"), ps.getMembers()));
        inOrder.verify(visitor).visitAttribute(ctx(RDFS.LABEL, Study.class.getDeclaredField("name"), ps.getName()));
    }

    @JsonLdAttributeOrder({"participants", "members"})
    private static class PartiallyOrderedStudy extends Study {
    }

    @Test
    void traversePutsVisitedInstanceTogetherWithIdentifierIntoKnownInstancesOnOpeningObject() throws Exception {
        final Person person = Generator.generatePerson();
        traverser.traverse(person);
        final Map<Object, String> knownInstances = getKnownInstances();
        assertTrue(knownInstances.containsKey(person));
        assertEquals(person.getUri().toString(), knownInstances.get(person));
    }

    @Test
    void traverseGeneratesBlankNodeIdentifierWhenPuttingInstanceWithoutIdentifierIntoKnownInstances() throws
            Exception {
        final Person person = Generator.generatePerson();
        person.setUri(null);
        traverser.traverse(person);
        final Map<Object, String> knownInstances = getKnownInstances();
        assertTrue(knownInstances.containsKey(person));
        assertThat(knownInstances.get(person), StringStartsWith.startsWith("_:"));
    }

    @Test
    void traverseInvokesVisitIdentifierWithInstanceIdentifier() throws Exception {
        final Person person = Generator.generatePerson();
        traverser.traverse(person);
        final InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).openObject(ctx(null, null, person));
        inOrder.verify(visitor)
               .visitIdentifier(ctx(JsonLd.ID, Person.class.getDeclaredField("uri"), person.getUri().toString()));
    }

    @Test
    void traverseInvokesVisitIndividualWhenInstanceIsPlainIdentifierPropertyValue() throws Exception {
        final EmployeeWithUriEmployer employee = new EmployeeWithUriEmployer();
        employee.setUri(Generator.generateUri());
        employee.employer = Generator.generateUri();

        final SerializationContext<URI> ctx = ctx(Vocabulary.IS_MEMBER_OF, EmployeeWithUriEmployer.class.getDeclaredField("employer"),
                                                  employee.employer);
        traverser.traverse(ctx);
        verify(visitor).visitIndividual(ctx);
        verify(visitor, never()).visitAttribute(any());
        verify(visitor, never()).visitTypes(any());
    }

    @Test
    void traverseInvokesVisitTypesAfterOpeningInstance() throws Exception {
        final Employee employee = Generator.generateEmployee();
        traverser.traverse(employee);

        final InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).openObject(ctx(null, null, employee));
        inOrder.verify(visitor).visitTypes(ctx(JsonLd.TYPE, User.class.getDeclaredField("types"),
                                               new InstanceTypeResolver().resolveTypes(employee)));
    }

    @Test
    void traverseThrowsMissingIdentifierExceptionWhenIdentifierIsRequiredAndMissing() {
        final Person person = Generator.generatePerson();
        person.setUri(null);
        traverser.setRequireId(true);
        assertThrows(MissingIdentifierException.class, () -> traverser.traverse(person));
    }

    @Test
    void traverseThrowsMissingTypeInfoExceptionWhenObjectHasNoTypesAndIsNotOwlClass() {
        final NoType instance = new NoType();
        instance.uri = Generator.generateUri();
        final MissingTypeInfoException result = assertThrows(MissingTypeInfoException.class,
                                                             () -> traverser.traverse(instance));
        assertThat(result.getMessage(), containsString("@OWLClass"));
        assertThat(result.getMessage(), containsString("@Types"));
    }

    @Test
    void traverseSupportsInstanceOfClassWithoutOWLClassAnnotationButWithNonEmptyTypes() throws Exception {
        final NoType instance = new NoType();
        instance.uri = Generator.generateUri();
        instance.types = Collections.singleton(Vocabulary.PERSON);
        traverser.traverse(instance);
        verify(visitor).visitTypes(ctx(JsonLd.TYPE, NoType.class.getDeclaredField("types"), instance.types));
    }

    private static class NoType {
        @Id
        private URI uri;

        @Types
        private Set<String> types;
    }

    @Test
    void traverseSkipsNullValuesInCollectionAttribute() {
        final Organization org = Generator.generateOrganization();
        generateEmployees(org);
        org.getEmployees().add(null);
        traverser.traverse(org.getEmployees());

        final InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).openCollection(ctx(null, null, org.getEmployees()));
        inOrder.verify(visitor).closeCollection(ctx(null, null, org.getEmployees()));

        org.getEmployees().stream().filter(Objects::nonNull)
           .forEach(e -> verify(visitor).openObject(ctx(null, null, e)));
    }

    @Test
    void traverseSkipsNullValuesInCollection() {
        final Set<User> users = Generator.generateUsers();
        users.add(null);
        traverser.traverse(users);

        final InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).openCollection(ctx(null, null, users));
        for (User u : users) {
            if (u != null) {
                inOrder.verify(visitor).openObject(ctx(null, null, u));
                inOrder.verify(visitor).closeObject(ctx(null, null, u));
            }
        }
        inOrder.verify(visitor).closeCollection(ctx(null, null, users));
    }

    @Test
    void traverseInvokesVisitObjectBeforeOpeningIt() {
        final Person p = Generator.generatePerson();
        traverser.traverse(p);
        final InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).visitObject(ctx(null, null, p));
        inOrder.verify(visitor).openObject(ctx(null, null, p));
    }

    @Test
    void traverseDoesNotOpenObjectWhenVisitObjectReturnedFalse() {
        when(visitor.visitObject(any())).thenReturn(false);
        final Person p = Generator.generatePerson();
        traverser.traverse(p);
        verify(visitor).visitObject(ctx(null, null, p));
        verify(visitor, never()).openObject(any());
    }

    @Test
    void traverseSingularEnumConstantVisitsIndividual() {
        final SerializationContext<OwlPropertyType> ctx =
                new SerializationContext<>(OwlPropertyType.OBJECT_PROPERTY, DummyJsonLdContext.INSTANCE);
        traverser.traverse(ctx);
        verify(visitor).visitIndividual(ctx);
        verify(visitor, never()).visitAttribute(any());
        verify(visitor, never()).visitTypes(any());
    }
}
