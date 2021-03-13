package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ValueSerializers {

    private final Map<Class<?>, ValueSerializer<?>> serializers = new HashMap<>();

    private final ValueSerializer<?> defaultSerializer = new DefaultValueSerializer(new MultilingualStringSerializer());

    public <T> ValueSerializer<T> getSerializer(SerializationContext<T> ctx) {
        return (ValueSerializer<T>) serializers.getOrDefault(ctx.getValue().getClass(), defaultSerializer);
    }

    public <T> void registerSerializer(Class<T> forType, ValueSerializer<T> serializer) {
        Objects.requireNonNull(forType);
        Objects.requireNonNull(serializer);
        serializers.put(forType, serializer);
    }
}
