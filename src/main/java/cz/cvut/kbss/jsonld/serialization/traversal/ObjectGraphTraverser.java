/**
 * Copyright (C) 2017 Czech Technical University in Prague
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
import cz.cvut.kbss.jsonld.common.IdentifierUtil;
import cz.cvut.kbss.jsonld.exception.JsonLdSerializationException;

import java.lang.reflect.Field;
import java.util.*;

public class ObjectGraphTraverser implements InstanceVisitor {

    private final Set<InstanceVisitor> visitors = new HashSet<>(4);

    private Map<Object, String> knownInstances;

    public void addVisitor(InstanceVisitor visitor) {
        Objects.requireNonNull(visitor);
        visitors.add(visitor);
    }

    public void removeVisitor(InstanceVisitor visitor) {
        visitors.remove(visitor);
    }

    private void resetKnownInstances() {
        this.knownInstances = new IdentityHashMap<>();
    }

    public void traverse(Object instance) {
        Objects.requireNonNull(instance);
        resetKnownInstances();
        try {
            if (instance instanceof Collection) {
                traverseCollection((Collection<?>) instance);
            } else {
                traverseImpl(instance);
            }
        } catch (IllegalAccessException e) {
            throw new JsonLdSerializationException("Unable to extract field value.", e);
        }
    }

    private void traverseCollection(Collection<?> col) throws IllegalAccessException {
        openCollection(col);
        for (Object item : col) {
            traverseImpl(item);
        }
        closeCollection(col);
    }

    private void traverseImpl(Object instance) throws IllegalAccessException {
        if (knownInstances.containsKey(instance)) {
            visitKnownInstance(knownInstances.get(instance), instance);
            return;
        }
        openInstance(instance);
        final List<Field> fieldsToSerialize =
                orderAttributesForSerialization(BeanAnnotationProcessor.getSerializableFields(instance),
                        BeanAnnotationProcessor.getAttributeOrder(instance.getClass()));
        for (Field f : fieldsToSerialize) {
            Object value = BeanClassProcessor.getFieldValue(f, instance);
            if (value == null && BeanAnnotationProcessor.isInstanceIdentifier(f)) {
                // If the value is null for an identifier field, we have already generated one when opening the object
                value = knownInstances.get(instance);
            }
            visitField(f, value);
            if (value != null && BeanAnnotationProcessor.isObjectProperty(f)) {
                traverseObjectPropertyValue(value);
            }
        }
        closeInstance(instance);
    }

    private List<Field> orderAttributesForSerialization(List<Field> fields, String[] ordering) {
        final List<Field> result = new ArrayList<>(fields.size());
        for (String item : ordering) {
            final Iterator<Field> it = fields.iterator();
            while (it.hasNext()) {
                final Field f = it.next();
                if (f.getName().equals(item)) {
                    it.remove();
                    result.add(f);
                    break;
                }
            }
        }
        result.addAll(fields);
        return result;
    }

    private void traverseObjectPropertyValue(Object value) throws IllegalAccessException {
        if (value instanceof Collection) {
            final Collection<?> col = (Collection<?>) value;
            openCollection(col);
            for (Object elem : col) {
                traverseImpl(elem);
            }
            closeCollection(col);
        } else if (value.getClass().isArray()) {
            throw new JsonLdSerializationException("Arrays are not supported, yet.");
        } else {
            traverseImpl(value);
        }
    }

    @Override
    public void openInstance(Object instance) {
        if (!BeanClassProcessor.isIdentifierType(instance.getClass())) {
            final Optional<Object> identifier = BeanAnnotationProcessor.getInstanceIdentifier(instance);
            knownInstances.put(instance, identifier.orElse(IdentifierUtil.generateBlankNodeId()).toString());
        }
        visitors.forEach(v -> v.openInstance(instance));
    }

    @Override
    public void closeInstance(Object instance) {
        visitors.forEach(v -> v.closeInstance(instance));
    }

    @Override
    public void visitKnownInstance(String id, Object instance) {
        visitors.forEach(v -> v.visitKnownInstance(id, instance));
    }

    @Override
    public void visitField(Field field, Object value) {
        visitors.forEach(v -> v.visitField(field, value));
    }

    @Override
    public void openCollection(Collection<?> collection) {
        visitors.forEach(v -> v.openCollection(collection));
    }

    @Override
    public void closeCollection(Collection<?> collection) {
        visitors.forEach(v -> v.closeCollection(collection));
    }
}
