/**
 * Copyright (C) 2022 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.serialization.traversal;

import cz.cvut.kbss.jsonld.serialization.context.JsonLdContext;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Represents the current serialization context.
 * <p>
 * This means the value being serialized, and optionally the target JSON-LD term identifier and field.
 * <p>
 * Note that term and field may not always be available, e.g., when a collection is being serialized, neither is set.
 * This keeps the visitors simple.
 * <p>
 * This class also provides access to the JSON-LD context (if it is used). However, note that for selected serialization
 * outputs the context may be just a stub with no bearing on the serialization output.
 *
 * @param <T> Type of the value
 */
public class SerializationContext<T> {

    protected final String term;

    protected final Field field;

    protected final T value;

    protected final JsonLdContext jsonLdContext;

    public SerializationContext(String term, T value, JsonLdContext jsonLdContext) {
        this(term, null, value, jsonLdContext);
    }

    public SerializationContext(Field field, T value, JsonLdContext jsonLdContext) {
        this(null, field, value, jsonLdContext);
    }

    public SerializationContext(T value, JsonLdContext jsonLdContext) {
        this(null, null, value, jsonLdContext);
    }

    public SerializationContext(String term, Field field, T value, JsonLdContext jsonLdContext) {
        this.term = term;
        this.field = field;
        this.value = value;
        this.jsonLdContext = jsonLdContext;
    }

    public String getTerm() {
        return term;
    }

    public Field getField() {
        return field;
    }

    public String getFieldName() {
        return field != null ? field.getName() : null;
    }

    public T getValue() {
        return value;
    }

    public JsonLdContext getJsonLdContext() {
        return jsonLdContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SerializationContext<?> that = (SerializationContext<?>) o;
        return Objects.equals(getTerm(), that.getTerm()) && Objects
                .equals(getField(), that.getField()) && Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTerm(), getField(), getValue());
    }
}
