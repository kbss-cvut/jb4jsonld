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
package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.deserialization.util.DataTypeTransformer;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Represents deserialization context of a plain identifier object property.
 */
class NodeReferenceContext<T> extends InstanceContext<T> {

    private final InstanceContext<?> owner;
    private final Field targetField;

    NodeReferenceContext(InstanceContext<?> owner, Field targetField, Map<String, Object> knownInstances) {
        super(null, knownInstances);
        this.owner = owner;
        this.targetField = targetField;
    }

    NodeReferenceContext(InstanceContext<?> owner, Map<String, Object> knownInstances) {
        super(null, knownInstances);
        this.owner = owner;
        this.targetField = null;
    }

    @Override
    void setIdentifierValue(String value) {
        final Class<?> targetType = targetField != null ? targetField.getType() : owner.getItemType();
        this.instance = (T) transformToTargetType(value, targetType);
    }

    private Object transformToTargetType(String id, Class<?> targetType) {
        return DataTypeTransformer.transformValue(id, targetType);
    }

    @Override
    void close() {
        assert instance != null;
        if (targetField != null) {
            owner.setFieldValue(targetField, instance);
        } else {
            owner.addItem(instance);
        }
    }

    @Override
    boolean isPropertyMapped(String property) {
        return property.equals(JsonLd.ID);
    }

    @Override
    boolean supports(String property) {
        return isPropertyMapped(property);
    }

    @Override
    Class<T> getInstanceType() {
        return null;
    }

    @Override
    Field getFieldForProperty(String property) {
        return null;
    }

    @Override
    boolean hasPropertiesField() {
        return false;
    }
}
