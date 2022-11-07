package cz.cvut.kbss.jsonld.serialization.context;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.exception.JsonLdSerializationException;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContextFactory;

import java.lang.reflect.Field;
import java.util.Set;

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
    public <T> SerializationContext<T> createForProperties(Field field, T value) {
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
    public <T> SerializationContext<T> createForAttribute(Field field, T value) {
        final String attId = BeanAnnotationProcessor.getAttributeIdentifier(field);
        jsonLdContext.registerTermMapping(field.getName(), attId);
        return new SerializationContext<>(field.getName(), field, value);
    }

    @Override
    public SerializationContext<String> createForIdentifier(Field field, String value) {
        if (field != null) {
            jsonLdContext.registerTermMapping(field.getName(), JsonLd.ID);
            return new SerializationContext<>(field.getName(), field, value);
        }
        return new SerializationContext<>(JsonLd.ID, null, value);
    }

    @Override
    public SerializationContext<Set<String>> createForTypes(Field field, Set<String> value) {
        if (field != null) {
            jsonLdContext.registerTermMapping(field.getName(), JsonLd.TYPE);
            return new SerializationContext<>(field.getName(), field, value);
        }
        return new SerializationContext<>(JsonLd.TYPE, null, value);
    }
}
