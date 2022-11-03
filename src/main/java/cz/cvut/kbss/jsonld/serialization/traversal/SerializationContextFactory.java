package cz.cvut.kbss.jsonld.serialization.traversal;

import java.lang.reflect.Field;

/**
 * Creates {@link SerializationContext} instances.
 */
public interface SerializationContextFactory {

    <T> SerializationContext<T> create(T value);

    <T> SerializationContext<T> create(Field field, T value);

    <T> SerializationContext<T> createWithAttributeId(Field field, T value);
}
