package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.deserialization.util.DataTypeTransformer;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

class TypesContext<T extends Collection<E>, E> extends InstanceContext<T> {

    private final Set<String> mappedTypes;
    private final Class<E> elementType;

    TypesContext(T instance, Map<String, Object> knownInstances, Class<E> elementType, Class<?> ownerType) {
        super(instance, knownInstances);
        this.elementType = elementType;
        this.mappedTypes = BeanAnnotationProcessor.getOwlClasses(ownerType);
    }

    @Override
    void addItem(Object item) {
        assert item instanceof String;
        if (mappedTypes.contains(item)) {
            return;
        }
        if (!elementType.isAssignableFrom(item.getClass())) {
            instance.add(DataTypeTransformer.transformValue(item, elementType));
        } else {
            instance.add(elementType.cast(item));
        }
    }

    @Override
    Class<?> getItemType() {
        return elementType;
    }
}
