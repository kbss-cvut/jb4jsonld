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
package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.*;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

class SingularObjectContextTest {

    @Test
    void setFieldValueSetsFieldValueOnInstance() throws Exception {
        final SingularObjectContext<Person> ctx = new SingularObjectContext<>(new Person(),
                BeanAnnotationProcessor.mapFieldsForDeserialization(
                        Person.class), Collections.emptyMap());
        final String testFirstName = "John";
        ctx.setFieldValue(Person.class.getDeclaredField("firstName"), testFirstName);
        assertEquals(testFirstName, ctx.getInstance().getFirstName());
    }

    @Test
    void setFieldValueThrowsDeserializationExceptionWhenInvalidTypeIsUsedAsFieldValue() throws Exception {
        final Integer invalidValue = 117;
        final SingularObjectContext<Person> ctx = new SingularObjectContext<>(new User(),
                BeanAnnotationProcessor.mapFieldsForDeserialization(
                        User.class), Collections.emptyMap());
        JsonLdDeserializationException result = assertThrows(JsonLdDeserializationException.class,
                () -> ctx.setFieldValue(User.class.getDeclaredField("admin"), invalidValue));
        assertThat(result.getMessage(), containsString(
                "Type mismatch. Cannot set value " + invalidValue + " of type " + invalidValue.getClass() +
                        " on field " + User.class.getDeclaredField("admin")));
    }

    @Test
    void setFieldValueSetsReferenceToAlreadyVisitedObjectWhenObjectIdIsPassedIn() throws Exception {
        final Organization org = Generator.generateOrganization();
        final Map<String, Object> knownInstances = Collections.singletonMap(org.getUri().toString(), org);
        final SingularObjectContext<Employee> ctx = new SingularObjectContext<>(new Employee(),
                BeanAnnotationProcessor.mapFieldsForDeserialization(Employee.class),
                knownInstances);
        ctx.setFieldValue(Employee.class.getDeclaredField("employer"), org.getUri().toString());
        assertNotNull(ctx.getInstance().getEmployer());
        assertSame(org, ctx.getInstance().getEmployer());
    }

    @Test
    void setFieldValueThrowsDeserializationExceptionWhenUnknownObjectIdIsPassedIn() throws Exception {
        final Organization org = Generator.generateOrganization();
        final SingularObjectContext<Employee> ctx = new SingularObjectContext<>(new Employee(),
                BeanAnnotationProcessor.mapFieldsForDeserialization(Employee.class),
                Collections.emptyMap());
        JsonLdDeserializationException result = assertThrows(JsonLdDeserializationException.class,
                () -> ctx.setFieldValue(Employee.class.getDeclaredField("employer"), org.getUri().toString()));
        assertThat(result.getMessage(),
                containsString(
                        "Type mismatch. Cannot set value " + org.getUri().toString() + " of type " + String.class +
                                " on field " + Employee.class.getDeclaredField("employer")));
    }

    @Test
    void setFieldValueThrowsDeserializationExceptionWhenIdOfInstanceWithInvalidTypeIsPassedIn()
            throws Exception {
        final User u = Generator.generateUser();
        final Map<String, Object> knownInstances = Collections.singletonMap(u.getUri().toString(), u);
        final SingularObjectContext<Employee> ctx = new SingularObjectContext<>(new Employee(),
                BeanAnnotationProcessor.mapFieldsForDeserialization(Employee.class),
                knownInstances);
        JsonLdDeserializationException result = assertThrows(JsonLdDeserializationException.class,
                () -> ctx.setFieldValue(Employee.class.getDeclaredField("employer"), u.getUri().toString()));
        assertThat(result.getMessage(),
                containsString("Type mismatch. Cannot set value " + u + " of type " + u.getClass() + " on field " +
                        Employee.class.getDeclaredField("employer")));
    }

    @Test
    void setFieldValueHandlesConversionFromStringToUri() throws Exception {
        final URI id = Generator.generateUri();
        final SingularObjectContext<Person> ctx = new SingularObjectContext<>(new Person(),
                BeanAnnotationProcessor.mapFieldsForDeserialization(
                        Person.class), Collections.emptyMap());
        ctx.setFieldValue(Person.class.getDeclaredField("uri"), id.toString());
        assertEquals(id, ctx.getInstance().getUri());
    }

