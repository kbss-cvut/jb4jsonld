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
package cz.cvut.kbss.jsonld.serialization.traversal;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.serialization.context.JsonLdContext;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * Creates {@link SerializationContext}s based on attribute/field being processed.
 */
public class SerializationContextFactory {

    private final JsonLdContext rootContext;

    public SerializationContextFactory(JsonLdContext rootContext) {
        this.rootContext = rootContext;
    }

    public <T> SerializationContext<T> create(T value) {
        // Create root serialization context
        return new SerializationContext<>(value, rootContext);
    }

    public <T> SerializationContext<T> create(T value, SerializationContext<?> current) {
        return new SerializationContext<>(value, current.getJsonLdContext());
    }

    public <T> SerializationContext<T> createForProperties(Field field, T value, SerializationContext<?> current) {
        assert BeanAnnotationProcessor.isPropertiesField(field);

        return new SerializationContext<>(field, value, current.getJsonLdContext());
    }

    public <T> SerializationContext<T> createForAttribute(Field field, T value, SerializationContext<?> current) {
        return new SerializationContext<>(BeanAnnotationProcessor.getAttributeIdentifier(field), field, value,
                                          current.getJsonLdContext());
    }

    public SerializationContext<String> createForIdentifier(Field field, String value,
                                                            SerializationContext<?> current) {
        return new SerializationContext<>(JsonLd.ID, field, value, current.getJsonLdContext());
    }

    public SerializationContext<Set<String>> createForTypes(Field field, Set<String> value,
                                                            SerializationContext<?> current) {
        return new SerializationContext<>(JsonLd.TYPE, field, value, current.getJsonLdContext());
    }
}
