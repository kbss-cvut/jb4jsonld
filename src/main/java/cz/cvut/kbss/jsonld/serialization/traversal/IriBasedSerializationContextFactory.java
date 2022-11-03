package cz.cvut.kbss.jsonld.serialization.traversal;

import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;

import java.lang.reflect.Field;

/**
 * Default implementation of {@link SerializationContextFactory}.
 * <p>
 * Resolves attribute identifiers based on the mapped IRIs.
 */
public class IriBasedSerializationContextFactory implements SerializationContextFactory {

    @Override
    public <T> SerializationContext<T> create(T value) {
        return new SerializationContext<>(value);
    }

    @Override
    public <T> SerializationContext<T> create(Field field, T value) {
        return new SerializationContext<>(field, value);
    }

    @Override
    public <T> SerializationContext<T> createWithAttributeId(Field field, T value) {
        return new SerializationContext<>(BeanAnnotationProcessor.getAttributeIdentifier(field), field, value);
    }
}
