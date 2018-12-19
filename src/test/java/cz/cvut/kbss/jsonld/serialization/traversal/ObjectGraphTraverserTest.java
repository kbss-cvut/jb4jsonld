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
package cz.cvut.kbss.jsonld.serialization.traversal;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.Types;
import cz.cvut.kbss.jsonld.annotation.JsonLdAttributeOrder;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.*;
import cz.cvut.kbss.jsonld.exception.MissingIdentifierException;
import cz.cvut.kbss.jsonld.exception.MissingTypeInfoException;
import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ObjectGraphTraverserTest {

    @Mock
    private InstanceVisitor visitor;

    private ObjectGraphTraverser traverser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        this.traverser = new ObjectGraphTraverser();
        traverser.addVisitor(visitor);
    }

    @Test
    void traverseVisitsSerializableFieldsOfInstanceWithDataPropertiesOnly() throws Exception {
        final User user = Generator.generateUser();
        traverser.traverse(user);
        final InOrder inOrder = inOrder(visitor);
        // Verify that openInstance was called before visitField
        inOrder.verify(visitor).openInstance(user);
        inOrder.verify(visitor, atLeastOnce()).visitField(any(Field.class), any());
        inOrder.verify(visitor).closeInstance(user);

        verifyUserFieldsVisited(user);
    }

    private void verifyUserFieldsVisited(User user) throws NoSuchFieldException {
        verify(visitor).visitIdentifier(user.getUri().toString(), user);
        verify(visitor).visitField(Person.class.getDeclaredField("firstName"), user.getFirstName());
        verify(visitor).visitField(Person.class.getDeclaredField("lastName"), user.getLastName());
        verify(visitor).visitField(User.class.getDeclaredField("username"), user.getUsername());
    }

    @Test
    void traverseTraversesObjectReferences() throws Exception {
        final Employee employee = Generator.generateEmployee();
        traverser.traverse(employee);
        final InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).openInstance(employee);
        inOrder.verify(visitor).openInstance(employee.getEmployer());
        inOrder.verify(visitor).closeInstance(employee.getEmployer());
        inOrder.verify(visitor).closeInstance(employee);

        verifyUserFieldsVisited(employee);
        verify(visitor).visitField(Employee.class.getDeclaredField("employer"), employee.getEmployer());
        verify(visitor).visitIdentifier(employee.getEmployer().getUri().toString(), employee.getEmployer());
        verify(visitor).visitField(Organization.class.getDeclaredField("dateCreated"),
                employee.getEmployer().getDateCreated());
        verify(visitor).visitField(Organization.class.getDeclaredField("name"), employee.getEmployer().getName());
    }

    @Test
    void traverseTraversesObjectPropertyCollection() {
        final Organization org = Generator.generateOrganization();
        generateEmployees(org);
        traverser.traverse(org);

        final InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).openInstance(org);
        inOrder.verify(visitor).openCollection(org.getEmployees());
        inOrder.verify(visitor).closeCollection(org.getEmployees());
        inOrder.verify(visitor).closeInstance(org);

        verify(visitor).openInstance(org);
        org.getEmployees().forEach(e -> verify(visitor).openInstance(e));
    }

    private void generateEmployees(Organization org) {
        org.setEmployees(new HashSet<>());
        for (int i = 0; i < Generator.randomCount(10); i++) {
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

        verifyUserFieldsVisited(employee);
        verify(visitor).openInstance(employee.getEmployer());
        verify(visitor).openCollection(employee.getEmployer().getEmployees());
        verify(visitor).visitKnownInstance(employee.getUri().toString(), employee);
        verify(visitor).closeCollection(employee.getEmployer().getEmployees());
        verify(visitor).closeInstance(employee.getEmployer());
    }

    @Test
    void traverseOnCollectionOpensCollectionAddsInstancesAndClosesCollection() {
        final Set<User> users = Generator.generateUsers();
        traverser.traverse(users);

        final InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).openCollection(users);
        for (User u : users) {
            inOrder.verify(visitor).openInstance(u);
            inOrder.verify(visitor).closeInstance(u);
        }
        inOrder.verify(visitor).closeCollection(users);
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
    private class EmployeeWithUriEmployer extends User {

        @OWLObjectProperty(iri = Vocabulary.IS_MEMBER_OF)
        private URI employer;
    }

    @Test
    void traverseUsesAttributePropertyOrderingInformationWhenSerializingObject() throws Exception {
        final Study study = generateStudy();

        traverser.traverse(study);
        final InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).visitIdentifier(study.getUri().toString(), study);
        inOrder.verify(visitor).visitField(Study.class.getDeclaredField("name"), study.getName());
        inOrder.verify(visitor).visitField(Study.class.getDeclaredField("participants"), study.getParticipants());
        inOrder.verify(visitor).visitField(Study.class.getDeclaredField("members"), study.getMembers());
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
        inOrder.verify(visitor).visitField(Study.class.getDeclaredField("participants"), ps.getParticipants());
        inOrder.verify(visitor).visitField(Study.class.getDeclaredField("members"), ps.getMembers());
        inOrder.verify(visitor).visitField(Study.class.getDeclaredField("name"), ps.getName());
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
    void traverseInvokesVisitIdentifierWithInstanceIdentifier() {
        final Person person = Generator.generatePerson();
        traverser.traverse(person);
        final InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).openInstance(person);
        inOrder.verify(visitor).visitIdentifier(person.getUri().toString(), person);
    }

    @Test
    void traverseInvokesVisitWhenInstanceIsPlainIdentifierPropertyValue() throws Exception {
        final EmployeeWithUriEmployer employee = new EmployeeWithUriEmployer();
        employee.setUri(Generator.generateUri());
        employee.employer = Generator.generateUri();

        traverser.traverse(employee);
        final InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).openInstance(employee);
        inOrder.verify(visitor)
               .visitField(EmployeeWithUriEmployer.class.getDeclaredField("employer"), employee.employer);
        inOrder.verify(visitor).openInstance(employee.employer);
        inOrder.verify(visitor).visitIdentifier(employee.employer.toString(), employee.employer);
    }

    @Test
    void traverseInvokesVisitTypesAfterOpeningInstance() {
        final Employee employee = Generator.generateEmployee();
        traverser.traverse(employee);

        final InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).openInstance(employee);
        inOrder.verify(visitor).visitTypes(new InstanceTypeResolver().resolveTypes(employee), employee);
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
    void traverseSupportsInstanceOfClassWithoutOWLClassAnnotationButWithNonEmptyTypes() {
        final NoType instance = new NoType();
        instance.uri = Generator.generateUri();
        instance.types = Collections.singleton(Vocabulary.PERSON);
        traverser.traverse(instance);
        verify(visitor).visitTypes(instance.types, instance);
    }

    private static class NoType {
        @Id
        private URI uri;

        @Types
        private Set<String> types;
    }
}