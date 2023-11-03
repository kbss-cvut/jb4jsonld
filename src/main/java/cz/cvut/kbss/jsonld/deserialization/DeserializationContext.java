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
