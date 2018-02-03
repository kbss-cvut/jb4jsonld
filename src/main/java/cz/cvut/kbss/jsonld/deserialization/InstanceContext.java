/**
 * Copyright (C) 2017 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.JsonLd;

import java.lang.reflect.Field;
import java.util.Map;

abstract class InstanceContext<T> {

    T instance;

    final Map<String, Object> knownInstances;

    InstanceContext(T instance, Map<String, Object> knownInstances) {
        this.instance = instance;
        this.knownInstances = knownInstances;
    }

    T getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    Class<T> getInstanceType() {
        return (Class<T>) instance.getClass();
    }

    /**
     * Sets identifier of the instance specified by this context.
     *
     * @param value Identifier value
     */
    void setIdentifierValue(Object value) {
        final Field idField = getFieldForProperty(JsonLd.ID);
        assert idField != null;
        setFieldValue(idField, value);
        knownInstances.put(value.toString(), instance);
    }

    // These methods are intended for overriding, because the behaviour is supported only by some context implementations

    /**
     * Gets a Java field mapped by the specified property.
     * <p>
     * This applies to singular object contexts only.
     *
     * @param property Property IRI
     * @return Field mapped by the specified property. Can be {@code null}
     */
    Field getFieldForProperty(String property) {
        throw new UnsupportedOperationException("Not supported by this type of instance context.");
    }

    /**
     * Sets value of the specified field on the instance represented by this context
     *
     * @param field The field to set
     * @param value The value to set
     */
    void setFieldValue(Field field, Object value) {
        throw new UnsupportedOperationException("Not supported by this type of instance context.");
    }

    /**
     * Adds item to the collection represented by this context.
     *
     * @param item Item to add
     */
    void addItem(Object item) {
        throw new UnsupportedOperationException("Not supported by this type of instance context.");
    }

    /**
     * Gets type of the element type of a collection represented by this context.
     *
     * @return Collection element type
     */
    Class<?> getItemType() {
        throw new UnsupportedOperationException("Not supported by this type of instance context.");
    }

    /**
     * Whether the specified property is mapped by a field in this context.
     * <p>
     * Note that if the context represents an instance with a {@link cz.cvut.kbss.jopa.model.annotations.Properties}
     * field, every property is considered mapped
     *
     * @param property The property to check
     * @return {@code true} if a field mapping the property exists in this context, {@code false} otherwise
     */
    boolean isPropertyMapped(String property) {
        // Default behaviour
        return false;
    }

    /**
     * Called when  this context is being closed
     */
    void close() {
        // Do nothing by default
    }
}
