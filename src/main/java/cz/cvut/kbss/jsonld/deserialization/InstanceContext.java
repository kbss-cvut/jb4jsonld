package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

class InstanceContext<T> {

    private final T instance;

    private final Map<String, Field> fieldMap;

    InstanceContext(T instance) {
        this.instance = instance;
        this.fieldMap = Collections.emptyMap();
    }

    InstanceContext(T instance, Map<String, Field> fieldMap) {
        this.instance = instance;
        this.fieldMap = fieldMap;
    }

    T getInstance() {
        return instance;
    }

    Map<String, Field> getFieldMap() {
        return fieldMap;
    }

    Field getFieldForProperty(String property) {
        // TODO Add handling of @Properties
        return fieldMap.get(property);
    }

    void setFieldValue(Field field, Object value) {
        assert !(instance instanceof Collection);
        // TODO Handle object references
        if (!field.getType().isAssignableFrom(value.getClass())) {
            throw new JsonLdDeserializationException(
                    "Type mismatch. Cannot set value " + value + " of type " + value.getClass() + " on field " + field);
        }
        BeanClassProcessor.setFieldValue(field, instance, value);
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
