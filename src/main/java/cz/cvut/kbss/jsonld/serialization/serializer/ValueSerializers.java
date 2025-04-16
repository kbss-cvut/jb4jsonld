/*
 * JB4JSON-LD
 * Copyright (C) 2025 Czech Technical University in Prague
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

import cz.cvut.kbss.jsonld.common.Configurable;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.IndividualSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.util.Optional;
import java.util.Set;

/**
 * Provides serializers for JSON-LD tree building.
 */
public interface ValueSerializers extends Configurable {

    /**
     * Checks whether a custom serializer is registered for the specified type.
     *
     * @param type Type to check for custom serializer for
     * @param <T>  Type of value
     * @return Whether a custom serializer exists
     */
    <T> boolean hasCustomSerializer(Class<T> type);

    /**
     * Gets a custom serializer for the specified serialization context.
     *
     * @param ctx Context representing the value to serialize
     * @param <T> Type of the value
     * @return Optional containing the custom serializer registered for the specified value or an empty optional if there is no custom
     * serializer registered
     * @see #getOrDefault(SerializationContext)
     */
    <T> Optional<ValueSerializer<T>> getSerializer(SerializationContext<T> ctx);

    /**
     * Gets a custom serializer registered for the specified serialization context or a default serializer if there is no custom one registered.
     *
     * @param ctx Context representing the value to serialize
     * @param <T> Type of the value
     * @return Value serializer for the specified context
     * @see #getSerializer(SerializationContext)
     */
    <T> ValueSerializer<T> getOrDefault(SerializationContext<T> ctx);

    /**
     * Registers the specified serializer for the specified type.
     *
     * @param forType    Type to be serialized using the specified serializer
     * @param serializer Serializer to register
     * @param <T>        Value type
     */
    <T> void registerSerializer(Class<T> forType, ValueSerializer<? super T> serializer);

    ValueSerializer<String> getIdentifierSerializer();

    void registerIdentifierSerializer(ValueSerializer<String> idSerializer);

    ValueSerializer<Set<String>> getTypesSerializer();

    void registerTypesSerializer(ValueSerializer<Set<String>> typesSerializer);

    ValueSerializer<?> getIndividualSerializer();

    void registerIndividualSerializer(ValueSerializer<?> individualSerializer);
}
