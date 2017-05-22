package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.User;
import org.junit.Test;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TypesContextTest {

    @Test
    public void addItemSkipsTypeDeclaredOnClass() {
        final TypesContext<Set<String>, String> context = new TypesContext<>(new HashSet<>(), Collections.emptyMap(),
                String.class, User.class);
        context.addItem(Vocabulary.USER);
        context.addItem(Vocabulary.AGENT);
        context.addItem(Vocabulary.EMPLOYEE);
        assertTrue(context.getInstance().contains(Vocabulary.AGENT));
        assertTrue(context.getInstance().contains(Vocabulary.EMPLOYEE));
        assertFalse(context.getInstance().contains(Vocabulary.USER));
    }

    @Test
    public void addItemTransformsValueToElementTypeUri() {
        final TypesContext<Set<URI>, URI> context = new TypesContext<>(new HashSet<>(), Collections.emptyMap(),
                URI.class, User.class);
        context.addItem(Vocabulary.USER);
        context.addItem(Vocabulary.AGENT);
        context.addItem(Vocabulary.EMPLOYEE);
        assertTrue(context.getInstance().contains(URI.create(Vocabulary.AGENT)));
        assertTrue(context.getInstance().contains(URI.create(Vocabulary.EMPLOYEE)));
        assertFalse(context.getInstance().contains(URI.create(Vocabulary.USER)));
    }
}