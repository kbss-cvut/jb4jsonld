package cz.cvut.kbss.jsonld.deserialization;

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
        instance.add(item);
    }

    @Override
    Class<?> getItemType() {
        return targetType;
    }
}
