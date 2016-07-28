package cz.cvut.kbss.jsonld.common;

import cz.cvut.kbss.jsonld.exception.BeanProcessingException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class BeanClassProcessor {

    private BeanClassProcessor() {
        throw new AssertionError();
    }

    /**
     * Extracts value of the specified field, from the specified instance.
     *
     * @param field    The field to extract value from
     * @param instance Instance containing the field
     * @return Field value, possibly {@code null}
     */
    public static Object getFieldValue(Field field, Object instance) {
        Objects.requireNonNull(field);
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new BeanProcessingException("Unable to extract value of field " + field, e);
        }
    }

    /**
     * Creates new instance of the specified class.
     *
     * @param cls The class to instantiate
     * @return New instance
     * @throws BeanProcessingException If the class is missing a public no-arg constructor
     */
    public static <T> T createInstance(Class<T> cls) {
        try {
            return cls.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BeanProcessingException("Class " + cls + " is missing a public no-arg constructor.", e);
        }
    }

    /**
     * Creates collection of the specified type.
     *
     * @param type Collection type
     * @return New collection instance
     */
    public static Collection<?> createCollection(CollectionType type) {
        switch (type) {
            case LIST:
                return new ArrayList<>();
            case SET:
                return new HashSet<>();
            default:
                throw new IllegalArgumentException("Unsupported collection type " + type);
        }
    }
}
