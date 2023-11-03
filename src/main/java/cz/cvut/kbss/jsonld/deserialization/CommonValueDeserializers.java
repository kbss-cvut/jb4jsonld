/*
 * JB4JSON-LD
 * Copyright (C) 2023 Czech Technical University in Prague
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
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
        deserializers.put(Instant.class, new InstantDeserializer(coreDatetimeDeserializer));
        deserializers.put(OffsetTime.class, coreTimeDeserializer);
        deserializers.put(LocalTime.class, new LocalTimeDeserializer(coreTimeDeserializer));
        deserializers.put(LocalDate.class, new LocalDateDeserializer());
        deserializers.put(Duration.class, new DurationDeserializer());
        deserializers.put(Period.class, new PeriodDeserializer());
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
