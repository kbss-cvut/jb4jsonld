package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.common.CollectionType;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Default implementation of the JSON-LD deserializer, which takes values parsed from a JSON-LD document and builds
 * Java instances from them.
 */
public class DefaultJsonLdDeserializer implements JsonLdDeserializer {

    // Identifiers to instances
    private final Map<String, Object> knownInstances = new HashMap<>();
    private final Stack<InstanceContext> openInstances = new Stack<>();

    private InstanceContext currentInstance;

    // TODO Add support for polymorphism
    @Override
    public void openObject(String property) {
        Objects.requireNonNull(property);
        final Field targetField = currentInstance.getFieldForProperty(property);
        final Class<?> type = targetField.getType();
        assert BeanAnnotationProcessor.isOwlClassEntity(type);
        final Object instance = BeanClassProcessor.createInstance(type);
        final InstanceContext<?> ctx = new InstanceContext<>(instance,
                BeanAnnotationProcessor.mapSerializableFields(type), knownInstances);
        currentInstance.setFieldValue(targetField, instance);
        openInstances.push(currentInstance);
        this.currentInstance = ctx;
    }

    @Override
    public <T> void openObject(Class<T> cls) {
        final T instance = BeanClassProcessor.createInstance(cls);
        final InstanceContext<?> ctx = new InstanceContext<>(instance,
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
        final Collection<?> instance = BeanClassProcessor.createCollection(targetField);
        final InstanceContext<Collection<?>> ctx = new InstanceContext<>(instance, knownInstances);
        currentInstance.setFieldValue(targetField, instance);
        openInstances.push(currentInstance);
        this.currentInstance = ctx;
    }

    @Override
    public void openCollection(CollectionType collectionType) {
        final Collection<?> collection = BeanClassProcessor.createCollection(collectionType);
        final InstanceContext<?> context = new InstanceContext<>(collection, knownInstances);
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
        currentInstance.setFieldValue(targetField, value);

        if (BeanAnnotationProcessor.isInstanceIdentifier(targetField)) {
            registerKnownInstance(targetField);
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
}
