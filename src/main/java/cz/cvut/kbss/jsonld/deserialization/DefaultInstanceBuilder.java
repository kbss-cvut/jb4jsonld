/**
 * Copyright (C) 2022 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.common.CollectionType;
import cz.cvut.kbss.jsonld.deserialization.reference.PendingReferenceRegistry;
import cz.cvut.kbss.jsonld.deserialization.util.DataTypeTransformer;
import cz.cvut.kbss.jsonld.deserialization.util.TargetClassResolver;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import cz.cvut.kbss.jsonld.exception.TargetTypeException;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.*;

/**
 * Default implementation of the JSON-LD deserializer, which takes values parsed from a JSON-LD document and builds Java
 * instances from them.
 */
public class DefaultInstanceBuilder implements InstanceBuilder {

    // Identifiers to instances
    private final Map<String, Object> knownInstances = new HashMap<>();
    private final Stack<InstanceContext> openInstances = new Stack<>();

    private final TargetClassResolver classResolver;

    private final PendingReferenceRegistry pendingReferenceRegistry;

    private InstanceContext currentInstance;

    public DefaultInstanceBuilder(TargetClassResolver classResolver,
                                  PendingReferenceRegistry pendingReferenceRegistry) {
        this.classResolver = classResolver;
        this.pendingReferenceRegistry = pendingReferenceRegistry;
    }

    @Override
    public void openObject(String id, String property, List<String> types) {
        Objects.requireNonNull(property);
        final Field targetField = currentInstance.getFieldForProperty(property);
        assert targetField != null;
        final Class<?> type = targetField.getType();
        final InstanceContext<?> ctx;
        if (BeanClassProcessor.isIdentifierType(type)) {
            ctx = new NodeReferenceContext<>(currentInstance, targetField, knownInstances);
            ctx.setIdentifierValue(id);
        } else {
            ctx = openObjectForProperty(id, types, targetField);
            final Object newPropertyObject = ctx.getInstance();
            if (!isPlural(property)) {
                final Object oldPropertyObject = BeanClassProcessor
                        .getFieldValue(targetField, currentInstance.getInstance());
                if (oldPropertyObject != null && !oldPropertyObject.equals(newPropertyObject))
                    // Value already set on singular attribute of a reopened instance
                    throw JsonLdDeserializationException.singularAttributeCardinalityViolated(property, targetField);
            }
            currentInstance.setFieldValue(targetField, newPropertyObject);
        }
        openInstances.push(currentInstance);
        this.currentInstance = ctx;
    }

    private InstanceContext<?> openObjectForProperty(String id, List<String> types, Field targetField) {
        final Class<?> type = targetField.getType();
        final Class<?> targetClass = classResolver.getTargetClass(type, types);
        assert BeanAnnotationProcessor.isOwlClassEntity(targetClass);
        if (knownInstances.containsKey(id)) {
            return reopenExistingInstance(id, targetClass);
        } else {
            final Object instance = BeanClassProcessor.createInstance(targetClass);
            final InstanceContext<?> ctx = new SingularObjectContext<>(instance,
                    BeanAnnotationProcessor.mapFieldsForDeserialization(targetClass), knownInstances);
            ctx.setIdentifierValue(id);
            return ctx;
        }
    }

    private <T> InstanceContext<T> reopenExistingInstance(String id, Class<T> cls) {
        final Object instance = knownInstances.get(id);
        if (!cls.isAssignableFrom(instance.getClass())) {
            throw new TargetTypeException("An instance with id " + id + " already exists, but its type " + instance
                    .getClass() + " is not compatible with target type " + cls + ".");
        }
        return new SingularObjectContext<>(cls.cast(instance),
                BeanAnnotationProcessor.mapFieldsForDeserialization(cls), knownInstances);
    }

