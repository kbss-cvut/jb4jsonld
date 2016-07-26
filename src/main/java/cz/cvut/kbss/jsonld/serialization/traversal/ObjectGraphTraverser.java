package cz.cvut.kbss.jsonld.serialization.traversal;

import cz.cvut.kbss.jsonld.exception.JsonLdSerializationException;
import cz.cvut.kbss.jsonld.serialization.BeanAnnotationProcessor;

import java.lang.reflect.Field;
import java.util.*;

public class ObjectGraphTraverser implements InstanceVisitor {

    private static final Object EMPTY_OBJECT = new Object();

    private final Set<InstanceVisitor> visitors = new HashSet<>(4);

    private Map<Object, Object> knownInstances;

    public void addVisitor(InstanceVisitor visitor) {
        Objects.requireNonNull(visitor);
        visitors.add(visitor);
    }

    private void resetKnownInstances() {
        this.knownInstances = new IdentityHashMap<>();
    }

    public void traverse(Object instance) {
        Objects.requireNonNull(instance);
        resetKnownInstances();
        try {
            traverseImpl(instance);
        } catch (IllegalAccessException e) {
            throw new JsonLdSerializationException("Unable to extract field value.", e);
        }
    }

    private void traverseImpl(Object instance) throws IllegalAccessException {
        if (knownInstances.containsKey(instance)) {
            visitKnownInstance(instance);
            return;
        }
        openInstance(instance);
        for (Field f : BeanAnnotationProcessor.getSerializableFields(instance)) {
            if (!f.isAccessible()) {
                f.setAccessible(true);
            }
            final Object value = f.get(instance);
            visitField(f, value);
            if (value != null && BeanAnnotationProcessor.isObjectProperty(f)) {
                traverseObjectPropertyValue(value);
            }
        }
        closeInstance(instance);
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
        knownInstances.put(instance, EMPTY_OBJECT);
        visitors.forEach(v -> v.openInstance(instance));
    }

    @Override
    public void closeInstance(Object instance) {
        visitors.forEach(v -> v.closeInstance(instance));
    }

    @Override
    public void visitKnownInstance(Object instance) {
        visitors.forEach(v -> v.visitKnownInstance((instance)));
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
