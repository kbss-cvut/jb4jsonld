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

import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.deserialization.util.DataTypeTransformer;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

class TypesContext<T extends Collection<E>, E> extends InstanceContext<T> {

    private final Set<String> mappedTypes;
    private final Class<E> elementType;

    TypesContext(T instance, Map<String, Object> knownInstances, Class<E> elementType, Class<?> ownerType) {
        super(instance, knownInstances);
        this.elementType = elementType;
        this.mappedTypes = BeanAnnotationProcessor.getOwlClasses(ownerType);
    }

    @Override
    void addItem(Object item) {
        assert item instanceof String;
        if (mappedTypes.contains(item)) {
            return;
        }
        if (!elementType.isAssignableFrom(item.getClass())) {
            instance.add(DataTypeTransformer.transformValue(item, elementType));
        } else {
            instance.add(elementType.cast(item));
        }
    }

    @Override
    Class<?> getItemType() {
        return elementType;
    }
}
