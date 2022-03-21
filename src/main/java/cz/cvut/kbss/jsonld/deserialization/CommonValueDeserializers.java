package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Manages deserializers for one deserialization process.
 */
public class CommonValueDeserializers implements ValueDeserializers {

    private final Map<Class<?>, ValueDeserializer<?>> deserializers = new HashMap<>();

    @Override
    public <T> boolean hasCustomDeserializer(Class<T> type) {
        return deserializers.containsKey(type);
    }

    @Override
    public <T> Optional<ValueDeserializer<T>> getDeserializer(DeserializationContext<T> ctx) {
        Objects.requireNonNull(ctx);
        return Optional.ofNullable((ValueDeserializer<T>) deserializers.get(ctx.getTargetType()));
    }

    @Override
    public <T> void registerDeserializer(Class<T> forType, ValueDeserializer<T> deserializer) {
        Objects.requireNonNull(forType);
        Objects.requireNonNull(deserializer);
        deserializers.put(forType, deserializer);
    }

    @Override
    public void configure(Configuration configuration) {
        deserializers.values().forEach(dsr -> dsr.configure(configuration));
    }
}
