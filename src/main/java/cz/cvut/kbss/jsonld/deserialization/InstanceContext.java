package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.deserialization.util.DataTypeTransformer;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

class InstanceContext<T> {

    private final T instance;

    private final Map<String, Field> fieldMap;

    private final Map<String, Object> knownInstances;

    InstanceContext(T instance, Map<String, Object> knownInstances) {
        this.instance = instance;
        this.knownInstances = knownInstances;
        this.fieldMap = Collections.emptyMap();
    }

    InstanceContext(T instance, Map<String, Field> fieldMap, Map<String, Object> knownInstances) {
        this.instance = instance;
        this.fieldMap = fieldMap;
        this.knownInstances = knownInstances;
    }

    T getInstance() {
        return instance;
    }

    Field getFieldForProperty(String property) {
        // TODO Add handling of @Properties
        return fieldMap.get(property);
    }

    void setFieldValue(Field field, Object value) {
        assert !(instance instanceof Collection);
        if (!field.getType().isAssignableFrom(value.getClass())) {
            boolean success = trySettingReferenceToKnownInstance(field, value);
            if (success) {
                return;
            }
            success = tryTypeTransformation(field, value);
            if (success) {
                return;
            }
            throw valueTypeMismatch(value, field);
        }
        BeanClassProcessor.setFieldValue(field, instance, value);
    }

    private JsonLdDeserializationException valueTypeMismatch(Object value, Field field) {
        return new JsonLdDeserializationException(
                "Type mismatch. Cannot set value " + value + " of type " + value.getClass() + " on field " + field);
    }

    private boolean trySettingReferenceToKnownInstance(Field field, Object value) {
        if (!BeanAnnotationProcessor.isObjectProperty(field) || !knownInstances.containsKey(value.toString())) {
            return false;
        }
        final Object knownInstance = knownInstances.get(value.toString());
        if (!field.getType().isAssignableFrom(knownInstance.getClass())) {
            // Throw the exception right here so that it contains info about the known instance's type
            throw valueTypeMismatch(knownInstance, field);
        }
        BeanClassProcessor.setFieldValue(field, instance, knownInstance);
        return true;
    }

    private boolean tryTypeTransformation(Field field, Object value) {
        final Class<?> targetType = field.getType();
        final Object transformedValue = DataTypeTransformer.transformValue(value, targetType);
        if (transformedValue != null) {
            BeanClassProcessor.setFieldValue(field, instance, transformedValue);
            return true;
        }
        return false;
    }

    /**
     * Adds the specified item to this instance, if it is a collection.
     *
     * @param item The item to add
     * @throws JsonLdDeserializationException When this instance is not a collection
     */
    void addItem(Object item) {
        if (!(instance instanceof Collection)) {
            throw new JsonLdDeserializationException("The current instance " + instance + " is not a Collection.");
        }
        ((Collection) instance).add(item);
    }
}
