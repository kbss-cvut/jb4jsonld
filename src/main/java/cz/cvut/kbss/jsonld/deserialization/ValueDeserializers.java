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
package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.common.Configurable;

import java.util.Optional;

/**
 * Manages custom deserializers.
 */
public interface ValueDeserializers extends Configurable {

    /**
     * Checks whether a custom deserializer is registered for the specified type.
     *
     * @param type Type to check for custom deserializer for
     * @param <T>  Type of value
     * @return Whether a custom deserializer exists
     */
    <T> boolean hasCustomDeserializer(Class<T> type);

    /**
     * Gets a custom deserializer for the specified deserialization context.
     *
     * @param ctx Context representing the deserialization
     * @param <T> Type of the value
     * @return Optional containing the custom deserializer registered for the specified type or an empty optional if there is none
     */
    <T> Optional<ValueDeserializer<T>> getDeserializer(DeserializationContext<T> ctx);

    /**
     * Registers the specified deserializer for the specified type.
     *
     * @param forType      Type to be deserialized using the specified deserializer
     * @param deserializer Deserializer to register
     * @param <T>          Value type
     */
    <T> void registerDeserializer(Class<T> forType, ValueDeserializer<T> deserializer);
}
