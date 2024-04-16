/*
 * JB4JSON-LD
 * Copyright (C) 2024 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.serialization.serializer;

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.serialization.JsonLdSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.util.*;

/**
 * Manages serializers of non-object property values for a single {@link JsonLdSerializer} instance.
 * <p>
 * That is, if an object is not referenced by an object property mapped attribute, it will be serialized by a serializer
 * managed by this instance.
 */
public class LiteralValueSerializers implements ValueSerializers {

    private final Map<Class<?>, ValueSerializer<?>> serializers = new HashMap<>();

    private final ValueSerializer<?> defaultSerializer;

    private ValueSerializer<String> identifierSerializer;

    private ValueSerializer<Set<String>> typesSerializer;

    private ValueSerializer<?> individualSerializer;

    public LiteralValueSerializers(ValueSerializer<?> defaultSerializer) {
        this.defaultSerializer = Objects.requireNonNull(defaultSerializer);
    }

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
    public <T> void registerSerializer(Class<T> forType, ValueSerializer<? super T> serializer) {
        Objects.requireNonNull(forType);
        Objects.requireNonNull(serializer);
        serializers.put(forType, serializer);
    }

    @Override
    public ValueSerializer<String> getIdentifierSerializer() {
        return identifierSerializer;
    }

    @Override
    public void registerIdentifierSerializer(ValueSerializer<String> idSerializer) {
        this.identifierSerializer = Objects.requireNonNull(idSerializer);
    }

    @Override
    public ValueSerializer<Set<String>> getTypesSerializer() {
        return typesSerializer;
    }

    @Override
    public void registerTypesSerializer(ValueSerializer<Set<String>> typesSerializer) {
        this.typesSerializer = Objects.requireNonNull(typesSerializer);
    }

    @Override
    public ValueSerializer<?> getIndividualSerializer() {
        return individualSerializer;
    }

    @Override
    public void registerIndividualSerializer(ValueSerializer<?> individualSerializer) {
        this.individualSerializer = Objects.requireNonNull(individualSerializer);
    }

    @Override
    public void configure(Configuration configuration) {
        serializers.values().forEach(vs -> vs.configure(configuration));
        individualSerializer.configure(configuration);
        identifierSerializer.configure(configuration);
        typesSerializer.configure(configuration);
        defaultSerializer.configure(configuration);
    }
}
