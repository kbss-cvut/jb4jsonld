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
        throw new UnsupportedOperationException("Not supported by this type of instance context");
    }

    void setFieldValue(Field field, Object value) {
        throw new UnsupportedOperationException("Not supported by this type of instance context");
    }

    void addItem(Object item) {
        throw new UnsupportedOperationException("Not supported by this type of instance context");
    }

    Class<?> getItemType() {
        throw new UnsupportedOperationException("Not supported by this type of instance context");
    }
}
