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
        visitInstance(instance);
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
    }

    private void traverseObjectPropertyValue(Object value) throws IllegalAccessException {
        if (value instanceof Collection) {
            final Collection<?> col = (Collection<?>) value;
            for (Object elem : col) {
                traverseImpl(elem);
            }
        } else if (value.getClass().isArray()) {
            final Object[] arr = (Object[]) value;
            for (Object ob : arr) {
                traverseImpl(ob);
            }
        } else {
            traverseImpl(value);
        }
    }

    @Override
    public void visitInstance(Object instance) {
        knownInstances.put(instance, EMPTY_OBJECT);
        visitors.forEach(v -> v.visitInstance(instance));
    }

    @Override
    public void visitKnownInstance(Object instance) {
        visitors.forEach(v -> v.visitKnownInstance((instance)));
    }

    @Override
    public void visitField(Field field, Object value) {
        visitors.forEach(v -> v.visitField(field, value));
    }
}
