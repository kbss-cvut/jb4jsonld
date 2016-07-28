package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.environment.model.User;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class InstanceContextTest {

    @Test
    public void addItemAddsObjectToCollectionInTheContext() {
        final InstanceContext ctx = new InstanceContext(new HashSet<>());
        final User u = Generator.generateUser();
        ctx.addItem(u);
        final Collection<?> col = (Collection<?>) ctx.getInstance();
        assertEquals(1, col.size());
        assertSame(u, col.iterator().next());
    }

    @Test(expected = JsonLdDeserializationException.class)
    public void addItemsThrowsExceptionWhenCurrentInstanceIsNotCollection() {
        final InstanceContext ctx = new InstanceContext(new Person(), BeanAnnotationProcessor.mapSerializableFields(
                Person.class));
        ctx.addItem(Generator.generateUser());
    }
}