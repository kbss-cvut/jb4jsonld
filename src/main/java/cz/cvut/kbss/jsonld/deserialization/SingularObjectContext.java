/**
 * Copyright (C) 2022 Czech Technical University in Prague
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

import cz.cvut.kbss.jopa.datatype.exception.DatatypeMappingException;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

class SingularObjectContext<T> extends InstanceContext<T> {

    private final Map<String, Field> fieldMap;

    SingularObjectContext(T instance, Map<String, Field> fieldMap, Map<String, Object> knownInstances) {
        super(instance, knownInstances);
        this.fieldMap = fieldMap;
    }

    @Override
    Field getFieldForProperty(String property) {
        return fieldMap.get(property);
    }

    @Override
    void setFieldValue(Field field, Object value) {
        assert !(instance instanceof Collection);
        try {
            final Object toSet = resolveAssignableValue(field.getType(), value);
            BeanClassProcessor.setFieldValue(field, instance, toSet);
        } catch (DatatypeMappingException e) {
            throw new JsonLdDeserializationException("Type mismatch when setting value " + value + " on field " + field + ".", e);
        }
    }

    @Override
    boolean isPropertyMapped(String property) {
        return fieldMap.containsKey(property) || hasPropertiesField();
    }

    @Override
    boolean supports(String property) {
        if (!isPropertyMapped(property)) {
            return false;
        }
        return !fieldMap.containsKey(property) || BeanAnnotationProcessor.isWriteable(fieldMap.get(property));
    }
}
