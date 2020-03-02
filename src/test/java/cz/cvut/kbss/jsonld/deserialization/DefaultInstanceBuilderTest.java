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
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.CollectionType;
import cz.cvut.kbss.jsonld.deserialization.util.TargetClassResolver;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.TestUtil;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.*;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import cz.cvut.kbss.jsonld.exception.TargetTypeException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.*;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DefaultInstanceBuilderTest {

    private InstanceBuilder sut =
            new DefaultInstanceBuilder(new TargetClassResolver(TestUtil.getDefaultTypeMap()));

    @Test
    void getCurrentRootReturnsNullIfThereIsNoRoot() {
        assertNull(sut.getCurrentRoot());
    }

    @Test
    void openObjectCreatesNewInstanceOfSpecifiedClass() {
        sut.openObject(TestUtil.PALMER_URI.toString(), Person.class);
        final Object res = sut.getCurrentRoot();
        assertNotNull(res);
        assertTrue(res instanceof Person);
    }

    @Test
    void openCollectionCreatesSet() {
        sut.openCollection(CollectionType.SET);
        assertTrue(sut.getCurrentRoot() instanceof Set);
    }

    @Test
    void openCollectionInCollectionPushesOriginalCollectionToStack() throws Exception {
        sut.openCollection(CollectionType.SET);
        sut.openCollection(CollectionType.SET);
        assertTrue(sut.getCurrentRoot() instanceof Set);
        assertFalse(getOpenInstances().isEmpty());
        assertTrue(getOpenInstances().peek().getInstance() instanceof Set);
        assertNotSame(sut.getCurrentRoot(), getOpenInstances().peek().getInstance());
    }

    @Test
    void openObjectAddsObjectToCurrentlyOpenCollectionAndBecomesCurrentInstance() throws Exception {
        sut.openCollection(CollectionType.SET);
        sut.openObject(TestUtil.PALMER_URI.toString(), Employee.class);
        final Object root = sut.getCurrentRoot();
        assertTrue(root instanceof Employee);
        assertFalse(getOpenInstances().isEmpty());
        final Object stackTop = getOpenInstances().peek().getInstance();
        assertTrue(stackTop instanceof Set);
        final Set<?> set = (Set<?>) stackTop;
        assertEquals(1, set.size());
        assertSame(root, set.iterator().next());
    }

    @SuppressWarnings("unchecked")
    private Stack<InstanceContext<?>> getOpenInstances() throws Exception {
        final Field stackField = DefaultInstanceBuilder.class.getDeclaredField("openInstances");
        stackField.setAccessible(true);
        return (Stack<InstanceContext<?>>) stackField.get(sut);
    }

    private InstanceContext<?> getCurrentInstance() throws Exception {
        final Field instField = DefaultInstanceBuilder.class.getDeclaredField("currentInstance");
        instField.setAccessible(true);
        return (InstanceContext<?>) instField.get(sut);
    }

    @Test
    void closeObjectPopsPreviousInstanceFromTheStack() throws Exception {
        sut.openCollection(CollectionType.SET);
        final Object originalRoot = sut.getCurrentRoot();
        assertTrue(originalRoot instanceof Set);
        sut.openObject(TestUtil.PALMER_URI.toString(), Employee.class);
        assertFalse(getOpenInstances().isEmpty());
        sut.closeObject();
        assertTrue(getOpenInstances().isEmpty());
        assertSame(originalRoot, sut.getCurrentRoot());
    }

    @Test
    void closeObjectDoesNothingForRootObject() throws Exception {
        sut.openObject(TestUtil.PALMER_URI.toString(), Person.class);
        assertTrue(getOpenInstances().isEmpty());
        final Object root = sut.getCurrentRoot();
        sut.closeObject();
        assertTrue(getOpenInstances().isEmpty());
        assertSame(root, sut.getCurrentRoot());
    }

    @Test
    void addValueAddsValuesToCurrentlyOpenCollection() {
        final List<Integer> items = new ArrayList<>();
        for (int i = 0; i < Generator.randomCount(10); i++) {
            items.add(Generator.randomCount(Integer.MAX_VALUE));
        }
        sut.openCollection(CollectionType.LIST);
        items.forEach(item -> sut.addValue(item));
        assertTrue(sut.getCurrentRoot() instanceof List);
        final List<?> result = (List<?>) sut.getCurrentRoot();
        for (int i = 0; i < items.size(); i++) {
            assertEquals(items.get(i), result.get(i));
        }
    }

    @Test
    void openObjectByPropertyCreatesObjectOfCorrectType() {
        sut.openObject(TestUtil.PALMER_URI.toString(), Employee.class);
        sut.openObject(Generator.generateUri().toString(), Vocabulary.IS_MEMBER_OF,
                Collections.singletonList(Vocabulary.ORGANIZATION));
        assertTrue(sut.getCurrentRoot() instanceof Organization);
    }

    @Test
    void openObjectByPropertySetsNewInstanceAsFieldValueAndReplacesCurrentContext() throws Exception {
        sut.openObject(TestUtil.PALMER_URI.toString(), Employee.class);
        sut.openObject(Generator.generateUri().toString(), Vocabulary.IS_MEMBER_OF,
                Collections.singletonList(Vocabulary.ORGANIZATION));
        assertFalse(getOpenInstances().isEmpty());
        final Object top = getOpenInstances().peek().getInstance();
        assertTrue(top instanceof Employee);
        assertNotNull(((Employee) top).getEmployer());
    }

    @Test
    void addValueSetsPropertyValue() {
        sut.openObject(TestUtil.HALSEY_URI.toString(), User.class);
        final String firstName = "Catherine";
        final String lastName = "Halsey";
        sut.addValue(Vocabulary.FIRST_NAME, firstName);
        sut.addValue(Vocabulary.LAST_NAME, lastName);
        sut.addValue(Vocabulary.IS_ADMIN, true);
        final User res = (User) sut.getCurrentRoot();
        assertEquals(firstName, res.getFirstName());
        assertEquals(lastName, res.getLastName());
        assertTrue(res.getAdmin());
    }

    @Test
    void openCollectionByPropertyCreatesCollectionOfCorrectType() throws Exception {
        sut.openObject(Generator.generateUri().toString(), Organization.class);
        sut.openCollection(Vocabulary.HAS_MEMBER);
        assertFalse(getOpenInstances().isEmpty());
        assertTrue(sut.getCurrentRoot() instanceof Set);
    }

    @Test
    void openCollectionByPropertySetsFieldValueAndPushesCurrentInstanceToStack() throws Exception {
        sut.openObject(Generator.generateUri().toString(), Organization.class);
        sut.openCollection(Vocabulary.HAS_MEMBER);
        assertFalse(getOpenInstances().isEmpty());
        assertTrue(getOpenInstances().peek().getInstance() instanceof Organization);
        final Organization org = (Organization) getOpenInstances().peek().getInstance();
        assertSame(sut.getCurrentRoot(), org.getEmployees());
    }

    @Test
    void closeCollectionPopsLastInstanceFromStack() throws Exception {
        sut.openObject(Generator.generateUri().toString(), Organization.class);
        final Object originalRoot = sut.getCurrentRoot();
        sut.openCollection(Vocabulary.HAS_MEMBER);
        assertFalse(getOpenInstances().isEmpty());
        sut.closeCollection();
        assertTrue(getOpenInstances().isEmpty());
        assertSame(originalRoot, sut.getCurrentRoot());
    }

    @Test
    void closeCollectionDoesNothingWhenItIsTheTopElement() throws Exception {
        sut.openCollection(CollectionType.SET);
        assertTrue(getOpenInstances().isEmpty());
        final Object root = sut.getCurrentRoot();
        sut.closeCollection();
        assertTrue(getOpenInstances().isEmpty());
        assertSame(root, sut.getCurrentRoot());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getKnownInstances() throws Exception {
        final Field field = DefaultInstanceBuilder.class.getDeclaredField("knownInstances");
        field.setAccessible(true);
        return (Map<String, Object>) field.get(sut);
    }

    @Test
    void openCollectionCreatesTypesContextForTypesField() throws Exception {
        sut.openObject(TestUtil.PALMER_URI.toString(), User.class);
        sut.openCollection(JsonLd.TYPE);
        final InstanceContext<?> result = getCurrentInstance();
        assertTrue(result instanceof TypesContext);
        final TypesContext<?, ?> ctx = (TypesContext<?, ?>) result;
        assertEquals(String.class, ctx.getItemType());
    }

    @Test
    void isPropertyMappedReturnsTrueForMappedProperty() {
        sut.openObject(TestUtil.PALMER_URI.toString(), Person.class);
        assertTrue(sut.isPropertyMapped(Vocabulary.FIRST_NAME));
    }

    @Test
    void isPropertyMappedReturnsFalseForUnknownProperty() {
        sut.openObject(Generator.generateUri().toString(), Organization.class);
        assertFalse(sut.isPropertyMapped(Vocabulary.IS_ADMIN));
    }

    @Test
    void isPropertyMappedReturnsTrueForType() {
        sut.openObject(TestUtil.PALMER_URI.toString(), Person.class);
        assertTrue(sut.isPropertyMapped(JsonLd.TYPE));
    }

    @Test
    void isPluralReturnsTrueForType() {
        sut.openObject(TestUtil.PALMER_URI.toString(), Person.class);
        assertTrue(sut.isPlural(JsonLd.TYPE));
    }

    @Test
    void openCollectionOpensDummyCollectionContextForTypeInInstanceWithoutTypesField() throws Exception {
        sut.openObject(TestUtil.PALMER_URI.toString(), Person.class);
        assertTrue(getCurrentInstance() instanceof SingularObjectContext);
        sut.openCollection(JsonLd.TYPE);
        assertTrue(getCurrentInstance() instanceof DummyCollectionInstanceContext);
        sut.closeCollection();
        assertTrue(getCurrentInstance() instanceof SingularObjectContext);
    }

    @Test
    void addingTypesToInstanceWithoutTypesFieldDoesNothing() {
        sut.openObject(TestUtil.PALMER_URI.toString(), Person.class);
        sut.openCollection(JsonLd.TYPE);
        sut.addValue(Vocabulary.EMPLOYEE);
        sut.addValue(Vocabulary.USER);
        sut.closeCollection();
        assertNotNull(sut.getCurrentRoot());
    }

    @Test
    void addValueOpensCollectionAndAddsSingleItemToItWhenSingleValueForPluralAttributeIsSpecified() {
        sut.openObject(Generator.generateUri().toString(), Organization.class);
        final String value = "Mjolnir-IV";
        sut.addValue(Vocabulary.BRAND, value);
        final Organization object = (Organization) sut.getCurrentRoot();
        assertEquals(1, object.getBrands().size());
        assertTrue(object.getBrands().contains(value));
    }

    @Test
    void openCollectionCreatesPropertiesContextAndSetsNewPropertiesMapOnTargetInstanceWhenItDoesNotExist() {
        sut.openObject(TestUtil.PALMER_URI.toString(), Person.class);
        final Person instance = (Person) sut.getCurrentRoot();
        assertNull(instance.getProperties());
        sut.openCollection(Vocabulary.IS_ADMIN);
        assertNotNull(instance.getProperties());
    }

    @Test
    void openCollectionReusesPropertiesInstanceWhenItAlreadyExistsOnTargetInstance() {
        sut.openObject(TestUtil.PALMER_URI.toString(), Person.class);
        final Person instance = (Person) sut.getCurrentRoot();
        assertNull(instance.getProperties());
        sut.openCollection(Vocabulary.IS_ADMIN);
        final Map<String, Set<String>> propsMap = instance.getProperties();
        sut.closeCollection();
        sut.openCollection(Vocabulary.USERNAME);
        assertSame(propsMap, instance.getProperties());
    }

    @Test
    void openObjectCreatesIdentifierContextWhenTargetTypeIsIdentifier() throws Exception {
        sut.openObject(Generator.generateUri().toString(), Event.class);
        sut.openCollection(Vocabulary.HAS_EVENT_TYPE);
        sut.openObject(Generator.generateUri().toString(), URI.class);
        final InstanceContext<?> ctx = getCurrentInstance();
        assertTrue(ctx instanceof NodeReferenceContext);
    }

    @Test
    void openObjectCreatesIdentifierContextForPropertyWithPlainIdentifierTargetFieldType() throws Exception {
        sut.openObject(Generator.generateUri().toString(), Organization.class);
        sut.openObject(Generator.generateUri().toString(), Vocabulary.ORIGIN, Collections.emptyList());
        final InstanceContext<?> ctx = getCurrentInstance();
        assertTrue(ctx instanceof NodeReferenceContext);
    }

    @Test
    void addNodeReferenceForPropertySetsPlainIdentifierObjectPropertyValue() {
        sut.openObject(Generator.generateUri().toString(), Organization.class);
        final String uri = Generator.generateUri().toString();
        sut.addNodeReference(Vocabulary.ORIGIN, uri);
        final Organization object = (Organization) sut.getCurrentRoot();
        assertEquals(uri, object.getCountry().toString());
    }

    @Test
    void addNodeReferenceForPropertySetsFieldValueToKnownInstance() throws Exception {
        final Organization org = Generator.generateOrganization();
        getKnownInstances().put(org.getUri().toString(), org);
        sut.openObject(TestUtil.PALMER_URI.toString(), Employee.class);
        sut.addNodeReference(Vocabulary.IS_MEMBER_OF, org.getUri().toString());
        final Employee object = (Employee) sut.getCurrentRoot();
        assertEquals(org, object.getEmployer());
    }

    @Test
    void addNodeReferenceForPropertyThrowsDeserializationExceptionWhenNoKnownInstanceIsFound() {
        final String nodeId = Generator.generateUri().toString();
        sut.openObject(TestUtil.PALMER_URI.toString(), Employee.class);
        final JsonLdDeserializationException result = assertThrows(JsonLdDeserializationException.class,
                () -> sut.addNodeReference(Vocabulary.IS_MEMBER_OF, nodeId));
        assertEquals("Node with IRI " + nodeId + " cannot be referenced, because it has not been encountered yet.",
                result.getMessage());
    }

    @Test
    void addNodeReferenceAddsPlainIdentifierObjectPropertyValueToCollection() {
        final URI nodeId = Generator.generateUri();
        sut.openObject(Generator.generateUri().toString(), Event.class);
        sut.openCollection(Vocabulary.HAS_EVENT_TYPE);
        sut.addNodeReference(nodeId.toString());
        sut.closeCollection();
        final Event object = (Event) sut.getCurrentRoot();
        assertTrue(object.eventTypes.contains(nodeId));
    }

    @OWLClass(iri = Generator.URI_BASE + "Event")
    public static class Event {
        @Id
        private URI uri;

        @OWLObjectProperty(iri = Vocabulary.HAS_EVENT_TYPE)
        private Set<URI> eventTypes;
    }

    @Test
    void addNodeReferenceAddsKnownInstanceWithMatchingIdentifier() throws Exception {
        final Employee employee = Generator.generateEmployee();
        getKnownInstances().put(employee.getUri().toString(), employee);
        sut.openObject(Generator.generateUri().toString(), Organization.class);
        sut.openCollection(Vocabulary.HAS_MEMBER);
        sut.addNodeReference(employee.getUri().toString());
        sut.closeCollection();
        final Organization object = (Organization) sut.getCurrentRoot();
        assertTrue(object.getEmployees().contains(employee));
    }

    @Test
    void addNodeReferenceThrowsDeserializationExceptionWhenNoKnownInstanceIsFound() {
        final String nodeId = Generator.generateUri().toString();
        sut.openObject(Generator.generateUri().toString(), Organization.class);
        sut.openCollection(Vocabulary.HAS_MEMBER);
        final JsonLdDeserializationException result = assertThrows(JsonLdDeserializationException.class,
                () -> sut.addNodeReference(nodeId));
        assertEquals("Node with IRI " + nodeId + " cannot be referenced, because it has not been encountered yet.",
                result.getMessage());
    }

    @Test
    void openObjectSetsInstanceIdentifier() {
        sut.openObject(TestUtil.HALSEY_URI.toString(), Employee.class);
        final Employee root = (Employee) sut.getCurrentRoot();
        assertEquals(TestUtil.HALSEY_URI, root.getUri());
    }

    @Test
    void openObjectAsPropertyValueSetsInstanceIdentifier() {
        sut.openObject(TestUtil.HALSEY_URI.toString(), Employee.class);
        sut.openObject(TestUtil.UNSC_URI.toString(), Vocabulary.IS_MEMBER_OF,
                Collections.singletonList(Vocabulary.ORGANIZATION));
        final Organization root = (Organization) sut.getCurrentRoot();
        assertEquals(TestUtil.UNSC_URI, root.getUri());
    }

    @Test
    void objectIsStoredInKnownInstancesWhenItIsOpen() throws Exception {
        sut.openObject(TestUtil.PALMER_URI.toString(), User.class);
        assertTrue(getKnownInstances().containsKey(TestUtil.PALMER_URI.toString()));
        assertTrue(getKnownInstances().get(TestUtil.PALMER_URI.toString()) instanceof User);
    }

    @Test
    void openObjectReopensAlreadyKnownInstance() throws Exception {
        final Employee employee = Generator.generateEmployee();
        getKnownInstances().put(employee.getUri().toString(), employee);
        sut.openObject(employee.getUri().toString(), Employee.class);
        assertSame(employee, sut.getCurrentRoot());
    }

    @Test
    void openObjectThrowsTargetTypeExceptionWhenReopenObjectHasIncompatibleTypeWithTarget() throws Exception {
        final Employee employee = Generator.generateEmployee();
        employee.setUri(TestUtil.PALMER_URI);
        getKnownInstances().put(employee.getUri().toString(), employee);
        final TargetTypeException result = assertThrows(TargetTypeException.class,
                () -> sut.openObject(employee.getUri().toString(), Organization.class));
        assertThat(result.getMessage(), containsString(
                "instance with id " + TestUtil.PALMER_URI + " already exists, but its type " + Employee.class +
                        " is not compatible with target type " + Organization.class));
    }

    @Test
    void openObjectForPropertyReopensAlreadyKnownInstance() throws Exception {
        final Organization org = Generator.generateOrganization();
        getKnownInstances().put(org.getUri().toString(), org);
        sut.openObject(TestUtil.PALMER_URI.toString(), Employee.class);
        sut.openObject(org.getUri().toString(), Vocabulary.IS_MEMBER_OF,
                Collections.singletonList(Vocabulary.ORGANIZATION));
        assertSame(org, sut.getCurrentRoot());
    }

    @Test
    void openCollectionForPropertyReusesExistingCollectionIfItWasSetOnReopenObject() {
        sut.openObject(TestUtil.UNSC_URI.toString(), Organization.class);
        sut.openCollection(Vocabulary.HAS_MEMBER);
        sut.openObject(TestUtil.HALSEY_URI.toString(), Employee.class);
        sut.closeObject();
        sut.closeCollection();
        sut.openCollection(Vocabulary.HAS_MEMBER);
        final Set<Employee> root = (Set<Employee>) sut.getCurrentRoot();
        assertFalse(root.isEmpty());
        assertEquals(TestUtil.HALSEY_URI, root.iterator().next().getUri());
    }

    @Test
    void openObjectThrowsDeserializationExceptionWhenTryingToSetSingularAttributeValueForSecondTime() {
        sut.openObject(TestUtil.HALSEY_URI.toString(), Employee.class);
        sut.openObject(TestUtil.UNSC_URI.toString(), Vocabulary.IS_MEMBER_OF,
                Collections.singletonList(Vocabulary.ORGANIZATION));
        sut.closeObject();
        assertNotNull(((Employee) sut.getCurrentRoot()).getEmployer());
        final JsonLdDeserializationException result = assertThrows(JsonLdDeserializationException.class,
                () -> sut.openObject(Generator.generateUri().toString(), Vocabulary.IS_MEMBER_OF,
                        Collections.singletonList(Vocabulary.ORGANIZATION)));
        assertThat(result.getMessage(), containsString("Encountered multiple values of property"));
    }

    @Test
    void isPropertyDeserializableReturnsTrueForMappedProperty() {
        sut.openObject(TestUtil.PALMER_URI.toString(), Person.class);
        assertTrue(sut.isPropertyDeserializable(Vocabulary.FIRST_NAME));
    }

    @Test
    void isPropertyDeserializableReturnsFalseForUnknownProperty() {
        sut.openObject(Generator.generateUri().toString(), Organization.class);
        assertFalse(sut.isPropertyDeserializable(Vocabulary.IS_ADMIN));
    }

    @Test
    void isPropertyDeserializableReturnsFalseForReadOnlyProperty() {
        sut.openObject(TestUtil.PALMER_URI.toString(), Study.class);
        assertFalse(sut.isPropertyDeserializable(Vocabulary.NUMBER_OF_PEOPLE_INVOLVED));
    }

    @Test
    void addNodeReferenceAddsItemToAnnotationPropertyCollectionWithObjectElements() {
        final URI nodeId = Generator.generateUri();
        sut.openObject(Generator.generateUri().toString(), ObjectWithAnnotationProperties.class);
        sut.openCollection(Vocabulary.ORIGIN);
        sut.addNodeReference(nodeId.toString());
        sut.closeCollection();
        final ObjectWithAnnotationProperties object = (ObjectWithAnnotationProperties) sut.getCurrentRoot();
        assertTrue(object.getOrigins().contains(nodeId));
    }

    @Test
    void addNodeReferenceAddsItemToAnnotationPropertyWithTypeObject() {
        final URI nodeId = Generator.generateUri();
        sut.openObject(Generator.generateUri().toString(), ObjectWithAnnotationProperty.class);
        sut.addNodeReference(Vocabulary.CHANGED_VALUE, nodeId.toString());
        final ObjectWithAnnotationProperty result = (ObjectWithAnnotationProperty) sut.getCurrentRoot();
        assertEquals(nodeId, result.value);
    }

    @OWLClass(iri = Vocabulary.OBJECT_WITH_ANNOTATIONS)
    public static class ObjectWithAnnotationProperty {

        @Id
        private URI uri;

        @OWLAnnotationProperty(iri = Vocabulary.CHANGED_VALUE)
        private Object value;

        public ObjectWithAnnotationProperty() {
        }
    }
}
