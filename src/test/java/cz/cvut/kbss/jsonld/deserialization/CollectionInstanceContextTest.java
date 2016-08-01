package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.User;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class CollectionInstanceContextTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void addItemAddsObjectToCollectionInTheContext() {
        final InstanceContext<?> ctx = new CollectionInstanceContext<>(new HashSet<>(), Collections.emptyMap());
        final User u = Generator.generateUser();
        ctx.addItem(u);
        final Collection<?> col = (Collection<?>) ctx.getInstance();
        assertEquals(1, col.size());
        assertSame(u, col.iterator().next());
    }

    @Test
    public void addItemResolvesReferenceToExistingInstanceAndAddsItIntoCollection() {
        final Employee e = Generator.generateEmployee();
        final InstanceContext<Set> ctx = new CollectionInstanceContext<>(new HashSet<>(), Employee.class,
                Collections.singletonMap(e.getUri().toString(), e));
        ctx.addItem(e.getUri().toString());
        assertTrue(ctx.getInstance().contains(e));
    }

    @Test
    public void addItemTransformsStringValuesToUris() {
        final InstanceContext<Set> ctx = new CollectionInstanceContext<>(new HashSet<>(), URI.class,
                Collections.emptyMap());
        final Set<User> users = Generator.generateUsers();
        for (User u : users) {
            ctx.addItem(u.getUri().toString());
        }
        users.forEach(u -> assertTrue(ctx.getInstance().contains(u.getUri())));
    }

    @Test
    public void addItemThrowsDeserializationExceptionWhenIncompatibleValueIsAdded() {
        thrown.expect(JsonLdDeserializationException.class);
        thrown.expectMessage(
                "Type mismatch. Unable to transform instance of type " + String.class + " to the expected type " +
                        Integer.class);
        final InstanceContext<Set> ctx = new CollectionInstanceContext<>(new HashSet<>(), Integer.class,
                Collections.emptyMap());
        ctx.addItem("Test");
    }
}