    @Override
    public <T> void openObject(String id, Class<T> cls) {
        if (BeanClassProcessor.isIdentifierType(cls)) {
            final InstanceContext<T> context = new NodeReferenceContext<>(currentInstance, knownInstances);
            context.setIdentifierValue(id);
            assert currentInstance != null;
            openInstances.push(currentInstance);
            this.currentInstance = context;
        } else {
            if (knownInstances.containsKey(id)) {
                final InstanceContext<T> context = reopenExistingInstance(id, cls);
                replaceCurrentContext(context);
            } else {
                final T instance = BeanClassProcessor.createInstance(cls);
                final InstanceContext<T> context = new SingularObjectContext<>(instance,
                        BeanAnnotationProcessor.mapFieldsForDeserialization(cls), knownInstances);
                replaceCurrentContext(context);
                currentInstance.setIdentifierValue(id);
            }
        }
    }

    private void replaceCurrentContext(InstanceContext<?> ctx) {
        if (currentInstance != null) {
            openInstances.push(currentInstance);
        }
        this.currentInstance = ctx;
    }

    @Override
    public void closeObject() {
        currentInstance.close();
        if (currentInstance.getIdentifier() != null) {
            pendingReferenceRegistry.resolveReferences(currentInstance.getIdentifier(), currentInstance.getInstance());
        }
        if (!openInstances.isEmpty()) {
            final InstanceContext<?> closing = this.currentInstance;
            this.currentInstance = openInstances.pop();
            // Add the item to the instance after closing it, so that all its fields have been initialized already (if they are needed by equals/hashCode)
            currentInstance.addItem(closing.getInstance());
        }
    }

    @Override
    public void openCollection(String property) {
        Objects.requireNonNull(property);
        final Field targetField = currentInstance.getFieldForProperty(property);
        final InstanceContext<?> ctx;
        if (targetField == null) {
            if (currentInstance.hasPropertiesField() && !JsonLd.TYPE.equals(property)) {
                ctx = buildPropertiesContext(property);
            } else {
                ctx = new DummyCollectionInstanceContext(knownInstances);
            }
        } else {
            if (MultilingualString.class.equals(targetField.getType())) {
                ctx = new MultilingualStringContext(new MultilingualString(), knownInstances);
            } else {
                verifyPluralAttribute(property, targetField);
                final Collection<?> instance = (Collection<?>) getCollectionForField(targetField);
                if (JsonLd.TYPE.equals(property)) {
                    ctx = new TypesContext(instance, knownInstances,
                            BeanClassProcessor.getCollectionItemType(targetField), currentInstance.getInstanceType());
                } else {
                    final Class<?> elementType = BeanClassProcessor.getCollectionItemType(targetField);
                    if (MultilingualString.class.equals(elementType)) {
                        ctx = new MultilingualStringCollectionContext<>((Collection<MultilingualString>) instance, knownInstances);
                    } else {
                        ctx = new CollectionInstanceContext<>(instance,
                                BeanClassProcessor.getCollectionItemType(targetField), knownInstances);
                    }
                }
            }
            currentInstance.setFieldValue(targetField, ctx.instance);
        }
        openInstances.push(currentInstance);
        this.currentInstance = ctx;
    }

    private InstanceContext<?> buildPropertiesContext(String property) {
        final Field propsField = BeanAnnotationProcessor.getPropertiesField(currentInstance.getInstanceType());
        BeanClassProcessor.verifyPropertiesFieldType(propsField);
        final Map<?, ?> propertiesMap = (Map<?, ?>) getCollectionForField(propsField);
        currentInstance.setFieldValue(propsField, propertiesMap);
        return new PropertiesInstanceContext(propertiesMap, property, propsField);
    }

    private void verifyPluralAttribute(String property, Field field) {
        if (!isPlural(property)) {
            throw JsonLdDeserializationException.singularAttributeCardinalityViolated(property, field);
        }
    }

    private Object getCollectionForField(Field targetField) {
        final Object existing = BeanClassProcessor.getFieldValue(targetField, currentInstance.getInstance());
        if (existing != null) {
            return existing;
        }
        return BeanAnnotationProcessor.isPropertiesField(targetField) ? new HashMap<>() :
                BeanClassProcessor.createCollection(targetField);
    }

