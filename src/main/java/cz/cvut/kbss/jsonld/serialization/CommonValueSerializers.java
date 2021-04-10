package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Manages serializers for a single {@link JsonLdSerializer} instance.
 */
public class CommonValueSerializers implements ValueSerializers {

    private final Map<Class<?>, ValueSerializer<?>> serializers = new HashMap<>();

    private final ValueSerializer<?> defaultSerializer = new DefaultValueSerializer(new MultilingualStringSerializer());

    @Override
    public <T> boolean hasCustomSerializer(Class<T> type) {
        return serializers.containsKey(type);
    }

    @Override
    public <T> Optional<ValueSerializer<T>> getSerializer(SerializationContext<T> ctx) {
        return Optional.ofNullable((ValueSerializer<T>) serializers.get(ctx.getValue().getClass()));
    }

    @Override
    public <T> ValueSerializer<T> getOrDefault(SerializationContext<T> ctx) {
        return (ValueSerializer<T>) serializers.getOrDefault(ctx.getValue().getClass(), defaultSerializer);
    }

    @Override
    public <T> void registerSerializer(Class<T> forType, ValueSerializer<T> serializer) {
        Objects.requireNonNull(forType);
        Objects.requireNonNull(serializer);
        serializers.put(forType, serializer);
    }
}
