/**
 * Copyright (C) 2020 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.serialization.traversal;

import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.common.IdentifierUtil;
import cz.cvut.kbss.jsonld.exception.JsonLdSerializationException;
import cz.cvut.kbss.jsonld.exception.MissingIdentifierException;

import java.lang.reflect.Field;
import java.util.*;

public class ObjectGraphTraverser {

    private final Set<InstanceVisitor> visitors = new HashSet<>(4);

    private final InstanceTypeResolver typeResolver = new InstanceTypeResolver();

    private boolean requireId = false;

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
                traverseCollection(new SerializationContext<>((Collection<?>) instance));
            } else {
                traverseSingular(new SerializationContext<>(instance));
            }
        } catch (IllegalAccessException e) {
            throw new JsonLdSerializationException("Unable to extract field value.", e);
        }
    }

    private void traverseCollection(SerializationContext<? extends Collection<?>> ctx) throws IllegalAccessException {
        openCollection(ctx);
        for (Object item : ctx.getValue()) {
            traverseSingular(new SerializationContext<>(item));
        }
        closeCollection(ctx);
    }

    void traverseSingular(SerializationContext<?> ctx) throws IllegalAccessException {
        if (ctx.getValue() == null) {
            return;
        }
        final boolean firstEncounter = !knownInstances.containsKey(ctx.getValue());
        openInstance(ctx);
        visitIdentifier(ctx.getValue());
        if (!BeanClassProcessor.isIdentifierType(ctx.getValue().getClass()) && firstEncounter) {
            visitTypes(ctx.getValue());
            serializeFields(ctx.getValue());
            serializePropertiesField(ctx.getValue());
        }
        closeInstance(ctx);
    }

    private void serializeFields(Object instance) throws IllegalAccessException {
        final List<Field> fieldsToSerialize =
                orderAttributesForSerialization(BeanAnnotationProcessor.getSerializableFields(instance),
                        BeanAnnotationProcessor.getAttributeOrder(instance.getClass()));
        for (Field f : fieldsToSerialize) {
            if (BeanAnnotationProcessor.isInstanceIdentifier(f) || BeanAnnotationProcessor.isPropertiesField(f) || BeanAnnotationProcessor.isTypesField(f)) {
                continue;
            }
            Object value = BeanClassProcessor.getFieldValue(f, instance);
            final SerializationContext<?> ctx =
                    new SerializationContext<>(BeanAnnotationProcessor.getAttributeIdentifier(f), f, value);
            visitAttribute(ctx);
            if (value != null && BeanAnnotationProcessor.isObjectProperty(f)) {
                traverseObjectPropertyValue(ctx);
            }
        }
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

    private void traverseObjectPropertyValue(SerializationContext<?> ctx) throws IllegalAccessException {
        if (ctx.getValue() instanceof Collection) {
            final SerializationContext<Collection<?>> colContext = (SerializationContext<Collection<?>>) ctx;
            openCollection(colContext);
            for (Object elem : colContext.getValue()) {
                traverseSingular(new SerializationContext<>(elem));
            }
            closeCollection(colContext);
        } else if (ctx.getValue().getClass().isArray()) {
            throw new JsonLdSerializationException("Arrays are not supported, yet.");
        } else {
            traverseSingular(ctx);
        }
    }

    private void serializePropertiesField(Object instance) {
        if (!BeanAnnotationProcessor.hasPropertiesField(instance.getClass())) {
            return;
        }
        final Field propertiesField = BeanAnnotationProcessor.getPropertiesField(instance.getClass());
        final Object value = BeanClassProcessor.getFieldValue(propertiesField, instance);
        if (value == null) {
            return;
        }
        assert value instanceof Map;
        new PropertiesTraverser(this)
                .traverseProperties(new SerializationContext<>(propertiesField, (Map<?, ?>) value));
    }

    public void openInstance(SerializationContext<?> ctx) {
        if (!BeanClassProcessor.isIdentifierType(ctx.getValue().getClass())) {
            final String identifier = resolveIdentifier(ctx.getValue());
            knownInstances.put(ctx.getValue(), identifier);
        }
        visitors.forEach(v -> v.openObject(ctx));
    }

    private String resolveIdentifier(Object instance) {
        final Optional<Object> extractedId = BeanAnnotationProcessor.getInstanceIdentifier(instance);
        if (!extractedId.isPresent() && requireId) {
            throw MissingIdentifierException.create(instance);
        }
        return extractedId.orElseGet(() -> knownInstances.containsKey(instance) ? knownInstances.get(instance) :
                IdentifierUtil.generateBlankNodeId()).toString();
    }

    public void closeInstance(SerializationContext<?> ctx) {
        visitors.forEach(v -> v.closeObject(ctx));
    }

    public void visitIdentifier(Object instance) {
        final String id;
        if (BeanClassProcessor.isIdentifierType(instance.getClass())) {
            id = instance.toString();
        } else {
            id = resolveIdentifier(instance);
            knownInstances.put(instance, id);
        }
        final SerializationContext<String> idContext = new SerializationContext<>(id);
        visitors.forEach(v -> v.visitIdentifier(idContext));
    }

    public void visitTypes(Object instance) {
        final Set<String> resolvedTypes = typeResolver.resolveTypes(instance);
        assert !resolvedTypes.isEmpty();
        final SerializationContext<Collection<String>> typesContext = new SerializationContext<>(resolvedTypes);
        visitors.forEach(v -> v.visitTypes(typesContext));
    }

    public void visitAttribute(SerializationContext<?> ctx) {
        visitors.forEach(v -> v.visitAttribute(ctx));
    }

    public void openCollection(SerializationContext<? extends Collection<?>> ctx) {
        visitors.forEach(v -> v.openCollection(ctx));
    }

    public void closeCollection(SerializationContext<?> ctx) {
        visitors.forEach(v -> v.closeCollection(ctx));
    }

    public void setRequireId(boolean requireId) {
        this.requireId = requireId;
    }
}
