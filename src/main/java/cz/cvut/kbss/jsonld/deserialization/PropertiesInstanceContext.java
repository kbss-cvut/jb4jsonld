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
package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.deserialization.util.DataTypeTransformer;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class PropertiesInstanceContext extends InstanceContext<Map> {

    private final Field propertiesField;
    private final String property;

    PropertiesInstanceContext(Map<?, ?> instance, String property, Field propertiesField) {
        super(instance, Collections.emptyMap());
        this.property = property;
        this.propertiesField = propertiesField;
    }

    @Override
    void addItem(Object item) {
        final Class<?> keyType = BeanClassProcessor.getMapKeyType(propertiesField);
        final Object typedProperty = DataTypeTransformer.transformValue(property, keyType);
        final Class<?> valueType = BeanClassProcessor.getMapValueType(propertiesField);
        if (Collection.class.isAssignableFrom(valueType)) {
            Collection values;
            if (instance.containsKey(typedProperty)) {
                values = (Collection) instance.get(typedProperty);
            } else {
                values = BeanClassProcessor.createCollection(valueType);
                instance.put(typedProperty, values);
            }
            final Class<?> itemType = BeanClassProcessor.getMapGenericValueType(propertiesField);
            final Object itemValue = itemType != null ? DataTypeTransformer.transformValue(item, itemType) : item;
            values.add(itemValue);
        } else {
            if (instance.containsKey(typedProperty)) {
                throw JsonLdDeserializationException.singularAttributeCardinalityViolated(property, propertiesField);
            }
            final Object typedValue = DataTypeTransformer.transformValue(item, valueType);
            instance.put(typedProperty, typedValue);
        }
    }

    @Override
    Class<?> getItemType() {
        final Class<?> mapValueType = BeanClassProcessor.getMapValueType(propertiesField);
        return Collection.class.isAssignableFrom(mapValueType) ?
               BeanClassProcessor.getMapGenericValueType(propertiesField) : mapValueType;
    }
}
