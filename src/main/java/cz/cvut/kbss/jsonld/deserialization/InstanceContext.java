package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

class InstanceContext {

    private final Object instance;

    private final Map<String, Field> fieldMap;

    InstanceContext(Object instance) {
        this.instance = instance;
        this.fieldMap = Collections.emptyMap();
    }

    InstanceContext(Object instance, Map<String, Field> fieldMap) {
        this.instance = instance;
        this.fieldMap = fieldMap;
    }

    Object getInstance() {
        return instance;
    }

    Map<String, Field> getFieldMap() {
        return fieldMap;
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
