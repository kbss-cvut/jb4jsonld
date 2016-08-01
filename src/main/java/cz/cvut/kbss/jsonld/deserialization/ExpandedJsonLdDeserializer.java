package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.Constants;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;

import java.util.List;
import java.util.Map;

public class ExpandedJsonLdDeserializer extends JsonLdDeserializer {

    private InstanceBuilder instanceBuilder;

    @Override
    public <T> T deserialize(Object jsonLd, Class<T> resultClass) {
        if (!(jsonLd instanceof List)) {
            throw new JsonLdDeserializationException(
                    "Expanded JSON-LD deserializer requires a JSON-LD array as input.");
        }
        final List<?> input = (List<?>) jsonLd;
        assert input.size() == 1;
        final Map<?, ?> root = (Map<?, ?>) input.get(0);
        this.instanceBuilder = new DefaultInstanceBuilder();
        instanceBuilder.openObject(resultClass);
        processObject(root);
        instanceBuilder.closeObject();
        assert resultClass.isAssignableFrom(instanceBuilder.getCurrentRoot().getClass());
        return resultClass.cast(instanceBuilder.getCurrentRoot());
    }

    private void processObject(Map<?, ?> root) {
        for (Map.Entry<?, ?> e : root.entrySet()) {
            final String property = e.getKey().toString();
            if (e.getValue() instanceof List) {
                resolveCollectionValue(property, (List<?>) e.getValue());
            } else {
                // Presumably @id
                instanceBuilder.addValue(property, e.getValue());
            }
        }
    }

    private void resolveCollectionValue(String property, List<?> value) {
        if (property.equals(Constants.JSON_LD_TYPE)) {
            // TODO This is not entirely correct, if the target type has a @Types field, types should be added to it
            // But only those which are not in the @OWLClass annotation on the target type or its ancestors
            return;
        }
        if (value.size() == 1 && value.get(0) instanceof Map) {
            resolvePropertyValue(property, (Map<?, ?>) value.get(0));
        } else {
            instanceBuilder.openCollection(property);
            for (Object item : value) {
                assert item instanceof Map;
                resolveValue((Map<?, ?>) item);
            }
            instanceBuilder.closeCollection();
        }
    }

    private void resolveValue(Map<?, ?> value) {
        if (value.size() == 1 && value.containsKey(Constants.JSON_LD_VALUE)) {
            instanceBuilder.addValue(value.get(Constants.JSON_LD_VALUE));
        } else {
            final Class<?> elementType = instanceBuilder.getCurrentCollectionElementType();
            instanceBuilder.openObject(elementType);
            processObject(value);
            instanceBuilder.closeObject();
        }
    }

    private void resolvePropertyValue(String property, Map<?, ?> value) {
        if (value.size() == 1 && value.containsKey(Constants.JSON_LD_VALUE)) {
            instanceBuilder.addValue(property, value.get(Constants.JSON_LD_VALUE));
        } else {
            instanceBuilder.openObject(property);
            processObject(value);
            instanceBuilder.closeObject();
        }
    }
}
