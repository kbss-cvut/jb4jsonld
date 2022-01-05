/**
 * Copyright (C) 2022 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.serialization.traversal;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Represents the current serialization context.
 * <p>
 * This means the value being serialized, and optionally the attribute identifier and field.
 * <p>
 * Note that attribute and field may not always be available, e.g., when a collection is being serialized, neither is set. This
 * keeps the visitors simple.
 *
 * @param <T> Type of the value
 */
public class SerializationContext<T> {

    protected final String attributeId;

    protected final Field field;

    protected final T value;

    public SerializationContext(String attributeId, T value) {
        this(attributeId, null, value);
    }

    public SerializationContext(Field field, T value) {
        this(null, field, value);
    }

    public SerializationContext(T value) {
        this(null, null, value);
    }

    public SerializationContext(String attributeId, Field field, T value) {
        this.attributeId = attributeId;
        this.field = field;
        this.value = value;
    }

    public String getAttributeId() {
        return attributeId;
    }

    public Field getField() {
        return field;
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SerializationContext<?> that = (SerializationContext<?>) o;
        return Objects.equals(getAttributeId(), that.getAttributeId()) && Objects
                .equals(getField(), that.getField()) && Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAttributeId(), getField(), getValue());
    }
}
