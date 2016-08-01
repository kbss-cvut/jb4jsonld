package cz.cvut.kbss.jsonld.common;

import cz.cvut.kbss.jsonld.exception.BeanProcessingException;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

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
     * Sets value of the specified field.
     *
     * @param field    The field to set
     * @param instance Instance on which the field will be set
     * @param value    The value to use
     */
    public static void setFieldValue(Field field, Object instance, Object value) {
        Objects.requireNonNull(field);
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new BeanProcessingException("Unable to set value of field " + field, e);
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

    /**
     * Creates a collection to fill the specified field.
     * <p>
     * I.e. the type of the collection is determined from the declared type of the field.
     *
     * @param field The field to create collection for
     * @return New collection instance
     */
    public static Collection<?> createCollection(Field field) {
        Objects.requireNonNull(field);
        CollectionType type;
        if (Set.class.isAssignableFrom(field.getType())) {
            type = CollectionType.SET;
        } else if (List.class.isAssignableFrom(field.getType())) {
            type = CollectionType.LIST;
        } else {
            throw new IllegalArgumentException("Field " + field + " is not of any supported collection type.");
        }
        return createCollection(type);
    }

    /**
     * Determines the declared element type of collection represented by the specified field.
     *
     * @param field Field whose type is a collection
     * @return Declared element type of the collection
     */
    public static Class<?> getCollectionItemType(Field field) {
        Objects.requireNonNull(field);
        try {
            final ParameterizedType fieldType = (ParameterizedType) field.getGenericType();
            return (Class<?>) fieldType.getActualTypeArguments()[0];
        } catch (ClassCastException e) {
            throw new BeanProcessingException("Field " + field + "is not a collection-valued field.");
        }
    }

    /**
     * Checks whether the specified field represents a collection.
     *
     * @param field The field to examine
     * @return Whether the field is a collection or not
     */
    public static boolean isCollection(Field field) {
        Objects.requireNonNull(field);
        return Collection.class.isAssignableFrom(field.getType());
    }
}
