/**
 * Copyright (C) 2016 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.common;

import cz.cvut.kbss.jopa.CommonVocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.environment.model.User;
import cz.cvut.kbss.jsonld.exception.JsonLdSerializationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class BeanAnnotationProcessorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void getOwlClassesExtractsClassIriFromObject() {
        final Person p = new Person();
        final Set<String> result = BeanAnnotationProcessor.getOwlClasses(p);
        assertEquals(1, result.size());
        assertTrue(result.contains(Vocabulary.PERSON));
    }

    @Test
    public void getOwlClassesReturnsNullForNonOwlClass() {
        final URI uri = URI.create("http://test");
        assertTrue(BeanAnnotationProcessor.getOwlClasses(uri).isEmpty());
    }

    @Test
    public void getOwlClassesExtractsClassesFromAncestors() {
        final User u = new User();
        final Set<String> result = BeanAnnotationProcessor.getOwlClasses(u);
        assertTrue(result.contains(Vocabulary.USER));
        assertTrue(result.contains(Vocabulary.PERSON));
    }

    @Test
    public void getSerializableFieldsExtractsAllFieldsOfAnInstanceIncludingSuperclassOnes() throws Exception {
        final User u = new User();
        final List<Field> fields = BeanAnnotationProcessor.getSerializableFields(u);
        assertTrue(fields.contains(User.class.getDeclaredField("username")));
        assertTrue(fields.containsAll(
                Arrays.asList(Person.class.getDeclaredField("uri"), Person.class.getDeclaredField("firstName"),
                        Person.class.getDeclaredField("lastName"))));
    }

    @Test
    public void getSerializableFieldsSkipsStaticFields() throws Exception {
        final Organization org = new Organization();
        final List<Field> fields = BeanAnnotationProcessor.getSerializableFields(org);
        assertFalse(fields.contains(Organization.class.getDeclaredField("DEFAULT_COUNTRY")));
    }

    @Test
    public void getSerializableFieldsSkipsNonAnnotatedFields() throws Exception {
        final Organization org = new Organization();
        final List<Field> fields = BeanAnnotationProcessor.getSerializableFields(org);
        assertFalse(fields.contains(Organization.class.getDeclaredField("age")));
    }

    @Test
    public void mapSerializableFieldsReturnsMapOfSerializableFieldsWithPropertyIriKeys() throws Exception {
        final Map<String, Field> result = BeanAnnotationProcessor.mapSerializableFields(Employee.class);
        assertEquals(Person.class.getDeclaredField("firstName"), result.get(Vocabulary.FIRST_NAME));
        assertEquals(Person.class.getDeclaredField("lastName"), result.get(Vocabulary.LAST_NAME));
        assertEquals(User.class.getDeclaredField("username"), result.get(Vocabulary.USERNAME));
        assertEquals(User.class.getDeclaredField("admin"), result.get(Vocabulary.IS_ADMIN));
        assertEquals(Employee.class.getDeclaredField("employer"), result.get(Vocabulary.IS_MEMBER_OF));
    }

    @Test
    public void mapSerializableFieldsResultContainsTypesFieldMappedToRdfType() throws Exception {
        final Map<String, Field> result = BeanAnnotationProcessor.mapSerializableFields(User.class);
        assertTrue(result.containsKey(JsonLd.TYPE));
        assertEquals(User.class.getDeclaredField("types"), result.get(JsonLd.TYPE));
    }

    @Test
    public void getAttributeIdentifierReturnsIdForIdAttribute() throws Exception {
        final String id = BeanAnnotationProcessor.getAttributeIdentifier(Person.class.getDeclaredField("uri"));
        assertEquals(JsonLd.ID, id);
    }

    @Test
    public void getAttributeIdentifierReturnsIriOfOWLDataProperty() throws Exception {
        final String id = BeanAnnotationProcessor.getAttributeIdentifier(Person.class.getDeclaredField("firstName"));
        assertEquals(Vocabulary.FIRST_NAME, id);
    }

    @Test
    public void getAttributeIdentifierReturnsIriOfOWLObjectProperty() throws Exception {
        final String id = BeanAnnotationProcessor.getAttributeIdentifier(Employee.class.getDeclaredField("employer"));
        assertEquals(Vocabulary.IS_MEMBER_OF, id);
    }

    @Test
    public void getAttributeIdentifierReturnsIriOfOWLAnnotationProperty() throws Exception {
        final String id = BeanAnnotationProcessor.getAttributeIdentifier(Organization.class.getDeclaredField("name"));
        assertEquals(CommonVocabulary.RDFS_LABEL, id);
    }

    @Test
    public void getAttributeIdentifierThrowsIllegalArgumentWhenFieldIsNotJsonLdSerializable() throws Exception {
        thrown.expect(JsonLdSerializationException.class);
        thrown.expectMessage(
                "Field " + Organization.class.getDeclaredField("DEFAULT_COUNTRY") + " is not JSON-LD serializable.");
        BeanAnnotationProcessor.getAttributeIdentifier(Organization.class.getDeclaredField("DEFAULT_COUNTRY"));
    }

    @Test
    public void getInstanceIdentifierExtractsIdFieldValue() throws Exception {
        final Organization org = Generator.generateOrganization();
        assertEquals(org.getUri(), BeanAnnotationProcessor.getInstanceIdentifier(org));
    }

    @Test
    public void getInstanceIdentifierExtractsIdFieldFromAncestorClass() throws Exception {
        final Employee e = Generator.generateEmployee();
        assertEquals(e.getUri(), BeanAnnotationProcessor.getInstanceIdentifier(e));
    }

    @Test
    public void getInstanceIdentifierThrowsExceptionWhenIdentifierFieldIsNotFound() {
        final Object instance = new ClassWithoutIdentifier();
        thrown.expect(JsonLdSerializationException.class);
        thrown.expectMessage("Instance " + instance + " contains no valid identifier field.");
        BeanAnnotationProcessor.getInstanceIdentifier(instance);
    }

    @OWLClass(iri = "http://krizik.felk.cvut.cz/ontologies/jaxb-jsonld/ClassWithoutIdentifier")
    private static class ClassWithoutIdentifier {
        @OWLAnnotationProperty(iri = CommonVocabulary.RDFS_LABEL)
        private String label;
    }

    @Test
    public void isOwlClassEntityReturnsTrueForClassAnnotatedWithOWLClass() {
        assertTrue(BeanAnnotationProcessor.isOwlClassEntity(User.class));
    }

    @Test
    public void isOwlClassEntityReturnsFalseForClassWithoutOWLClassAnnotation() {
        assertFalse(BeanAnnotationProcessor.isOwlClassEntity(String.class));
    }

    @Test
    public void isOwlClassEntityReturnsFalseForNullArgument() {
        assertFalse(BeanAnnotationProcessor.isOwlClassEntity(null));
    }

    @Test
    public void isInstanceIdentifierReturnsTrueForIdField() throws Exception {
        assertTrue(BeanAnnotationProcessor.isInstanceIdentifier(Person.class.getDeclaredField("uri")));
    }

    @Test
    public void isInstanceIdentifierReturnsFalseForNonIdField() throws Exception {
        assertFalse(BeanAnnotationProcessor.isInstanceIdentifier(Person.class.getDeclaredField("firstName")));
    }
}
