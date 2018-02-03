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
package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.CollectionType;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.environment.model.User;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.*;

import static org.junit.Assert.*;

public class DefaultInstanceBuilderTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private InstanceBuilder deserializer = new DefaultInstanceBuilder();

    @Test
    public void getCurrentRootReturnsNullIfThereIsNoRoot() {
        assertNull(deserializer.getCurrentRoot());
    }

    @Test
    public void openObjectCreatesNewInstanceOfSpecifiedClass() {
        deserializer.openObject(Person.class);
        final Object res = deserializer.getCurrentRoot();
        assertNotNull(res);
        assertTrue(res instanceof Person);
    }

    @Test
    public void openCollectionCreatesSet() {
        deserializer.openCollection(CollectionType.SET);
        assertTrue(deserializer.getCurrentRoot() instanceof Set);
    }

    @Test
    public void openCollectionInCollectionPushesOriginalCollectionToStack() throws Exception {
        deserializer.openCollection(CollectionType.SET);
        deserializer.openCollection(CollectionType.SET);
        assertTrue(deserializer.getCurrentRoot() instanceof Set);
        assertFalse(getOpenInstances().isEmpty());
        assertTrue(getOpenInstances().peek().getInstance() instanceof Set);
        assertNotSame(deserializer.getCurrentRoot(), getOpenInstances().peek().getInstance());
    }

    @Test
    public void openObjectAddsObjectToCurrentlyOpenCollectionAndBecomesCurrentInstance() throws Exception {
        deserializer.openCollection(CollectionType.SET);
        deserializer.openObject(Employee.class);
        final Object root = deserializer.getCurrentRoot();
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
        return (Stack<InstanceContext<?>>) stackField.get(deserializer);
    }

    private InstanceContext<?> getCurrentInstance() throws Exception {
        final Field instField = DefaultInstanceBuilder.class.getDeclaredField("currentInstance");
        instField.setAccessible(true);
        return (InstanceContext<?>) instField.get(deserializer);
    }

    @Test
    public void closeObjectPopsPreviousInstanceFromTheStack() throws Exception {
        deserializer.openCollection(CollectionType.SET);
        final Object originalRoot = deserializer.getCurrentRoot();
        assertTrue(originalRoot instanceof Set);
        deserializer.openObject(Employee.class);
        assertFalse(getOpenInstances().isEmpty());
        deserializer.closeObject();
        assertTrue(getOpenInstances().isEmpty());
        assertSame(originalRoot, deserializer.getCurrentRoot());
    }

    @Test
    public void closeObjectDoesNothingForRootObject() throws Exception {
        deserializer.openObject(Person.class);
        assertTrue(getOpenInstances().isEmpty());
        final Object root = deserializer.getCurrentRoot();
        deserializer.closeObject();
        assertTrue(getOpenInstances().isEmpty());
        assertSame(root, deserializer.getCurrentRoot());
    }

    @Test
    public void addValueAddsValuesToCurrentlyOpenCollection() {
        final List<Integer> items = new ArrayList<>();
        for (int i = 0; i < Generator.randomCount(10); i++) {
            items.add(Generator.randomCount(Integer.MAX_VALUE));
        }
        deserializer.openCollection(CollectionType.LIST);
        items.forEach(item -> deserializer.addValue(item));
        assertTrue(deserializer.getCurrentRoot() instanceof List);
        final List<?> result = (List<?>) deserializer.getCurrentRoot();
        for (int i = 0; i < items.size(); i++) {
            assertEquals(items.get(i), result.get(i));
        }
    }

    @Test
    public void openObjectByPropertyCreatesObjectOfCorrectType() {
        deserializer.openObject(Employee.class);
        deserializer.openObject(Vocabulary.IS_MEMBER_OF);
        assertTrue(deserializer.getCurrentRoot() instanceof Organization);
    }

    @Test
    public void openObjectByPropertySetsNewInstanceAsFieldValueAndReplacesCurrentContext() throws Exception {
        deserializer.openObject(Employee.class);
        deserializer.openObject(Vocabulary.IS_MEMBER_OF);
        assertFalse(getOpenInstances().isEmpty());
        final Object top = getOpenInstances().peek().getInstance();
        assertTrue(top instanceof Employee);
        assertNotNull(((Employee) top).getEmployer());
    }

    @Test
    public void addValueSetsPropertyValue() {
        deserializer.openObject(User.class);
        final String firstName = "Catherine";
        final String lastName = "Halsey";
        deserializer.addValue(Vocabulary.FIRST_NAME, firstName);
        deserializer.addValue(Vocabulary.LAST_NAME, lastName);
        deserializer.addValue(Vocabulary.IS_ADMIN, true);
        final User res = (User) deserializer.getCurrentRoot();
        assertEquals(firstName, res.getFirstName());
        assertEquals(lastName, res.getLastName());
        assertTrue(res.getAdmin());
    }

    @Test
    public void openCollectionByPropertyCreatesCollectionOfCorrectType() throws Exception {
        deserializer.openObject(Organization.class);
        deserializer.openCollection(Vocabulary.HAS_MEMBER);
        assertFalse(getOpenInstances().isEmpty());
        assertTrue(deserializer.getCurrentRoot() instanceof Set);
    }

    @Test
    public void openCollectionByPropertySetsFieldValueAndPushesCurrentInstanceToStack() throws Exception {
        deserializer.openObject(Organization.class);
        deserializer.openCollection(Vocabulary.HAS_MEMBER);
        assertFalse(getOpenInstances().isEmpty());
        assertTrue(getOpenInstances().peek().getInstance() instanceof Organization);
        final Organization org = (Organization) getOpenInstances().peek().getInstance();
        assertSame(deserializer.getCurrentRoot(), org.getEmployees());
    }

    @Test
    public void closeCollectionPopsLastInstanceFromStack() throws Exception {
        deserializer.openObject(Organization.class);
        final Object originalRoot = deserializer.getCurrentRoot();
        deserializer.openCollection(Vocabulary.HAS_MEMBER);
        assertFalse(getOpenInstances().isEmpty());
        deserializer.closeCollection();
        assertTrue(getOpenInstances().isEmpty());
        assertSame(originalRoot, deserializer.getCurrentRoot());
    }

    @Test
    public void closeCollectionDoesNothingWhenItIsTheTopElement() throws Exception {
        deserializer.openCollection(CollectionType.SET);
        assertTrue(getOpenInstances().isEmpty());
        final Object root = deserializer.getCurrentRoot();
        deserializer.closeCollection();
        assertTrue(getOpenInstances().isEmpty());
        assertSame(root, deserializer.getCurrentRoot());
    }

    @Test
    public void objectIsStoredInKnownInstancesWhenItsIdIsRead() throws Exception {
        final User user = Generator.generateUser();
        deserializer.openObject(User.class);
        deserializer.addValue(JsonLd.ID, user.getUri().toString());
        assertTrue(getKnownInstances().containsKey(user.getUri().toString()));
        assertTrue(getKnownInstances().get(user.getUri().toString()) instanceof User);

    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getKnownInstances() throws Exception {
        final Field field = DefaultInstanceBuilder.class.getDeclaredField("knownInstances");
        field.setAccessible(true);
        return (Map<String, Object>) field.get(deserializer);
    }

    @Test
    public void openCollectionCreatesTypesContextForTypesField() throws Exception {
        deserializer.openObject(User.class);
        deserializer.openCollection(JsonLd.TYPE);
        final InstanceContext<?> result = getCurrentInstance();
        assertTrue(result instanceof TypesContext);
        final TypesContext<?, ?> ctx = (TypesContext<?, ?>) result;
        assertEquals(String.class, ctx.getItemType());
    }

    @Test
    public void isPropertyMappedReturnsTrueForMappedProperty() {
        deserializer.openObject(Person.class);
        assertTrue(deserializer.isPropertyMapped(Vocabulary.FIRST_NAME));
    }

    @Test
    public void isPropertyMappedReturnsFalseForUnknownProperty() {
        deserializer.openObject(Organization.class);
        assertFalse(deserializer.isPropertyMapped(Vocabulary.IS_ADMIN));
    }

    @Test
    public void isPropertyMappedReturnsTrueForType() {
        deserializer.openObject(Person.class);
        assertTrue(deserializer.isPropertyMapped(JsonLd.TYPE));
    }

    @Test
    public void isPluralReturnsTrueForType() {
        deserializer.openObject(Person.class);
        assertTrue(deserializer.isPlural(JsonLd.TYPE));
    }

    @Test
    public void openCollectionOpensDummyCollectionContextForTypeInInstanceWithoutTypesField() throws Exception {
        deserializer.openObject(Person.class);
        assertTrue(getCurrentInstance() instanceof SingularObjectContext);
        deserializer.openCollection(JsonLd.TYPE);
        assertTrue(getCurrentInstance() instanceof DummyCollectionInstanceContext);
        deserializer.closeCollection();
        assertTrue(getCurrentInstance() instanceof SingularObjectContext);
    }

    @Test
    public void addingTypesToInstanceWithoutTypesFieldDoesNothing() {
        deserializer.openObject(Person.class);
        deserializer.openCollection(JsonLd.TYPE);
        deserializer.addValue(Vocabulary.EMPLOYEE);
        deserializer.addValue(Vocabulary.USER);
        deserializer.closeCollection();
        assertNotNull(deserializer.getCurrentRoot());
    }

    @Test
    public void openCollectionCreatesPropertiesContextAndSetsNewPropertiesMapOnTargetInstanceWhenItDoesNotExist() {
        deserializer.openObject(Person.class);
        final Person instance = (Person) deserializer.getCurrentRoot();
        assertNull(instance.getProperties());
        deserializer.openCollection(Vocabulary.IS_ADMIN);
        assertNotNull(instance.getProperties());
    }

    @Test
    public void openCollectionReusesPropertiesInstanceWhenItAlreadyExistsOnTargetInstance() {
        deserializer.openObject(Person.class);
        final Person instance = (Person) deserializer.getCurrentRoot();
        assertNull(instance.getProperties());
        deserializer.openCollection(Vocabulary.IS_ADMIN);
        final Map<String, Set<String>> propsMap = instance.getProperties();
        deserializer.closeCollection();
        deserializer.openCollection(Vocabulary.USERNAME);
        assertSame(propsMap, instance.getProperties());
    }

    @Test
    public void openObjectCreatesIdentifierContextWhenTargetTypeIsIdentifier() throws Exception {
        deserializer.openCollection(CollectionType.LIST);
        deserializer.openObject(URI.class);
        final InstanceContext<?> ctx = getCurrentInstance();
        assertTrue(ctx instanceof NodeReferenceContext);
    }

    @Test
    public void openObjectCreatesIdentifierContextForPropertyWithPlainIdentifierTargetFieldType() throws Exception {
        deserializer.openObject(Organization.class);
        deserializer.openObject(Vocabulary.ORIGIN);
        final InstanceContext<?> ctx = getCurrentInstance();
        assertTrue(ctx instanceof NodeReferenceContext);
    }
}
