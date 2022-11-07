package cz.cvut.kbss.jsonld.serialization.traversal;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * Creates {@link SerializationContext} instances.
 */
public interface SerializationContextFactory {

    <T> SerializationContext<T> create(T value);

    <T> SerializationContext<T> createForAttribute(Field field, T value);

    <T> SerializationContext<T> createForProperties(Field field, T value);

    SerializationContext<String> createForIdentifier(Field field, String value);

    SerializationContext<Set<String>> createForTypes(Field field, Set<String> value);
}
