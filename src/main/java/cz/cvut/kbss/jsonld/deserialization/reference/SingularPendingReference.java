/**
 * Copyright (C) 2020 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.deserialization.reference;

import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.exception.TargetTypeException;

import java.lang.reflect.Field;
import java.util.Objects;

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
