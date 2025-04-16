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
package cz.cvut.kbss.jsonld.deserialization.expanded;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.common.IdentifierUtil;
import cz.cvut.kbss.jsonld.deserialization.InstanceBuilder;
import cz.cvut.kbss.jsonld.deserialization.util.ValueUtils;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import cz.cvut.kbss.jsonld.exception.UnknownPropertyException;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

class ObjectDeserializer extends Deserializer<JsonObject> {

    private final String property;
    private final Class<?> targetClass;

    ObjectDeserializer(InstanceBuilder instanceBuilder, DeserializerConfig config, String property) {
        super(instanceBuilder, config);
        assert property != null;
        this.property = property;
        this.targetClass = null;
    }

    ObjectDeserializer(InstanceBuilder instanceBuilder, DeserializerConfig config, Class<?> targetClass) {
        super(instanceBuilder, config);
        assert targetClass != null;
        this.targetClass = targetClass;
        this.property = null;
    }

    @Override
    void processValue(JsonObject value) {
        openObject(value);
        final List<String> orderedProps = orderAttributesForProcessing(value);
        for (String property : orderedProps) {
            final boolean shouldSkip = shouldSkipProperty(property);
            if (shouldSkip) {
                continue;
            }
            assert value.get(property).getValueType() == JsonValue.ValueType.ARRAY;
            new CollectionDeserializer(instanceBuilder, config, property).processValue(value.getJsonArray(property));
        }
        instanceBuilder.closeObject();
    }

    private void openObject(JsonObject value) {
        try {
            if (property != null) {
                instanceBuilder.openObject(getId(value), property, getObjectTypes(value));
            } else {
                assert targetClass != null;
                final Class<?> cls = resolveTargetClass(value, targetClass);
                assert targetClass.isAssignableFrom(cls);
                instanceBuilder.openObject(getId(value), cls);
            }
        } catch (UnknownPropertyException e) {
            if (!configuration().is(ConfigParam.IGNORE_UNKNOWN_PROPERTIES)) {
                throw e;
            }
        }
    }

    private String getId(JsonObject object) {
        return object.containsKey(JsonLd.ID) ? ValueUtils.stringValue(object.get(JsonLd.ID)) : IdentifierUtil.generateBlankNodeId();
    }

    private List<String> orderAttributesForProcessing(JsonObject value) {
        final List<String> propertyOrder = getPropertyOrder();
        final Set<String> ordered = new HashSet<>(propertyOrder);
        final List<String> result = new ArrayList<>();
        propertyOrder.stream().filter(value::containsKey).forEach(result::add);

        value.keySet().stream().filter(p -> !ordered.contains(p)).forEach(result::add);
        return result;
    }

    private List<String> getPropertyOrder() {
        final Class<?> cls = instanceBuilder.getCurrentContextType();
        if (cls == null) {
            return Collections.emptyList();
        }
        final String[] attributeOrder = BeanAnnotationProcessor.getAttributeOrder(cls);
        if (attributeOrder.length == 0) {
            return Collections.emptyList();
        }
        final List<Field> fields = BeanAnnotationProcessor
                .getMarshallableFields(instanceBuilder.getCurrentContextType());
        final List<String> propertyOrder = new ArrayList<>(attributeOrder.length);
        for (String name : attributeOrder) {
            final Optional<Field> field = fields.stream().filter(f -> f.getName().equals(name)).findFirst();
            if (field.isEmpty()) {
                throw new JsonLdDeserializationException(
                        "Field called " + name + " declared in JsonLdAttributeOrder annotation not found in class " +
                                cls + ".");
            }
            propertyOrder.add(BeanAnnotationProcessor.getAttributeIdentifier(field.get()));
        }
        return propertyOrder;
    }

    private boolean shouldSkipProperty(String property) {
        if (JsonLd.ID.equals(property)) {
            return true;
        }
        if (!instanceBuilder.isPropertyDeserializable(property)) {
            throwUnknownPropertyIfNotIgnored(property);
            return true;
        }
        return false;
    }

    private void throwUnknownPropertyIfNotIgnored(String property) {
        if (!instanceBuilder.isPropertyMapped(property) && !configuration().is(ConfigParam.IGNORE_UNKNOWN_PROPERTIES)) {
            throw UnknownPropertyException.create(property, instanceBuilder.getCurrentContextType());
        }
    }
}
