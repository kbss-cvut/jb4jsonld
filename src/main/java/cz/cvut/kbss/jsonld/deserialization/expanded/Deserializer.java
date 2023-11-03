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
package cz.cvut.kbss.jsonld.deserialization.expanded;

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.deserialization.InstanceBuilder;
import cz.cvut.kbss.jsonld.deserialization.util.ValueUtils;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

abstract class Deserializer<X> {

    final InstanceBuilder instanceBuilder;
    final DeserializerConfig config;

    Deserializer(InstanceBuilder instanceBuilder, DeserializerConfig config) {
        this.instanceBuilder = instanceBuilder;
        this.config = config;
    }

    Configuration configuration() {
        return config.getConfiguration();
    }

    <T> Class<? extends T> resolveTargetClass(JsonObject jsonRoot, Class<T> resultClass) {
        if (BeanClassProcessor.isIdentifierType(resultClass)) {
            return resultClass;
        }
        final List<String> types = getObjectTypes(jsonRoot);
        return config.getTargetResolver().getTargetClass(resultClass, types);
    }

    List<String> getObjectTypes(JsonObject jsonLdObject) {
        final JsonValue types = jsonLdObject.get(JsonLd.TYPE);
        if (types == null) {
            return Collections.emptyList();
        }
        assert types.getValueType() == JsonValue.ValueType.ARRAY;
        return types.asJsonArray().stream().map(ValueUtils::stringValue).collect(Collectors.toList());
    }

    abstract void processValue(X value);
}
