package cz.cvut.kbss.jsonld.common;

import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.exception.BeanProcessingException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

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

    @Test
    public void testCreateCollectionFromSetField() throws Exception {
        final Collection<?> res = BeanClassProcessor.createCollection(Organization.class.getDeclaredField("employees"));
        assertTrue(res instanceof Set);
    }

    @Test
    public void testCreateCollectionFromListField() throws Exception {
        final Collection<?> res = BeanClassProcessor
                .createCollection(ClassWithListField.class.getDeclaredField("list"));
        assertTrue(res instanceof List);
    }

    private static class ClassWithListField {

        @OWLDataProperty(iri = "http://krizik.felk.cvut.cz/ontologies/jaxb-jsonld/List")
        private List<Integer> list;
    }

    @Test
    public void createCollectionFromFieldThrowsIllegalArgumentForNonCollectionField() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(
                "Field " + Person.class.getDeclaredField("firstName") + " is not of any supported collection type.");
        BeanClassProcessor.createCollection(Person.class.getDeclaredField("firstName"));
    }

    @Test
    public void getCollectionItemTypeReturnsDeclaredCollectionGenericArgument() throws Exception {
        final Class<?> res = BeanClassProcessor.getCollectionItemType(Organization.class.getDeclaredField("employees"));
        assertEquals(Employee.class, res);
    }

    @Test
    public void getCollectionItemThrowsBeanProcessingExceptionWhenFieldIsNotCollection() throws Exception {
        thrown.expect(BeanProcessingException.class);
        thrown.expectMessage(
                "Field " + Person.class.getDeclaredField("firstName") + "is not a collection-valued field.");
        BeanClassProcessor.getCollectionItemType(Person.class.getDeclaredField("firstName"));
    }
}
