package cz.cvut.kbss.jsonld.deserialization.reference;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Represents a pending reference.
 * <p>
 * A pending reference represents a situation when the JSON-LD contains just an object with and {@code @id}, while the mapped
 * attribute expects a full-blown object. In this case, it is expected that somewhere in the JSON-LD, there is the object's
 * full serialization and this is only a reference to it.
 */
public final class PendingReference {

    private final Object targetObject;

    private final Field targetField;

    public PendingReference(Object targetObject, Field targetField) {
        this.targetObject = Objects.requireNonNull(targetObject);
        this.targetField = Objects.requireNonNull(targetField);
    }

    public Object getTargetObject() {
        return targetObject;
    }

    public Field getTargetField() {
        return targetField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PendingReference that = (PendingReference) o;
        return targetObject.equals(that.targetObject) && targetField.equals(that.targetField);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetObject, targetField);
    }
}
