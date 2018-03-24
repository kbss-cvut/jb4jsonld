/**
 * Copyright (C) 2017 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.common.CollectionType;
import cz.cvut.kbss.jsonld.deserialization.util.TargetClassResolver;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;

import java.lang.reflect.Field;
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

    private InstanceContext currentInstance;

    public DefaultInstanceBuilder(TargetClassResolver classResolver) {
        this.classResolver = classResolver;
    }

    @Override
    public void openObject(String property, List<String> types) {
        Objects.requireNonNull(property);
        final Field targetField = currentInstance.getFieldForProperty(property);
        assert targetField != null;
        final Class<?> type = targetField.getType();
        final InstanceContext<?> ctx;
        if (BeanClassProcessor.isIdentifierType(type)) {    // This will be useful for Enhancement #5
            ctx = new NodeReferenceContext<>(currentInstance, targetField, knownInstances);
        } else {
            assert BeanAnnotationProcessor.isOwlClassEntity(type);
            final Class<?> targetClass = classResolver.getTargetClass(type, types);
            final Object instance = BeanClassProcessor.createInstance(targetClass);
            ctx = new SingularObjectContext<>(instance,
                    BeanAnnotationProcessor.mapSerializableFields(targetClass), knownInstances);
            currentInstance.setFieldValue(targetField, instance);
        }
        openInstances.push(currentInstance);
        this.currentInstance = ctx;
    }

    @Override
    public <T> void openObject(Class<T> cls) {
        if (BeanClassProcessor.isIdentifierType(cls)) {     // This will be useful for Enhancement #5
            final InstanceContext<T> context = new NodeReferenceContext<>(currentInstance, knownInstances);
            assert currentInstance != null;
            openInstances.push(currentInstance);
            this.currentInstance = context;
        } else {
            final T instance = BeanClassProcessor.createInstance(cls);
            final InstanceContext<T> context = new SingularObjectContext<>(instance,
                    BeanAnnotationProcessor.mapSerializableFields(cls), knownInstances);
            replaceCurrentContext(instance, context);
        }
    }

    private <T> void replaceCurrentContext(T instance, InstanceContext<?> ctx) {
        if (currentInstance != null) {
            currentInstance.addItem(instance);
            openInstances.push(currentInstance);
        }
        this.currentInstance = ctx;
    }

    @Override
    public void closeObject() {
        currentInstance.close();
        if (!openInstances.isEmpty()) {
            this.currentInstance = openInstances.pop();
        }
    }

    @Override
    public void openCollection(String property) {
        Objects.requireNonNull(property);
        final Field targetField = currentInstance.getFieldForProperty(property);
        final InstanceContext<?> ctx;
        if (targetField == null) {
            if (BeanAnnotationProcessor.hasPropertiesField(currentInstance.getInstanceType()) &&
                    !JsonLd.TYPE.equals(property)) {
                ctx = buildPropertiesContext(property);
            } else {
                ctx = new DummyCollectionInstanceContext(knownInstances);
            }
        } else {
            verifyPluralAttribute(property, targetField);
            final Collection<?> instance = BeanClassProcessor.createCollection(targetField);
            if (JsonLd.TYPE.equals(property)) {
                ctx = new TypesContext(instance, knownInstances,
                        BeanClassProcessor.getCollectionItemType(targetField), currentInstance.getInstanceType());
            } else {
                ctx = new CollectionInstanceContext<>(instance, BeanClassProcessor.getCollectionItemType(targetField),
                        knownInstances);
            }
            currentInstance.setFieldValue(targetField, instance);
        }
        openInstances.push(currentInstance);
        this.currentInstance = ctx;
    }

    private InstanceContext<?> buildPropertiesContext(String property) {
        final Field propsField = BeanAnnotationProcessor.getPropertiesField(currentInstance.getInstanceType());
        BeanClassProcessor.verifyPropertiesFieldType(propsField);
        Map<?, ?> propertiesMap =
                (Map<?, ?>) BeanClassProcessor.getFieldValue(propsField, currentInstance.getInstance());
        if (propertiesMap == null) {
            propertiesMap = new HashMap<>();
            currentInstance.setFieldValue(propsField, propertiesMap);
        }
        return new PropertiesInstanceContext(propertiesMap, property, propsField);
    }

    private void verifyPluralAttribute(String property, Field field) {
        if (!isPlural(property)) {
            throw JsonLdDeserializationException.singularAttributeCardinalityViolated(property, field);
        }
    }

    @Override
    public void openCollection(CollectionType collectionType) {
        final Collection<?> collection = BeanClassProcessor.createCollection(collectionType);
        final InstanceContext<?> context = new CollectionInstanceContext<>(collection, knownInstances);
        replaceCurrentContext(collection, context);
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
        if (JsonLd.ID.equals(property)) {
            currentInstance.setIdentifierValue(value);
            return;
        }
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
        if (BeanClassProcessor.isIdentifierType(type)) {
            currentInstance.setFieldValue(field, nodeId);
        } else {
            currentInstance.setFieldValue(field, getKnownInstance(nodeId));
        }
    }

    private Object getKnownInstance(String nodeId) {
        if (!knownInstances.containsKey(nodeId)) {
            throw new JsonLdDeserializationException(
                    "Node with IRI " + nodeId + " cannot be referenced, because it has not been encountered yet.");
        }
        return knownInstances.get(nodeId);
    }

    @Override
    public void addNodeReference(String nodeId) {
        final Class<?> targetType = getCurrentCollectionElementType();
        if (BeanClassProcessor.isIdentifierType(targetType)) {
            currentInstance.addItem(nodeId);
        } else {
            currentInstance.addItem(getKnownInstance(nodeId));
        }
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
    public Class<?> getCurrentContextType() {
        return currentInstance.getInstanceType();
    }
}
