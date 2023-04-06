/**
 * Copyright (C) 2022 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.serialization.traversal;

import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.common.IdentifierUtil;
import cz.cvut.kbss.jsonld.exception.MissingIdentifierException;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Traverses the provided object graph, visiting each instance and its fields, notifying visitors of these encounters.
 * <p>
 * Each object is visited only once, so circular references are not a problem.
 * <p>
 * The traversal algorithm is depth-first in nature.
 */
public class ObjectGraphTraverser {

    private final SerializationContextFactory serializationContextFactory;

    private InstanceVisitor visitor;

    private final InstanceTypeResolver typeResolver = new InstanceTypeResolver();

    private boolean requireId = false;

    private final Map<Object, String> knownInstances = new IdentityHashMap<>();

    public ObjectGraphTraverser(SerializationContextFactory serializationContextFactory) {
        this.serializationContextFactory = serializationContextFactory;
    }

    public void setVisitor(InstanceVisitor visitor) {
        this.visitor = Objects.requireNonNull(visitor);
    }

    public void removeVisitor() {
        this.visitor = null;
    }

    public void traverse(Object instance) {
        Objects.requireNonNull(instance);
        traverse(serializationContextFactory.create(instance));
    }

    public void traverse(SerializationContext<?> ctx) {
        Objects.requireNonNull(ctx);
        assert visitor != null;
        if (ctx.getValue() instanceof Collection) {
            traverseCollection((SerializationContext<? extends Collection<?>>) ctx);
        } else {
            traverseSingular(ctx);
        }
    }

    private void traverseCollection(SerializationContext<? extends Collection<?>> ctx) {
        openCollection(ctx);
        for (Object item : ctx.getValue()) {
            if (item == null) {
                continue;
            }
            traverseSingular(serializationContextFactory.create(item, ctx));
        }
        closeCollection(ctx);
    }

    void traverseSingular(SerializationContext<?> ctx) {
        if (ctx.getValue() == null) {
            return;
        }
        final boolean shouldTraverse = visitInstance(ctx);
        if (!shouldTraverse) {
            return;
        }
        if (BeanClassProcessor.isIndividualType(ctx.getValue().getClass())) {
            visitIndividual(ctx);
            return;
        }
        final boolean firstEncounter = !knownInstances.containsKey(ctx.getValue());
        openInstance(ctx);
        visitIdentifier(ctx);
        if (firstEncounter) {
            visitTypes(ctx);
            serializeFields(ctx);
            serializePropertiesField(ctx);
        }
        closeInstance(ctx);
    }

    private void serializeFields(SerializationContext<?> ctx) {
        final Object instance = ctx.getValue();
        final List<Field> fieldsToSerialize =
                orderAttributesForSerialization(BeanAnnotationProcessor.getSerializableFields(instance),
                                                BeanAnnotationProcessor.getAttributeOrder(instance.getClass()));
        for (Field f : fieldsToSerialize) {
            if (shouldSkipFieldSerialization(f)) {
                continue;
            }
            Object value = BeanClassProcessor.getFieldValue(f, instance);
            final SerializationContext<?> fieldCtx = serializationContextFactory.createForAttribute(f, value, ctx);
            visitAttribute(fieldCtx);
        }
    }

    private boolean shouldSkipFieldSerialization(Field f) {
        return BeanAnnotationProcessor.isInstanceIdentifier(f) || BeanAnnotationProcessor.isPropertiesField(
                f) || BeanAnnotationProcessor.isTypesField(f);
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

    private void serializePropertiesField(SerializationContext<?> ctx) {
        final Object instance = ctx.getValue();
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
                .traverseProperties(
                        serializationContextFactory.createForProperties(propertiesField, (Map<?, ?>) value, ctx));
    }

    public boolean visitInstance(SerializationContext<?> ctx) {
        return visitor.visitObject(ctx);
    }

    public void visitIndividual(SerializationContext<?> ctx) {
        visitor.visitIndividual(ctx);
    }

    public void openInstance(SerializationContext<?> ctx) {
        if (!BeanClassProcessor.isIdentifierType(ctx.getValue().getClass())) {
            final String identifier = resolveIdentifier(ctx.getValue());
            knownInstances.put(ctx.getValue(), identifier);
        }
        visitor.openObject(ctx);
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
        visitor.closeObject(ctx);
    }

    public void visitIdentifier(SerializationContext<?> ctx) {
        final Object identifier = ctx.getValue();
        final Class<?> idCls = identifier.getClass();
        final String id;
        final SerializationContext<String> idContext;
        id = resolveIdentifier(identifier);
        idContext = serializationContextFactory.createForIdentifier(
                BeanAnnotationProcessor.getIdentifierField(idCls).orElse(null), id, ctx);
        knownInstances.put(identifier, id);
        visitor.visitIdentifier(idContext);
    }

    public void visitTypes(SerializationContext<?> ctx) {
        final Object instance = ctx.getValue();
        final Set<String> resolvedTypes = typeResolver.resolveTypes(instance);
        assert !resolvedTypes.isEmpty();
        final SerializationContext<Set<String>> typesContext = serializationContextFactory.createForTypes(
                BeanAnnotationProcessor.getTypesField(instance.getClass()).orElse(null), resolvedTypes, ctx);
        visitor.visitTypes(typesContext);
    }

    public void visitAttribute(SerializationContext<?> ctx) {
        visitor.visitAttribute(ctx);
    }

    public void openCollection(SerializationContext<? extends Collection<?>> ctx) {
        visitor.openCollection(ctx);
    }

    public void closeCollection(SerializationContext<?> ctx) {
        visitor.closeCollection(ctx);
    }

    public void setRequireId(boolean requireId) {
        this.requireId = requireId;
    }
}
