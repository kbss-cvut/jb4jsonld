package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.deserialization.util.TargetClassResolver;

/**
 * Current context of the deserialization process.
 * <p>
 * Provides value deserializers additional information they may require to deserialize values.
 *
 * @param <T> Deserialization target type
 */
public class DeserializationContext<T> {

    private final Class<T> targetType;

    private final TargetClassResolver classResolver;

    public DeserializationContext(Class<T> targetType, TargetClassResolver classResolver) {
        this.targetType = targetType;
        this.classResolver = classResolver;
    }

    public Class<T> getTargetType() {
        return targetType;
    }

    public TargetClassResolver getClassResolver() {
        return classResolver;
    }
}
