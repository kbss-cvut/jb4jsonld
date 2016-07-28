package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.environment.model.User;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class InstanceContextTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void addItemAddsObjectToCollectionInTheContext() {
        final InstanceContext<?> ctx = new InstanceContext<>(new HashSet<>());
        final User u = Generator.generateUser();
        ctx.addItem(u);
        final Collection<?> col = (Collection<?>) ctx.getInstance();
        assertEquals(1, col.size());
        assertSame(u, col.iterator().next());
    }

    @Test(expected = JsonLdDeserializationException.class)
    public void addItemsThrowsExceptionWhenCurrentInstanceIsNotCollection() {
        final InstanceContext<Person> ctx = new InstanceContext<>(new Person(),
                BeanAnnotationProcessor.mapSerializableFields(
                        Person.class));
        ctx.addItem(Generator.generateUser());
    }

    @Test
    public void setFieldValueSetsFieldValueOnInstance() throws Exception {
        final InstanceContext<Person> ctx = new InstanceContext<>(new Person(),
                BeanAnnotationProcessor.mapSerializableFields(
                        Person.class));
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
        final InstanceContext<Person> ctx = new InstanceContext<>(new Person(),
                BeanAnnotationProcessor.mapSerializableFields(
                        Person.class));
        ctx.setFieldValue(Person.class.getDeclaredField("firstName"), invalidValue);
    }
}
