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

    private InstanceContext currentInstance;

    // TODO Add support for polymorphism
    @Override
    public void openObject(String property) {
        Objects.requireNonNull(property);
        final Field targetField = currentInstance.getFieldForProperty(property);
        assert targetField != null;
        final Class<?> type = targetField.getType();
        assert BeanAnnotationProcessor.isOwlClassEntity(type);
        final Object instance = BeanClassProcessor.createInstance(type);
        final InstanceContext<?> ctx = new SingularObjectContext<>(instance,
                BeanAnnotationProcessor.mapSerializableFields(type), knownInstances);
        currentInstance.setFieldValue(targetField, instance);
        openInstances.push(currentInstance);
        this.currentInstance = ctx;
    }

    @Override
    public <T> void openObject(Class<T> cls) {
        final T instance = BeanClassProcessor.createInstance(cls);
        final SingularObjectContext<?> ctx = new SingularObjectContext<>(instance,
                BeanAnnotationProcessor.mapSerializableFields(cls), knownInstances);
        replaceCurrentContext(instance, ctx);
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
        Map<?, ?> propertiesMap = (Map<?, ?>) BeanClassProcessor
                .getFieldValue(propsField, currentInstance.getInstance());
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
        final Field targetField = currentInstance.getFieldForProperty(property);
        assert targetField != null;
        // This is in case there is only one value in the JSON-LD array, because then it might be treated as single valued attribute
        if (BeanClassProcessor.isCollection(targetField)) {
            openCollection(property);
            addValue(value);
            closeCollection();
        } else {
            currentInstance.setFieldValue(targetField, value);
            if (BeanAnnotationProcessor.isInstanceIdentifier(targetField)) {
                registerKnownInstance(targetField);
            }
        }
    }

    private void registerKnownInstance(Field targetField) {
        final Object instance = currentInstance.getInstance();
        knownInstances.put(BeanClassProcessor.getFieldValue(targetField, instance).toString(), instance);
    }

    @Override
    public void addValue(Object value) {
        assert currentInstance != null;
        currentInstance.addItem(value);
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
