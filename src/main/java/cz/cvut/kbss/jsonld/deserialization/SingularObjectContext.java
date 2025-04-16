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

import cz.cvut.kbss.jopa.datatype.exception.DatatypeMappingException;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

class SingularObjectContext<T> extends InstanceContext<T> {

    private final Map<String, Field> fieldMap;

    SingularObjectContext(T instance, Map<String, Field> fieldMap, Map<String, Object> knownInstances) {
        super(instance, knownInstances);
        this.fieldMap = fieldMap;
    }

    @Override
    Field getFieldForProperty(String property) {
        return fieldMap.get(property);
    }

    @Override
    void setFieldValue(Field field, Object value) {
        assert !(instance instanceof Collection);
        try {
            final Object toSet = resolveAssignableValue(field.getType(), value);
            BeanClassProcessor.setFieldValue(field, instance, toSet);
        } catch (DatatypeMappingException e) {
            throw new JsonLdDeserializationException("Type mismatch when setting value " + value + " on field " + field + ".", e);
        }
    }

    @Override
    boolean isPropertyMapped(String property) {
        return fieldMap.containsKey(property) || hasPropertiesField();
    }

    @Override
    boolean supports(String property) {
        if (!isPropertyMapped(property)) {
            return false;
        }
        return !fieldMap.containsKey(property) || BeanAnnotationProcessor.isWriteable(fieldMap.get(property));
    }
}
