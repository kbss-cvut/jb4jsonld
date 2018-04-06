package cz.cvut.kbss.jsonld.deserialization.expanded;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.deserialization.InstanceBuilder;
import cz.cvut.kbss.jsonld.exception.UnknownPropertyException;

import java.util.List;
import java.util.Map;

class ObjectDeserializer extends Deserializer<Map<?, ?>> {

    private final String property;
    private final Class<?> targetClass;

    ObjectDeserializer(InstanceBuilder instanceBuilder, DeserializerConfig config, String property) {
        super(instanceBuilder, config);
        assert property != null;
        this.property = property;
        this.targetClass = null;
    }

    ObjectDeserializer(InstanceBuilder instanceBuilder, DeserializerConfig config, Class<?> targetClass) {
        super(instanceBuilder, config);
        assert targetClass != null;
        this.targetClass = targetClass;
        this.property = null;
    }

    @Override
    void processValue(Map<?, ?> value) {
        openObject(value);
        for (Map.Entry<?, ?> e : value.entrySet()) {
            final String property = e.getKey().toString();
            final boolean shouldSkip = shouldSkipProperty(property);
            if (shouldSkip) {
                continue;
            }
            if (e.getValue() instanceof List) {
                new CollectionDeserializer(instanceBuilder, config, property).processValue((List<?>) e.getValue());
            } else {
                // Presumably @id
                instanceBuilder.addValue(property, e.getValue());
            }
        }
        instanceBuilder.closeObject();
    }

    private void openObject(Map<?, ?> value) {
        if (property != null) {
            instanceBuilder.openObject(property, getObjectTypes(value));
        } else {
            assert targetClass != null;
            final Class<?> cls = resolveTargetClass(value, targetClass);
            assert targetClass.isAssignableFrom(cls);
            instanceBuilder.openObject(cls);
        }
    }

    private boolean shouldSkipProperty(String property) {
        if (!instanceBuilder.isPropertyMapped(property)) {
            if (configuration().is(ConfigParam.IGNORE_UNKNOWN_PROPERTIES)) {
                return true;
            }
            throw UnknownPropertyException.create(property, instanceBuilder.getCurrentContextType());
        }
        return false;
    }
}
