package cz.cvut.kbss.jsonld.serialization.context;

import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.exception.JsonLdSerializationException;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContextFactory;

import java.lang.reflect.Field;

public class ContextMappingSerializationContextFactory implements SerializationContextFactory {

    private final JsonLdContext jsonLdContext;

    public ContextMappingSerializationContextFactory(JsonLdContext jsonLdContext) {
        this.jsonLdContext = jsonLdContext;
    }

    @Override
    public <T> SerializationContext<T> create(T value) {
        return new SerializationContext<>(value);
    }

    @Override
    public <T> SerializationContext<T> create(Field field, T value) {
        try {
            final String attId = BeanAnnotationProcessor.getAttributeIdentifier(field);
            jsonLdContext.registerTermMapping(field.getName(), attId);
            return new SerializationContext<>(field.getName(), field, value);
        } catch (JsonLdSerializationException e) {
            if (BeanAnnotationProcessor.isPropertiesField(field)) {
                return new SerializationContext<>(field, value);
            }
            throw e;
        }
    }

    @Override
    public <T> SerializationContext<T> createWithAttributeId(Field field, T value) {
        final String attId = BeanAnnotationProcessor.getAttributeIdentifier(field);
        jsonLdContext.registerTermMapping(field.getName(), attId);
        return new SerializationContext<>(field.getName(), field, value);
    }
}
