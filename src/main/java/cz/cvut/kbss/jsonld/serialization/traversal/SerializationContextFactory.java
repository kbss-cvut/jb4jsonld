package cz.cvut.kbss.jsonld.serialization.traversal;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.serialization.context.JsonLdContext;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * Creates {@link SerializationContext}s based on attribute/field being processed.
 */
public class SerializationContextFactory {

    private final JsonLdContext jsonLdContext;

    public SerializationContextFactory(JsonLdContext jsonLdContext) {
        this.jsonLdContext = jsonLdContext;
    }

    public <T> SerializationContext<T> create(T value) {
        return new SerializationContext<>(value, jsonLdContext);
    }

    public <T> SerializationContext<T> createForProperties(Field field, T value) {
        assert BeanAnnotationProcessor.isPropertiesField(field);

        return new SerializationContext<>(field, value, jsonLdContext);
    }

    public <T> SerializationContext<T> createForAttribute(Field field, T value) {
        return new SerializationContext<>(BeanAnnotationProcessor.getAttributeIdentifier(field), field, value,
                                          jsonLdContext);
    }

    public SerializationContext<String> createForIdentifier(Field field, String value) {
        return new SerializationContext<>(JsonLd.ID, field, value, jsonLdContext);
    }

    public SerializationContext<Set<String>> createForTypes(Field field, Set<String> value) {
        return new SerializationContext<>(JsonLd.TYPE, field, value, jsonLdContext);
    }
}
