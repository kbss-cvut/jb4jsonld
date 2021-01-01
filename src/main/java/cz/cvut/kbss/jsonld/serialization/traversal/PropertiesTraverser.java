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

import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.exception.JsonLdSerializationException;

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
        for (Map.Entry<?, ?> e : ctx.getValue().entrySet()) {
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
        assert value != null;
        if (isTraversable(value)) {
            try {
                parent.traverseSingular(new SerializationContext<>(property, value));
            } catch (IllegalAccessException e) {
                throw new JsonLdSerializationException("Unable to process property value.", e);
            }
        } else {
            parent.visitAttribute(new SerializationContext<>(property, value));
        }
    }

    private static boolean isTraversable(Object value) {
        final Class<?> cls = value.getClass();
        return (BeanClassProcessor.isIdentifierType(value.getClass()) && !String.class.equals(cls)) ||
                BeanAnnotationProcessor.isOwlClassEntity(value.getClass()) ||
                BeanAnnotationProcessor.hasTypesField(cls);
    }

    private void serializePropertyValues(String property, Collection<?> values) {
        if (values.isEmpty()) {
            return;
        }
        if (values.size() == 1) {
            final Object val = values.iterator().next();
            if (val != null) {
                visitSingleValue(property, val);
            }
        } else {
            final SerializationContext<Collection<?>> colContext = new SerializationContext<>(property, values);
            parent.openCollection(colContext);
            values.stream().filter(Objects::nonNull).forEach(v -> visitSingleValue(null, v));
            parent.closeCollection(colContext);
        }
    }
}
