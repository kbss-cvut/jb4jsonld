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

import cz.cvut.kbss.jopa.datatype.exception.DatatypeMappingException;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;

import java.util.Collection;
import java.util.Map;

class CollectionInstanceContext<T extends Collection> extends InstanceContext<T> {

    private final Class<?> targetType;

    CollectionInstanceContext(T instance, Map<String, Object> knownInstances) {
        super(instance, knownInstances);
        this.targetType = null;
    }

    CollectionInstanceContext(T instance, Class<?> targetType, Map<String, Object> knownInstances) {
        super(instance, knownInstances);
        this.targetType = targetType;
    }

    /**
     * Adds the specified item into this collection.
     *
     * @param item The item to add
     */
    @Override
    void addItem(Object item) {
        if (targetType == null) {
            instance.add(item);
            return;
        }
        try {
            final Object toAdd = resolveAssignableValue(targetType, item);
            instance.add(toAdd);
        } catch (DatatypeMappingException e) {
            throw new JsonLdDeserializationException("Type mismatch when adding item " + item + " to collection.", e);
        }
    }

    @Override
    Class<?> getItemType() {
        return targetType;
    }
}
