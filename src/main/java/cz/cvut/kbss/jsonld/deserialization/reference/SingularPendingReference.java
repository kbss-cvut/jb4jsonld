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
package cz.cvut.kbss.jsonld.deserialization.reference;

import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.exception.TargetTypeException;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a singular pending reference.
 * <p>
 * That is, a singular attribute referencing an object.
 */
public final class SingularPendingReference implements PendingReference {

    private final Object targetObject;

    private final Field targetField;

    public SingularPendingReference(Object targetObject, Field targetField) {
        this.targetObject = Objects.requireNonNull(targetObject);
        this.targetField = Objects.requireNonNull(targetField);
    }

    @Override
    public void apply(Object referencedObject) {
        assert referencedObject != null;
        if (!targetField.getType().isAssignableFrom(referencedObject.getClass())) {
            throw new TargetTypeException(
                    "Cannot assign referenced object " + referencedObject + " of type " + referencedObject
                            .getClass() + " to field " + targetField);
        }
        BeanClassProcessor.setFieldValue(targetField, targetObject, referencedObject);
    }

    @Override
    public Optional<Class<?>> getTargetType() {
        return Optional.of(targetField.getType());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SingularPendingReference that = (SingularPendingReference) o;
        return targetObject.equals(that.targetObject) && targetField.equals(that.targetField);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetObject, targetField);
    }
}
