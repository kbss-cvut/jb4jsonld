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
