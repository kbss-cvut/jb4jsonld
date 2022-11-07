package cz.cvut.kbss.jsonld.serialization.traversal;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;

import java.lang.reflect.Field;
import java.util.Set;

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
    public <T> SerializationContext<T> createForProperties(Field field, T value) {
        return new SerializationContext<>(field, value);
    }

    @Override
    public <T> SerializationContext<T> createForAttribute(Field field, T value) {
        return new SerializationContext<>(BeanAnnotationProcessor.getAttributeIdentifier(field), field, value);
    }

    @Override
    public SerializationContext<String> createForIdentifier(Field field, String value) {
        return new SerializationContext<>(JsonLd.ID, field, value);
    }

    @Override
    public SerializationContext<Set<String>> createForTypes(Field field, Set<String> value) {
        return new SerializationContext<>(JsonLd.TYPE, field, value);
    }
}
