/**
 * Copyright (C) 2016 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.environment.model.User;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

public class SingularObjectContextTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void setFieldValueSetsFieldValueOnInstance() throws Exception {
        final SingularObjectContext<Person> ctx = new SingularObjectContext<>(new Person(),
                BeanAnnotationProcessor.mapSerializableFields(
                        Person.class), Collections.emptyMap());
        final String testFirstName = "John";
        ctx.setFieldValue(Person.class.getDeclaredField("firstName"), testFirstName);
        assertEquals(testFirstName, ctx.getInstance().getFirstName());
    }

    @Test
    public void setFieldValueThrowsDeserializationExceptionWhenInvalidTypeIsUsedAsFieldValue() throws Exception {
        final Integer invalidValue = 117;
        thrown.expect(JsonLdDeserializationException.class);
        thrown.expectMessage("Type mismatch. Cannot set value " + invalidValue + " of type " + invalidValue.getClass() +
                " on field " + Person.class.getDeclaredField("firstName"));
        final SingularObjectContext<Person> ctx = new SingularObjectContext<>(new Person(),
                BeanAnnotationProcessor.mapSerializableFields(
                        Person.class), Collections.emptyMap());
        ctx.setFieldValue(Person.class.getDeclaredField("firstName"), invalidValue);
    }

    @Test
    public void setFieldValueSetsReferenceToAlreadyVisitedObjectWhenObjectIdIsPassedIn() throws Exception {
        final Organization org = Generator.generateOrganization();
        final Map<String, Object> knownInstances = Collections.singletonMap(org.getUri().toString(), org);
        final SingularObjectContext<Employee> ctx = new SingularObjectContext<>(new Employee(),
                BeanAnnotationProcessor.mapSerializableFields(Employee.class),
                knownInstances);
        ctx.setFieldValue(Employee.class.getDeclaredField("employer"), org.getUri().toString());
        assertNotNull(ctx.getInstance().getEmployer());
        assertSame(org, ctx.getInstance().getEmployer());
    }

    @Test
    public void setFieldValueThrowsDeserializationExceptionWhenUnknownObjectIdIsPassedIn() throws Exception {
        final Organization org = Generator.generateOrganization();
        thrown.expect(JsonLdDeserializationException.class);
        thrown.expectMessage("Type mismatch. Cannot set value " + org.getUri().toString() + " of type " + String.class +
                " on field " + Employee.class.getDeclaredField("employer"));
        final SingularObjectContext<Employee> ctx = new SingularObjectContext<>(new Employee(),
                BeanAnnotationProcessor.mapSerializableFields(Employee.class),
                Collections.emptyMap());
        ctx.setFieldValue(Employee.class.getDeclaredField("employer"), org.getUri().toString());
    }

    @Test
    public void setFieldValueThrowsDeserializationExceptionWhenIdOfInstanceWithInvalidTypeIsPassedIn()
            throws Exception {
        final User u = Generator.generateUser();
        final Map<String, Object> knownInstances = Collections.singletonMap(u.getUri().toString(), u);
        thrown.expect(JsonLdDeserializationException.class);
        thrown.expectMessage("Type mismatch. Cannot set value " + u + " of type " + u.getClass() + " on field " +
                Employee.class.getDeclaredField("employer"));
        final SingularObjectContext<Employee> ctx = new SingularObjectContext<>(new Employee(),
                BeanAnnotationProcessor.mapSerializableFields(Employee.class),
                knownInstances);
        ctx.setFieldValue(Employee.class.getDeclaredField("employer"), u.getUri().toString());
    }

    @Test
    public void setFieldValueHandlesConversionFromStringToUri() throws Exception {
        final URI id = Generator.generateUri();
        final SingularObjectContext<Person> ctx = new SingularObjectContext<>(new Person(),
                BeanAnnotationProcessor.mapSerializableFields(
                        Person.class), Collections.emptyMap());
        ctx.setFieldValue(Person.class.getDeclaredField("uri"), id.toString());
        assertEquals(id, ctx.getInstance().getUri());
    }
}
