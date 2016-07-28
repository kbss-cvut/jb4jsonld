package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.common.CollectionType;

import java.util.Collection;
import java.util.Stack;

/**
 * Default implementation of the JSON-LD deserializer, which takes values parsed from a JSON-LD document and builds
 * Java instances from them.
 */
public class DefaultJsonLdDeserializer implements JsonLdDeserializer {

    private final Stack<InstanceContext> openInstances = new Stack<>();

    private InstanceContext currentInstance;

    // TODO Add support for polymorphism
    @Override
    public void openObject(String property) {

    }

    @Override
    public <T> void openObject(Class<T> cls) {
        final T instance = BeanClassProcessor.createInstance(cls);
        final InstanceContext ctx = new InstanceContext(instance, BeanAnnotationProcessor.mapSerializableFields(cls));
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

    }

    @Override
    public void openCollection(CollectionType collectionType) {
        final Collection<?> collection = BeanClassProcessor.createCollection(collectionType);
        final InstanceContext context = new InstanceContext(collection);
        this.currentInstance = context;
    }

    @Override
    public void closeCollection() {

    }

    @Override
    public void addValue(String property, Object value) {

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
