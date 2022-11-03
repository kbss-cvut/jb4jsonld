package cz.cvut.kbss.jsonld.serialization.traversal;

import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.exception.JsonLdSerializationException;

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
        try {
            return new SerializationContext<>(BeanAnnotationProcessor.getAttributeIdentifier(field), field, value);
        } catch (JsonLdSerializationException e) {
            if (BeanAnnotationProcessor.isPropertiesField(field)) {
                return new SerializationContext<T>(field, value);
            }
            throw e;
        }
    }
}
