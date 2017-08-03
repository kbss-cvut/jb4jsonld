package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;

import java.lang.reflect.Field;
import java.util.*;

class PropertiesFieldSerializer implements FieldSerializer {

    @Override
    public List<JsonNode> serializeField(Field field, Object value) {
        assert value instanceof Map;
        final Map<?, ?> map = (Map<?, ?>) value;
        final List<JsonNode> result = new ArrayList<>(map.size());
        for (Map.Entry<?, ?> e : map.entrySet()) {
            final String property = e.getKey().toString();
            if (e.getValue() == null) {
                continue;
            }
            if (e.getValue() instanceof Collection) {
                final Collection<?> propertyValues = (Collection<?>) e.getValue();
                serializePropertyValues(property, propertyValues).ifPresent(result::add);
            } else {
                result.add(JsonNodeFactory.createLiteralNode(property, e.getValue()));
            }
        }
        return result;
    }

    private Optional<JsonNode> serializePropertyValues(String property, Collection<?> values) {
        if (values.isEmpty()) {
            return Optional.empty();
        }
        if (values.size() == 1) {
            final Object val = values.iterator().next();
            return Optional.of(JsonNodeFactory.createLiteralNode(property, val));
        } else {
            final CollectionNode propertyNode = JsonNodeFactory.createCollectionNode(property, values);
            values.stream().filter(Objects::nonNull)
                  .forEach(v -> propertyNode.addItem(JsonNodeFactory.createLiteralNode(v)));
            return Optional.of(propertyNode);
        }
    }
}
