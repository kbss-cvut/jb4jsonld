/**
 * Copyright (C) 2020 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.common;

import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.Properties;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.exception.BeanProcessingException;
import cz.cvut.kbss.jsonld.exception.TargetTypeException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cz.cvut.kbss.jsonld.common.BeanClassProcessor.createCollection;
import static cz.cvut.kbss.jsonld.common.BeanClassProcessor.getCollectionItemType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

class BeanClassProcessorTest {

    @Test
    void createNewInstanceCreatesNewClassInstance() {
        final Person result = BeanClassProcessor.createInstance(Person.class);
        assertNotNull(result);
    }

    @Test
    void createNewInstanceThrowsBeanProcessingExceptionWhenNoArgConstructorIsMissing() {
        final BeanProcessingException result = assertThrows(BeanProcessingException.class,
                () -> BeanClassProcessor.createInstance(ClassWithoutPublicCtor.class));
        assertEquals("Class " + ClassWithoutPublicCtor.class + " is missing a public no-arg constructor.",
                result.getMessage());
    }

    private static class ClassWithoutPublicCtor {
        private String name;

        public ClassWithoutPublicCtor(String name) {
            this.name = name;
        }
    }

    @Test
    void testCreateCollectionOfListType() {
        final Collection<?> res = createCollection(CollectionType.LIST);
        assertTrue(res instanceof List);
    }

    @Test
    void testCreateCollectionOfSetType() {
        final Collection<?> res = createCollection(CollectionType.SET);
        assertTrue(res instanceof Set);
    }

    @Test
    void testCreateCollectionFromSetField() throws Exception {
        final Collection<?> res = createCollection(Organization.class.getDeclaredField("employees"));
        assertTrue(res instanceof Set);
    }

    @Test
    void testCreateCollectionFromListField() throws Exception {
        final Collection<?> res = createCollection(ClassWithListField.class.getDeclaredField("list"));
        assertTrue(res instanceof List);
    }

    private static class ClassWithListField {

        @OWLDataProperty(iri = "http://krizik.felk.cvut.cz/ontologies/jb4jsonld/List")
        private List<Integer> list;
    }

    @Test
    void createCollectionFromFieldThrowsIllegalArgumentForNonCollectionField() {
        final IllegalArgumentException result = assertThrows(IllegalArgumentException.class,
                () -> createCollection(Person.class.getDeclaredField("firstName")));
        assertEquals(String.class + " is not a supported collection type.", result.getMessage());
    }

    @Test
    void getCollectionItemTypeReturnsDeclaredCollectionGenericArgument() throws Exception {
        final Class<?> res = getCollectionItemType(Organization.class.getDeclaredField("employees"));
        assertEquals(Employee.class, res);
    }

    @Test
    void getCollectionItemThrowsBeanProcessingExceptionWhenFieldIsNotCollection() throws Exception {
        final BeanProcessingException result = assertThrows(BeanProcessingException.class,
                () -> getCollectionItemType(Person.class.getDeclaredField("firstName")));
        assertEquals("Field " + Person.class.getDeclaredField("firstName") + " is not of parametrized type.",
                result.getMessage());
    }

    @Test
    void verifyPropertiesFieldTypeThrowsExceptionWhenFieldIsNotMap() throws Exception {
        final Field field = ClassWithInvalidProperties.class.getDeclaredField("properties");
        final TargetTypeException result = assertThrows(TargetTypeException.class,
                () -> BeanClassProcessor.verifyPropertiesFieldType(field));
        assertThat(result.getMessage(), containsString("Properties field " + field + " must be a java.util.Map"));
    }

    private static class ClassWithInvalidProperties {
        @Properties
        private List<String> properties;
    }

    @Test
    void getMapValueTypeReturnsValueType() throws Exception {
        final Field field = Person.class.getDeclaredField("properties");
        assertEquals(Set.class, BeanClassProcessor.getMapValueType(field));
    }

    @Test
    void getMapKeyTypeReturnsKeyType() throws Exception {
        final Field field = Person.class.getDeclaredField("properties");
        assertEquals(String.class, BeanClassProcessor.getMapKeyType(field));
    }

    @Test
    void getMapValueTypeThrowsBeanProcessingExceptionWhenFieldIsNotMap() throws Exception {
        final Field field = ClassWithInvalidProperties.class.getDeclaredField("properties");
        final BeanProcessingException result = assertThrows(BeanProcessingException.class,
                () -> BeanClassProcessor.getMapValueType(field));
        assertThat(result.getMessage(),
                containsString("Unable to determine declared Map value type of field " + field));
    }

    @Test
    void getMapGenericValueTypeDeterminesCorrectItemTypeOfMapValueWhenItIsCollection() throws Exception {
        final Field field = Person.class.getDeclaredField("properties");
        assertEquals(String.class, BeanClassProcessor.getMapGenericValueType(field));
    }

    @Test
    void getMapGenericValueTypeThrowsBeanProcessingExceptionWhenMapValueTypeIsNotGeneric() throws Exception {
        final Field field = NonGenericValue.class.getDeclaredField("properties");
        final BeanProcessingException result = assertThrows(BeanProcessingException.class,
                () -> BeanClassProcessor.getMapGenericValueType(field));
        assertEquals("Expected map value type to be generic. Field: " + field, result.getMessage());
    }

    private static class NonGenericValue {
        @Properties
        private Map<String, String> properties;
    }

    @Test
    void getMapGenericValueTypeReturnsNullWhenGenericValueTypeIsUnspecified() throws Exception {
        final Field field = UndefinedGenericValueType.class.getDeclaredField("properties");
        assertNull(BeanClassProcessor.getMapGenericValueType(field));
    }

    private static class UndefinedGenericValueType {
        @Properties
        private Map<URI, Set<?>> properties;
    }

    @Test
    void getMapGenericValueTypeReturnsNullForRawValueType() throws Exception {
        final Field field = RawValueType.class.getDeclaredField("properties");
        assertNull(BeanClassProcessor.getMapGenericValueType(field));
    }

    private static class RawValueType {
        @Properties
        private Map<String, Set> properties;
    }
}
