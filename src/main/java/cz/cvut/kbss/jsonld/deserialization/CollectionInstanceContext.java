package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.deserialization.util.DataTypeTransformer;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;

import java.util.Collection;
import java.util.Map;

class CollectionInstanceContext<T extends Collection> extends InstanceContext<T> {

    private final Class<?> targetType;

    CollectionInstanceContext(T instance, Map<String, Object> knownInstances) {
        super(instance, knownInstances);
        this.targetType = null;
    }

    CollectionInstanceContext(T instance, Class<?> targetType, Map<String, Object> knownInstances) {
        super(instance, knownInstances);
        this.targetType = targetType;
    }

    /**
     * Adds the specified item into this collection.
     *
     * @param item The item to add
     */
    @Override
    void addItem(Object item) {
        if (targetType == null) {
            instance.add(item);
            return;
        }
        Object toAdd = item;
        if (!targetType.isAssignableFrom(item.getClass())) {
            toAdd = null;
            if (knownInstances.containsKey(item.toString())) {
                toAdd = knownInstances.get(item.toString());
                if (!targetType.isAssignableFrom(toAdd.getClass())) {
                    toAdd = null;
                }
            } else {
                toAdd = DataTypeTransformer.transformValue(item, targetType);
            }
        }
        if (toAdd == null) {
            throw typeMismatch(targetType, item.getClass());
        }
        instance.add(toAdd);
    }

    private JsonLdDeserializationException typeMismatch(Class<?> expected, Class<?> actual) {
        return new JsonLdDeserializationException(
                "Type mismatch. Unable to transform instance of type " + actual + " to the expected type " + expected);
    }

    @Override
    Class<?> getItemType() {
        return targetType;
    }
}
