package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.exception.TargetTypeException;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
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
        // TODO This should also be reused from JOPA API
        if (URI.class.equals(targetType)) {
            return URI.create(id);
        } else if (URL.class.equals(targetType)) {
            try {
                return new URL(id);
            } catch (MalformedURLException e) {
                throw new TargetTypeException(id + " is not a valid URL.");
            }
        } else {
            return id;
        }
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