    @Override
    public void openCollection(CollectionType collectionType) {
        final Collection<?> collection = BeanClassProcessor.createCollection(collectionType);
        final InstanceContext<?> context = new CollectionInstanceContext<>(collection, knownInstances);
        replaceCurrentContext(context);
    }

    @Override
    public void closeCollection() {
        if (!openInstances.isEmpty()) {
            currentInstance = openInstances.pop();
        }
    }

    @Override
    public void addValue(String property, Object value) {
        assert currentInstance != null;
        final Field targetField = currentInstance.getFieldForProperty(property);
        assert targetField != null;
        // This is in case there is only one value in the JSON-LD array, because then it might be treated as single valued attribute
        if (BeanClassProcessor.isCollection(targetField)) {
            openCollection(property);
            addValue(value);
            closeCollection();
        } else {
            currentInstance.setFieldValue(targetField, value);
        }
    }

    @Override
    public void addValue(Object value) {
        assert currentInstance != null;
        currentInstance.addItem(value);
    }

    @Override
    public void addNodeReference(String property, String nodeId) {
        final Field field = currentInstance.getFieldForProperty(property);
        assert field != null;
        final Class<?> type = field.getType();
        if (canDirectlyAddNodeReference(type)) {
            currentInstance.setFieldValue(field, DataTypeTransformer.transformValue(URI.create(nodeId), type));
        } else {
            if (knownInstances.containsKey(nodeId)) {
                currentInstance.setFieldValue(field, knownInstances.get(nodeId));
            } else {
                pendingReferenceRegistry.addPendingReference(nodeId, currentInstance.getInstance(), field);
            }
        }
    }

    @Override
    public void addNodeReference(String nodeId) {
        final Class<?> targetType = getCurrentCollectionElementType();
        if (canDirectlyAddNodeReference(targetType)) {
            currentInstance.addItem(DataTypeTransformer.transformValue(URI.create(nodeId), targetType));
        } else {
            if (knownInstances.containsKey(nodeId)) {
                currentInstance.addItem(knownInstances.get(nodeId));
            } else {
                pendingReferenceRegistry.addPendingReference(nodeId, (Collection) currentInstance.getInstance());
            }
        }
    }

    private boolean canDirectlyAddNodeReference(Class<?> targetType) {
        return BeanClassProcessor.isIdentifierType(targetType) || Objects.equals(Object.class, targetType) ||
                targetType == null;
    }

    @Override
    public Object getCurrentRoot() {
        return currentInstance != null ? currentInstance.getInstance() : null;
    }

    @Override
    public Class<?> getCurrentCollectionElementType() {
        try {
            return currentInstance.getItemType();
        } catch (UnsupportedOperationException e) {
            throw new JsonLdDeserializationException("The current instance is not a collection.", e);
        }
    }

    @Override
    public boolean isPlural(String property) {
        assert isPropertyMapped(property);
        final Field mappedField = currentInstance.getFieldForProperty(property);
        return mappedField == null || BeanClassProcessor.isCollection(mappedField);
    }

    @Override
    public boolean isPropertyMapped(String property) {
        return currentInstance.isPropertyMapped(property) || JsonLd.TYPE.equals(property);
    }

    @Override
    public boolean isPropertyDeserializable(String property) {
        return currentInstance.supports(property) || JsonLd.TYPE.equals(property);
    }

    @Override
    public Class<?> getCurrentContextType() {
        return currentInstance.getInstanceType();
    }

    @Override
    public boolean isCurrentCollectionProperties() {
        return currentInstance instanceof PropertiesInstanceContext;
    }

    @Override
    public Class<?> getTargetType(String property) {
        assert isPropertyMapped(property);
        final Field field = currentInstance.getFieldForProperty(property);
        if (field == null && currentInstance.hasPropertiesField()) {
            return Object.class;
        }
        assert field != null;
        return BeanClassProcessor.isCollection(field) ? BeanClassProcessor.getCollectionItemType(field) : field.getType();
    }
}
