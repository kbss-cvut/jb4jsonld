package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.deserialization.datetime.DateDeserializer;
import cz.cvut.kbss.jsonld.deserialization.datetime.LocalDateTimeDeserializer;
import cz.cvut.kbss.jsonld.deserialization.datetime.OffsetDateTimeDeserializer;
import cz.cvut.kbss.jsonld.deserialization.datetime.ZonedDateTimeDeserializer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
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
        final OffsetDateTimeDeserializer coreDeserializer = new OffsetDateTimeDeserializer();
        deserializers.put(OffsetDateTime.class, coreDeserializer);
        deserializers.put(LocalDateTime.class, new LocalDateTimeDeserializer(coreDeserializer));
        deserializers.put(ZonedDateTime.class, new ZonedDateTimeDeserializer(coreDeserializer));
        deserializers.put(Date.class, new DateDeserializer(coreDeserializer));
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