    @Test
    void isPropertyMappedReturnsTrueForUnmappedPropertyWhenClassContainsPropertiesField() {
        final SingularObjectContext<Person> ctx = new SingularObjectContext<>(new Person(),
                BeanAnnotationProcessor.mapFieldsForDeserialization(Person.class), Collections.emptyMap());
        assertTrue(ctx.isPropertyMapped(Vocabulary.IS_ADMIN));
    }

    @Test
    void isPropertyMappedReturnsFalseForUnknownProperty() {
        final SingularObjectContext<Organization> ctx = new SingularObjectContext<>(new Organization(),
                BeanAnnotationProcessor.mapFieldsForDeserialization(
                        Organization.class), Collections.emptyMap());
        assertFalse(ctx.isPropertyMapped(Vocabulary.IS_ADMIN));
    }

    @Test
    void setIdentifierValueSkipsBlankNodeWhenTargetTypeIsNotString() {
        final SingularObjectContext<Person> ctx = new SingularObjectContext<>(new Person(),
                BeanAnnotationProcessor.mapFieldsForDeserialization(Person.class), Collections.emptyMap());
        final String bNode = "_:b1";
        assertNull(ctx.instance.getUri());
        ctx.setIdentifierValue(bNode);
        assertNull(ctx.instance.getUri());
    }

    @Test
    void setIdentifierValueSetsBlankNodeIdentifierWhenTargetTypeIsString() {
        final SingularObjectContext<WithStringId> ctx = new SingularObjectContext<>(new WithStringId(),
                BeanAnnotationProcessor.mapFieldsForDeserialization(WithStringId.class), new HashMap<>());
        final String bNode = "_:b1";
        ctx.setIdentifierValue(bNode);
        assertEquals(bNode, ctx.instance.id);
    }

    @SuppressWarnings("unused")
    @OWLClass(iri = Vocabulary.PERSON)
    private static class WithStringId {
        @Id
        private String id;
    }

    @Test
    void setFieldValueUsesKnownInstancesIdentifierToSetPlainIdentifierFieldValue() throws Exception {
        final Organization org = Generator.generateOrganization();
        final EmployeeWithPlainIdentifierField instance = new EmployeeWithPlainIdentifierField();
        final SingularObjectContext<EmployeeWithPlainIdentifierField> sut = new SingularObjectContext<>(instance,
                BeanAnnotationProcessor.mapFieldsForDeserialization(EmployeeWithPlainIdentifierField.class),
                Collections.singletonMap(org.getUri().toString(), org));
        sut.setFieldValue(EmployeeWithPlainIdentifierField.class.getDeclaredField("organization"),
                org.getUri().toString());
        assertEquals(org.getUri(), instance.organization);
    }

    @SuppressWarnings("unused")
    @OWLClass(iri = Vocabulary.EMPLOYEE)
    private static class EmployeeWithPlainIdentifierField {
        @Id
        private URI id;

        @OWLObjectProperty(iri = Vocabulary.IS_MEMBER_OF)
        private URI organization;
    }

    @Test
    void supportsReturnsTrueForMappedField() {
        final SingularObjectContext<Organization> ctx = new SingularObjectContext<>(new Organization(),
                BeanAnnotationProcessor.mapFieldsForDeserialization(Organization.class), Collections.emptyMap());
        assertTrue(ctx.supports(Vocabulary.DATE_CREATED));
    }

    @Test
    void supportsReturnsFalseForPropertyWithReadOnlyAccess() {
        final SingularObjectContext<Study> ctx = new SingularObjectContext<>(new Study(),
                BeanAnnotationProcessor.mapFieldsForDeserialization(Study.class), Collections.emptyMap());
        assertFalse(ctx.supports(Vocabulary.NUMBER_OF_PEOPLE_INVOLVED));
    }

    @Test
    void setIdentifierValueStoresIdentifierToBeAccessibleByGetter() {
        final SingularObjectContext<Person> sut = new SingularObjectContext<>(new Person(),
                BeanAnnotationProcessor.mapFieldsForDeserialization(Person.class), new HashMap<>());
        final String identifier = Generator.generateUri().toString();
        sut.setIdentifierValue(identifier);
        assertEquals(identifier, sut.getIdentifier());
    }
}
