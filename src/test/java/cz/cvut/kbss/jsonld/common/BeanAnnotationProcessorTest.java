/**
 * Copyright (C) 2020 Czech Technical University in Prague
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

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.jopa.vocabulary.DC;
import cz.cvut.kbss.jopa.vocabulary.RDF;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jopa.vocabulary.SKOS;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.annotation.JsonLdAttributeOrder;
import cz.cvut.kbss.jsonld.annotation.JsonLdProperty;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.*;
import cz.cvut.kbss.jsonld.exception.JsonLdSerializationException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.*;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
class BeanAnnotationProcessorTest {

    @Test
    void getOwlClassesExtractsClassIriFromObject() {
        final Person p = new Person();
        final Set<String> result = BeanAnnotationProcessor.getOwlClasses(p);
        assertEquals(1, result.size());
        assertTrue(result.contains(Vocabulary.PERSON));
    }

    @Test
    void getOwlClassesReturnsNullForNonOwlClass() {
        final URI uri = URI.create("http://test");
        assertTrue(BeanAnnotationProcessor.getOwlClasses(uri).isEmpty());
    }

    @Test
    void getOwlClassesExtractsClassesFromAncestors() {
        final User u = new User();
        final Set<String> result = BeanAnnotationProcessor.getOwlClasses(u);
        assertTrue(result.contains(Vocabulary.USER));
        assertTrue(result.contains(Vocabulary.PERSON));
    }

    @Test
    void getSerializableFieldsExtractsAllFieldsOfAnInstanceIncludingSuperclassOnes() throws Exception {
        final User u = new User();
        final List<Field> fields = BeanAnnotationProcessor.getSerializableFields(u);
        assertTrue(fields.contains(User.class.getDeclaredField("username")));
        assertTrue(fields.containsAll(
                Arrays.asList(Person.class.getDeclaredField("uri"), Person.class.getDeclaredField("firstName"),
                        Person.class.getDeclaredField("lastName"))));
    }

    @Test
    void getSerializableFieldsSkipsStaticFields() throws Exception {
        final Organization org = new Organization();
        final List<Field> fields = BeanAnnotationProcessor.getSerializableFields(org);
        assertFalse(fields.contains(Organization.class.getDeclaredField("DEFAULT_COUNTRY")));
    }

    @Test
    void getSerializableFieldsSkipsNonAnnotatedFields() throws Exception {
        final Organization org = new Organization();
        final List<Field> fields = BeanAnnotationProcessor.getSerializableFields(org);
        assertFalse(fields.contains(Organization.class.getDeclaredField("age")));
    }

    @Test
    void getSerializableFieldsSkipsFieldsWithWriteOnlyAccess() throws Exception {
        final SerializableWithWriteOnly instance = new SerializableWithWriteOnly();
        final List<Field> result = BeanAnnotationProcessor.getSerializableFields(instance);
        assertFalse(result.contains(SerializableWithWriteOnly.class.getDeclaredField("writeOnly")));
    }

    private static class SerializableWithWriteOnly {
        @Id
        private URI uri;

        @OWLDataProperty(iri = "http://serializable")
        private String serializableField;

        @JsonLdProperty(access = JsonLdProperty.Access.WRITE_ONLY)
        @OWLDataProperty(iri = "http://writeOnly")
        private String writeOnly;
    }

    @Test
    void mapSerializableFieldsReturnsMapOfSerializableFieldsWithPropertyIriKeys() throws Exception {
        final Map<String, Field> result = BeanAnnotationProcessor.mapFieldsForDeserialization(Employee.class);
        assertEquals(Person.class.getDeclaredField("firstName"), result.get(Vocabulary.FIRST_NAME));
        assertEquals(Person.class.getDeclaredField("lastName"), result.get(Vocabulary.LAST_NAME));
        assertEquals(User.class.getDeclaredField("username"), result.get(Vocabulary.USERNAME));
        assertEquals(User.class.getDeclaredField("admin"), result.get(Vocabulary.IS_ADMIN));
        assertEquals(Employee.class.getDeclaredField("employer"), result.get(Vocabulary.IS_MEMBER_OF));
    }

    @Test
    void mapSerializableFieldsResultContainsTypesFieldMappedToRdfType() throws Exception {
        final Map<String, Field> result = BeanAnnotationProcessor.mapFieldsForDeserialization(User.class);
        assertTrue(result.containsKey(JsonLd.TYPE));
        assertEquals(User.class.getDeclaredField("types"), result.get(JsonLd.TYPE));
    }

    @Test
    void getAttributeIdentifierReturnsIdForIdAttribute() throws Exception {
        final String id = BeanAnnotationProcessor.getAttributeIdentifier(Person.class.getDeclaredField("uri"));
        assertEquals(JsonLd.ID, id);
    }

    @Test
    void getAttributeIdentifierReturnsIriOfOWLDataProperty() throws Exception {
        final String id = BeanAnnotationProcessor.getAttributeIdentifier(Person.class.getDeclaredField("firstName"));
        assertEquals(Vocabulary.FIRST_NAME, id);
    }

    @Test
    void getAttributeIdentifierReturnsIriOfOWLObjectProperty() throws Exception {
        final String id = BeanAnnotationProcessor.getAttributeIdentifier(Employee.class.getDeclaredField("employer"));
        assertEquals(Vocabulary.IS_MEMBER_OF, id);
    }

    @Test
    void getAttributeIdentifierReturnsIriOfOWLAnnotationProperty() throws Exception {
        final String id = BeanAnnotationProcessor.getAttributeIdentifier(Organization.class.getDeclaredField("name"));
        assertEquals(RDFS.LABEL, id);
    }

    @Test
    void getAttributeIdentifierThrowsIllegalArgumentWhenFieldIsNotJsonLdSerializable() throws Exception {
        final JsonLdSerializationException result = assertThrows(JsonLdSerializationException.class,
                () -> BeanAnnotationProcessor
                        .getAttributeIdentifier(Organization.class.getDeclaredField("DEFAULT_COUNTRY")));
        assertEquals(
                "Field " + Organization.class.getDeclaredField("DEFAULT_COUNTRY") + " is not JSON-LD serializable.",
                result.getMessage());
    }

    @Test
    void getInstanceIdentifierExtractsIdFieldValue() {
        final Organization org = Generator.generateOrganization();
        assertEquals(org.getUri(), BeanAnnotationProcessor.getInstanceIdentifier(org).get());
    }

    @Test
    void getInstanceIdentifierExtractsIdFieldFromAncestorClass() {
        final Employee e = Generator.generateEmployee();
        assertEquals(e.getUri(), BeanAnnotationProcessor.getInstanceIdentifier(e).get());
    }

    @Test
    void getInstanceIdentifierReturnsEmptyOptionalWhenInstanceHasNoIdentifierField() {
        final ClassWithoutIdentifier instance = new ClassWithoutIdentifier();
        final Optional<Object> result = BeanAnnotationProcessor.getInstanceIdentifier(instance);
        assertNotNull(result);
        assertFalse(result.isPresent());
    }

    @OWLClass(iri = "http://krizik.felk.cvut.cz/ontologies/jb4jsonld/ClassWithoutIdentifier")
    private static class ClassWithoutIdentifier {
        @OWLAnnotationProperty(iri = RDFS.LABEL)
        private String label;
    }

    @Test
    void isOwlClassEntityReturnsTrueForClassAnnotatedWithOWLClass() {
        assertTrue(BeanAnnotationProcessor.isOwlClassEntity(User.class));
    }

    @Test
    void isOwlClassEntityReturnsFalseForClassWithoutOWLClassAnnotation() {
        assertFalse(BeanAnnotationProcessor.isOwlClassEntity(String.class));
    }

    @Test
    void isOwlClassEntityReturnsFalseForNullArgument() {
        assertFalse(BeanAnnotationProcessor.isOwlClassEntity(null));
    }

    @Test
    void isInstanceIdentifierReturnsTrueForIdField() throws Exception {
        assertTrue(BeanAnnotationProcessor.isInstanceIdentifier(Person.class.getDeclaredField("uri")));
    }

    @Test
    void isInstanceIdentifierReturnsFalseForNonIdField() throws Exception {
        assertFalse(BeanAnnotationProcessor.isInstanceIdentifier(Person.class.getDeclaredField("firstName")));
    }

    @Test
    void getOwlClassExtractsClassIriFromJavaType() {
        assertEquals(Vocabulary.EMPLOYEE, BeanAnnotationProcessor.getOwlClass(Employee.class));
    }

    @Test
    void getOwlClassThrowsIllegalArgumentWhenNonOwlClassJavaTypeIsPassedAsArgument() {
        final IllegalArgumentException result = assertThrows(IllegalArgumentException.class,
                () -> BeanAnnotationProcessor.getOwlClass(Integer.class));
        assertEquals(Integer.class + " is not an OWL class entity.", result.getMessage());
    }

    @Test
    void getOwlClassExpandsIriIfItUsesNamespacePrefix() {
        assertEquals(DC.Terms.AGENT, BeanAnnotationProcessor.getOwlClass(ClassWithNamespace.class));
    }

    @Namespace(prefix = "dc", namespace = DC.Terms.NAMESPACE)
    @OWLClass(iri = "dc:Agent")
    private static class ClassWithNamespace {
    }

    @Test
    void getOWLClassExpandsIriBasedOnNamespacesDeclarationIfItUsesPrefix() {
        assertEquals(SKOS.CONCEPT, BeanAnnotationProcessor.getOwlClass(ClassWithNamespaces.class));
    }

    @Namespaces({@Namespace(prefix = "rdf", namespace = RDF.NAMESPACE),
                 @Namespace(prefix = "skos", namespace = SKOS.NAMESPACE)})
    @OWLClass(iri = "skos:Concept")
    private static class ClassWithNamespaces {

        @OWLDataProperty(iri = "skos:prefLabel")
        private String prefLabel;

        @OWLObjectProperty(iri = "rdfs:range")
        private URI range;
    }

    @Test
    void getOWLClassExpandsIriBasedOnNamespaceDeclaredInAncestorIfItUsesPrefix() {
        assertEquals(SKOS.CONCEPT_SCHEME, BeanAnnotationProcessor.getOwlClass(ClassWithParentNamespaces.class));
    }

    @OWLClass(iri = "skos:ConceptScheme")
    private static class ClassWithParentNamespaces extends ClassWithNamespaces {
    }

    @Test
    void getOwlClassesExpandsCompactIrisBasedOnNamespaces() {
        final Set<String> result = BeanAnnotationProcessor.getOwlClasses(ClassWithParentNamespaces.class);
        assertThat(result, hasItems(SKOS.CONCEPT_SCHEME, SKOS.CONCEPT));
    }

    @Test
    void getSerializableFieldsReturnsPropertiesFieldAsWell() throws Exception {
        final List<Field> fields = BeanAnnotationProcessor.getSerializableFields(new Person());
        assertTrue(fields.contains(Person.class.getDeclaredField("properties")));
    }

    @Test
    void mapSerializableFieldsSkipsPropertiesField() throws Exception {
        final Map<String, Field> fields = BeanAnnotationProcessor.mapFieldsForDeserialization(Person.class);
        assertFalse(fields.containsValue(Person.class.getDeclaredField("properties")));
    }

    @Test
    void hasPropertiesFieldReturnsFalseForClassWithoutProperties() {
        assertFalse(BeanAnnotationProcessor.hasPropertiesField(Organization.class));
    }

    @Test
    void hasPropertiesFieldReturnsTrueForClassWithProperties() {
        assertTrue(BeanAnnotationProcessor.hasPropertiesField(Person.class));
        assertTrue(BeanAnnotationProcessor.hasPropertiesField(User.class)); // Check also subclasses
    }

    @Test
    void getPropertiesFieldReturnsPropertiesFieldOfClass() throws Exception {
        final Field result = BeanAnnotationProcessor.getPropertiesField(Person.class);
        assertEquals(Person.class.getDeclaredField("properties"), result);
    }

    @Test
    void getPropertiesFieldThrowsIllegalArgumentForClassWithoutPropertiesField() {
        final IllegalArgumentException result = assertThrows(IllegalArgumentException.class,
                () -> BeanAnnotationProcessor.getPropertiesField(Organization.class));
        assertThat(result.getMessage(), containsString(Organization.class + " does not have a @Properties field."));
    }

    @Test
    void getAttributeOrderReturnsValueOfJsonLdAttributeOrderAnnotation() {
        final String[] result = BeanAnnotationProcessor.getAttributeOrder(Study.class);
        assertArrayEquals(Study.class.getDeclaredAnnotation(JsonLdAttributeOrder.class).value(), result);
    }

    @Test
    void getAttributeOrderReturnsEmptyArrayWhenOrderIsNotSpecifiedOnClass() {
        final String[] result = BeanAnnotationProcessor.getAttributeOrder(Person.class);
        assertEquals(0, result.length);
    }

    @Test
    void getTypesFieldReturnsTypesAttribute() {
        final Optional<Field> result = BeanAnnotationProcessor.getTypesField(User.class);
        assertTrue(result.isPresent());
    }

    @Test
    void getTypesFieldReturnsEmptyOptionalIfTypesAttributeIsMissing() {
        final Optional<Field> result = BeanAnnotationProcessor.getTypesField(Person.class);
        assertFalse(result.isPresent());
    }

    @Test
    void hasTypesFieldReturnsTrueWhenClassHasTypesAttribute() {
        assertTrue(BeanAnnotationProcessor.hasTypesField(Employee.class));
    }

    @Test
    void hasTypesFieldReturnsFalseWhenClassDoesNotHaveTypesAttribute() {
        assertFalse(BeanAnnotationProcessor.hasTypesField(Person.class));
    }

    @Test
    void isAnnotationPropertyReturnsTrueForAnnotationPropertyField() throws Exception {
        assertTrue(BeanAnnotationProcessor
                .isAnnotationProperty(ObjectWithAnnotationProperties.class.getDeclaredField("changedValue")));
        assertFalse(BeanAnnotationProcessor.isAnnotationProperty(Person.class.getDeclaredField("firstName")));
    }

    @Test
    void getAttributeIdentifierExpandsCompactedIriBasedOnNamespaceDeclaration() throws Exception {
        assertEquals(SKOS.PREF_LABEL, BeanAnnotationProcessor
                .getAttributeIdentifier(ClassWithNamespaces.class.getDeclaredField("prefLabel")));
    }

    @Test
    void getAttributeIdentifierExpandsCompactedIriBasedOnPackageLevelNamespaceDeclaration() throws Exception {
        assertEquals(RDFS.RANGE,
                BeanAnnotationProcessor.getAttributeIdentifier(ClassWithNamespaces.class.getDeclaredField("range")));
    }
}
