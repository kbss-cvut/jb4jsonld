/**
 * Copyright (C) 2020 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.serialization.traversal;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Serializes a {@link cz.cvut.kbss.jopa.model.annotations.Properties} field.
 * <p>
 * Note that at the moment, when the map also contains a property which is already mapped by another field, a conflict in
 * the resulting JSON-LD will arise.
 */
class PropertiesTraverser {

    private final ObjectGraphTraverser parent;

    PropertiesTraverser(ObjectGraphTraverser parent) {
        this.parent = parent;
    }

    public void traverseProperties(SerializationContext<Map<?, ?>> ctx) {
        for (Map.Entry<?, ?> e : ctx.value.entrySet()) {
            final String property = e.getKey().toString();
            if (e.getValue() == null) {
                continue;
            }
            if (e.getValue() instanceof Collection) {
                final Collection<?> propertyValues = (Collection<?>) e.getValue();
                serializePropertyValues(property, propertyValues);
            } else {
                visitSingleValue(property, e.getValue());
            }
        }
    }

    private void visitSingleValue(String property, Object value) {
        parent.visitAttribute(new SerializationContext<>(property, null, value));
    }

    private void serializePropertyValues(String property, Collection<?> values) {
        if (values.isEmpty()) {
            return;
        }
        if (values.size() == 1) {
            final Object val = values.iterator().next();
            visitSingleValue(property, val);
        } else {
            final SerializationContext<Collection<?>> colContext = new SerializationContext<>(property, null, values);
            parent.openCollection(colContext);
            values.stream().filter(Objects::nonNull).forEach(v -> visitSingleValue(property, v));
            parent.closeCollection(colContext);
        }
    }
}
