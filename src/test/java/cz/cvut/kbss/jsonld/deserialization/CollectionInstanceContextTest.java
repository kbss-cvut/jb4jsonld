package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.model.User;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class CollectionInstanceContextTest {

    @Test
    public void addItemAddsObjectToCollectionInTheContext() {
        final InstanceContext<?> ctx = new CollectionInstanceContext<>(new HashSet<>(), Collections.emptyMap());
        final User u = Generator.generateUser();
        ctx.addItem(u);
        final Collection<?> col = (Collection<?>) ctx.getInstance();
        assertEquals(1, col.size());
        assertSame(u, col.iterator().next());
    }
}
