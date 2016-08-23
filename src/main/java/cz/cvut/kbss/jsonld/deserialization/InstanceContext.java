/**
 * Copyright (C) 2016 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.deserialization;

import java.lang.reflect.Field;
import java.util.Map;

abstract class InstanceContext<T> {

    final T instance;

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

    // These methods are intended for overriding, because the behaviour is supported only by some context implementations

    Field getFieldForProperty(String property) {
        throw new UnsupportedOperationException("Not supported by this type of instance context.");
    }

    void setFieldValue(Field field, Object value) {
        throw new UnsupportedOperationException("Not supported by this type of instance context.");
    }

    void addItem(Object item) {
        throw new UnsupportedOperationException("Not supported by this type of instance context.");
    }

    Class<?> getItemType() {
        throw new UnsupportedOperationException("Not supported by this type of instance context.");
    }
}
