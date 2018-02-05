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
    void setIdentifierValue(Object value) {
        final String id = value.toString();
        final Class<?> targetType = targetField != null ? targetField.getType() : owner.getItemType();
        this.instance = (T) transformToTargetType(id, targetType);
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
    Class<T> getInstanceType() {
        return null;
    }
}
