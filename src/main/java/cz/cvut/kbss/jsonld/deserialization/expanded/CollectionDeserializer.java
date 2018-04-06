package cz.cvut.kbss.jsonld.deserialization.expanded;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.deserialization.InstanceBuilder;

import java.util.List;
import java.util.Map;

class CollectionDeserializer extends Deserializer<List<?>> {

    private final String property;

    CollectionDeserializer(InstanceBuilder instanceBuilder, DeserializerConfig config, String property) {
        super(instanceBuilder, config);
        this.property = property;
    }

    @Override
    void processValue(List<?> value) {
        if (value.size() == 1 && value.get(0) instanceof Map) {
            final Map<?, ?> map = (Map<?, ?>) value.get(0);
            if (!instanceBuilder.isPlural(property)) {
                resolvePropertyValue(property, map);
                return;
            }
            if (map.size() == 1 && map.containsKey(JsonLd.LIST)) {
                assert map.get(JsonLd.LIST) instanceof List;
                processValue((List<?>) map.get(JsonLd.LIST));
                return;
            }
        }
        instanceBuilder.openCollection(property);
        for (Object item : value) {
            if (item instanceof Map) {
                resolveValue((Map<?, ?>) item);
            } else {
                instanceBuilder.addValue(item);
            }
        }
        instanceBuilder.closeCollection();
    }

    private void resolveValue(Map<?, ?> value) {
        if (value.size() == 1 && value.containsKey(JsonLd.VALUE)) {
            instanceBuilder.addValue(value.get(JsonLd.VALUE));
        } else if (value.size() == 1 && value.containsKey(JsonLd.ID)) {
            instanceBuilder.addNodeReference(value.get(JsonLd.ID).toString());
        } else {
            final Class<?> elementType = instanceBuilder.getCurrentCollectionElementType();
            new ObjectDeserializer(instanceBuilder, config, elementType).processValue(value);
        }
    }

    private void resolvePropertyValue(String property, Map<?, ?> value) {
        if (value.size() == 1 && value.containsKey(JsonLd.VALUE)) {
            instanceBuilder.addValue(property, value.get(JsonLd.VALUE));
        } else if (value.size() == 1 && value.containsKey(JsonLd.ID)) {
            instanceBuilder.addNodeReference(property, value.get(JsonLd.ID).toString());
        } else {
            new ObjectDeserializer(instanceBuilder, config, property).processValue(value);
        }
    }
}
