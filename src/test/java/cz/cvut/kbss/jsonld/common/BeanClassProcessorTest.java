package cz.cvut.kbss.jsonld.common;

import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.exception.BeanProcessingException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BeanClassProcessorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void createNewInstanceCreatesNewClassInstance() {
        final Person result = BeanClassProcessor.createInstance(Person.class);
        assertNotNull(result);
    }

    @Test
    public void createNewInstanceThrowsBeanProcessingExceptionWhenNoArgConstructorIsMissing() {
        thrown.expect(BeanProcessingException.class);
        thrown.expectMessage("Class " + ClassWithoutPublicCtor.class + " is missing a public no-arg constructor.");
        BeanClassProcessor.createInstance(ClassWithoutPublicCtor.class);
    }

    private static class ClassWithoutPublicCtor {
        private String name;

        public ClassWithoutPublicCtor(String name) {
            this.name = name;
        }
    }

    @Test
    public void testCreateCollectionOfListType() {
        final Collection<?> res = BeanClassProcessor.createCollection(CollectionType.LIST);
        assertTrue(res instanceof List);
    }

    @Test
    public void testCreateCollectionOfSetType() {
        final Collection<?> res = BeanClassProcessor.createCollection(CollectionType.SET);
        assertTrue(res instanceof Set);
    }
}