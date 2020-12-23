package cz.cvut.kbss.jsonld.serialization.traversal;

import java.lang.reflect.Field;
import java.util.Objects;

public class SerializationContext<T> {

    public final String attributeId;

    public final Field field;

    public final T value;

    public SerializationContext(String attributeId, Field field, T value) {
        this.attributeId = attributeId;
        this.field = field;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SerializationContext<?> that = (SerializationContext<?>) o;
        return Objects.equals(attributeId, that.attributeId) && Objects
                .equals(field, that.field) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributeId, field, value);
    }
}
