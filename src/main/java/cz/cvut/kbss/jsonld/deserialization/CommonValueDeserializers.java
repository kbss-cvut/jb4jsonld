package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.deserialization.datetime.*;

import java.time.*;
import java.util.*;

/**
 * Manages deserializers for one deserialization process.
 */
public class CommonValueDeserializers implements ValueDeserializers {

    private final Map<Class<?>, ValueDeserializer<?>> deserializers = new HashMap<>();

    public CommonValueDeserializers() {
        initBuiltInDeserializers();
    }

    private void initBuiltInDeserializers() {
        final OffsetDateTimeDeserializer coreDatetimeDeserializer = new OffsetDateTimeDeserializer();
        final OffsetTimeDeserializer coreTimeDeserializer = new OffsetTimeDeserializer();
        deserializers.put(OffsetDateTime.class, coreDatetimeDeserializer);
        deserializers.put(LocalDateTime.class, new LocalDateTimeDeserializer(coreDatetimeDeserializer));
        deserializers.put(ZonedDateTime.class, new ZonedDateTimeDeserializer(coreDatetimeDeserializer));
        deserializers.put(Date.class, new DateDeserializer(coreDatetimeDeserializer));
        deserializers.put(OffsetTime.class, coreTimeDeserializer);
        deserializers.put(LocalTime.class, new LocalTimeDeserializer(coreTimeDeserializer));
    }

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
