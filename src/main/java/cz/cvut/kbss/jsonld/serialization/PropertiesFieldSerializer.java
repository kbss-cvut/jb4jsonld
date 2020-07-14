/**
 * Copyright (C) 2020 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Serializes a {@link cz.cvut.kbss.jopa.model.annotations.Properties} field.
 * <p>
 * Note that at the moment, when the map also contains a property which is already map by another field, a conflict in
 * the resulting JSON-LD will arise.
 */
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
