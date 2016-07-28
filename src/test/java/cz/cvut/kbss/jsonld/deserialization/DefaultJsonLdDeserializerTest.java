package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.common.CollectionType;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Person;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import static org.junit.Assert.*;

public class DefaultJsonLdDeserializerTest {

    private JsonLdDeserializer deserializer = new DefaultJsonLdDeserializer();

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
    private Stack<InstanceContext> getOpenInstances() throws Exception {
        final Field stackField = DefaultJsonLdDeserializer.class.getDeclaredField("openInstances");
        stackField.setAccessible(true);
        return (Stack<InstanceContext>) stackField.get(deserializer);
    }

    @Test
    public void closeObjectsPopsPreviousInstanceFromTheStack() throws Exception {
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
    public void addValueAddsValuesToCurrentlyOpenCollection() throws Exception {
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
}
