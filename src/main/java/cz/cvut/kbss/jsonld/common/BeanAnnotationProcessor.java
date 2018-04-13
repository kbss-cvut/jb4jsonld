/**
 * Copyright (C) 2017 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.common;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.jopa.model.annotations.Properties;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.annotation.JsonLdAttributeOrder;
import cz.cvut.kbss.jsonld.exception.JsonLdSerializationException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class BeanAnnotationProcessor {

    private static final String[] EMPTY_ARRAY = new String[0];

    private BeanAnnotationProcessor() {
        throw new AssertionError();
    }

    /**
     * Checks whether the specified class is annotated with the {@link OWLClass} annotation.
     *
     * @param cls The class to examine
     * @return Whether it is annotated with {@link OWLClass}
     */
    public static boolean isOwlClassEntity(Class<?> cls) {
        return cls != null && cls.getDeclaredAnnotation(OWLClass.class) != null;
    }

    /**
     * Gets IRI of the OWL class represented by the specified Java class.
     *
     * @param cls Java class to examine
     * @return Represented ontological class
     * @throws IllegalArgumentException If the specified class is not mapped by {@link OWLClass}
     */
    public static String getOwlClass(Class<?> cls) {
        Objects.requireNonNull(cls);
        final OWLClass owlClass = cls.getDeclaredAnnotation(OWLClass.class);
        if (owlClass == null) {
            throw new IllegalArgumentException(cls + " is not an OWL class entity.");
        }
        return owlClass.iri();
    }

    /**
     * Resolves ontological types of the specified object, as specified by the {@link OWLClass} annotation.
     *
     * @param object The object to resolve
     * @return Resolved OWL types or an empty set if the object's class is not annotated with {@link OWLClass}
     * @see #getOwlClasses(Class)
     */
    public static Set<String> getOwlClasses(Object object) {
        Objects.requireNonNull(object);
        final Class<?> cls = object.getClass();
        return getOwlClasses(cls);
    }

    /**
     * Resolves a transitive closure of ontological types of the specified class, as specified by the {@link OWLClass}
     * annotation.
     * <p>
     * I.e. the result contains also types mapped by the class's ancestors.
     *
     * @param cls The class to process
     * @return Set of mapped ontological classes (possibly empty)
     */
    public static Set<String> getOwlClasses(Class<?> cls) {
        final Set<String> classes = new HashSet<>();
        getAncestors(cls).forEach(c -> {
            final OWLClass owlClass = c.getDeclaredAnnotation(OWLClass.class);
            if (owlClass != null) {
                classes.add(owlClass.iri());
            }
        });
        return classes;
    }

    private static List<Class<?>> getAncestors(Class<?> cls) {
        final List<Class<?>> classes = new ArrayList<>();
        Class<?> current = cls;
        while (current != null && !current.equals(Object.class)) {
            classes.add(current);
            current = current.getSuperclass();
        }
        return classes;
    }

    /**
     * Returns all fields of the class of the specified object and its superclasses, which can be serialized into
     * JSON-LD.
     * <p>
     * For a field to be serializable, it has to be:
     * <pre>
     *     <ul>
     *         <li>Non-static</li>
     *         <li>Annotated with one of the following annotations: {@link Id}, {@link OWLAnnotationProperty}, {@link
     * OWLDataProperty}, {@link OWLObjectProperty}</li>
     *     </ul>
     * </pre>
     *
     * @param object Object whose fields should be discovered
     * @return List of discovered fields
     */
    public static List<Field> getSerializableFields(Object object) {
        Objects.requireNonNull(object);
        final Class<?> cls = object.getClass();
        return getSerializableFields(cls);
    }

    private static List<Field> getSerializableFields(Class<?> cls) {
        final List<Class<?>> classes = getAncestors(cls);
        final Set<Field> fields = new HashSet<>();
        for (Class<?> c : classes) {
            for (Field f : c.getDeclaredFields()) {
                if (!isFieldTransient(f)) {
                    fields.add(f);
                }
            }
        }
        return new ArrayList<>(fields);
    }

    /**
     * Creates a map of JSON-LD serializable fields, where the keys are IRIs of properties mapped by the fields.
     * <p>
     * Identifier field is mapped to the {@link JsonLd#ID} property identifier. Ancestors of the specified class are
     * also scanned.
     *
     * @param cls Class for which the mapping should be determined
     * @return Mapping of OWL properties to fields
     */
    public static Map<String, Field> mapSerializableFields(Class<?> cls) {
        Objects.requireNonNull(cls);
        final List<Field> fields = getSerializableFields(cls);
        final Map<String, Field> fieldMap = new HashMap<>(fields.size());
        fields.stream().filter(f -> !isPropertiesField(f)).forEach(f -> fieldMap.put(getAttributeIdentifier(f), f));
        return fieldMap;
    }

    private static boolean isFieldTransient(Field field) {
        return Modifier.isStatic(field.getModifiers()) || (
                field.getDeclaredAnnotation(OWLAnnotationProperty.class) == null &&
                        field.getDeclaredAnnotation(OWLDataProperty.class) == null &&
                        field.getDeclaredAnnotation(OWLObjectProperty.class) == null &&
                        field.getDeclaredAnnotation(Id.class) == null) &&
                field.getDeclaredAnnotation(Types.class) == null &&
                field.getDeclaredAnnotation(Properties.class) == null;
    }

    /**
     * Checks whether the specified field is a {@link Properties} field.
     *
     * @param field The field to examine
     * @return Whether the field has the {@link Properties} annotation
     */
    public static boolean isPropertiesField(Field field) {
        return field.getDeclaredAnnotation(Properties.class) != null;
    }

    /**
     * Checks whether the specified class contains a {@link Properties} field.
     *
     * @param cls The class to examine
     * @return Whether the class has properties field
     */
    public static boolean hasPropertiesField(Class<?> cls) {
        Objects.requireNonNull(cls);
        final List<Field> fields = getSerializableFields(cls);
        final Optional<Field> propertiesField = fields.stream().filter(BeanAnnotationProcessor::isPropertiesField)
                                                      .findAny();
        return propertiesField.isPresent();
    }

    /**
     * Retrieves a field representing {@link Properties} in the specified class.
     *
     * @param cls The class to get properties field from
     * @return Properties field
     * @throws IllegalArgumentException When the specified class does not have a {@link Properties} field
     */
    public static Field getPropertiesField(Class<?> cls) {
        final List<Field> fields = getSerializableFields(cls);
        final Optional<Field> propsField = fields.stream().filter(BeanAnnotationProcessor::isPropertiesField).findAny();
        return propsField.orElseThrow(() -> new IllegalArgumentException(cls + " does not have a @Properties field."));
    }

    /**
     * Extracts types field from the specified class or any of its ancestors.
     * <p>
     * This method assumes there is at most one types field in the class hierarchy.
     *
     * @param cls The class to scan
     * @return Types field
     */
    public static Optional<Field> getTypesField(Class<?> cls) {
        final List<Field> fields = getSerializableFields(cls);
        return fields.stream().filter(BeanAnnotationProcessor::isTypesField).findFirst();
    }

    /**
     * Checks whether the specified field is mapped to an OWL object property.
     *
     * @param field The field to examine
     * @return Whether field has a {@link OWLObjectProperty} annotation
     */
    public static boolean isObjectProperty(Field field) {
        Objects.requireNonNull(field);
        return field.getDeclaredAnnotation(OWLObjectProperty.class) != null;
    }

    /**
     * Checks whether the specified field is an identifier field.
     *
     * @param field The field to examine
     * @return Whether the field as a {@link Id} annotation
     */
    public static boolean isInstanceIdentifier(Field field) {
        Objects.requireNonNull(field);
        return field.getDeclaredAnnotation(Id.class) != null;
    }

    public static boolean isTypesField(Field field) {
        Objects.requireNonNull(field);
        return field.getDeclaredAnnotation(Types.class) != null;
    }

    /**
     * Resolves JSON-LD attribute identifier of the specified field.
     * <p>
     * For OWL properties, this will be their IRI. For id fields it will be the {@link JsonLd#ID} string.
     *
     * @param field The field to resolve
     * @return JSON-LD attribute identifier
     */
    public static String getAttributeIdentifier(Field field) {
        if (field.getDeclaredAnnotation(Id.class) != null) {
            return JsonLd.ID;
        }
        final OWLDataProperty dp = field.getDeclaredAnnotation(OWLDataProperty.class);
        if (dp != null) {
            return dp.iri();
        }
        final OWLObjectProperty op = field.getDeclaredAnnotation(OWLObjectProperty.class);
        if (op != null) {
            return op.iri();
        }
        final OWLAnnotationProperty ap = field.getDeclaredAnnotation(OWLAnnotationProperty.class);
        if (ap != null) {
            return ap.iri();
        }
        if (field.getDeclaredAnnotation(Types.class) != null) {
            return JsonLd.TYPE;
        }
        throw new JsonLdSerializationException("Field " + field + " is not JSON-LD serializable.");
    }

    /**
     * Resolves value of the identifier attribute (i.e. annotated with {@link Id}) of the specified instance.
     *
     * @param instance Instance to get identifier value from
     * @return Identifier value
     */
    public static Object getInstanceIdentifier(Object instance) {
        Objects.requireNonNull(instance);
        final List<Class<?>> classes = getAncestors(instance.getClass());
        for (Class<?> cls : classes) {
            for (Field f : cls.getDeclaredFields()) {
                if (f.getDeclaredAnnotation(Id.class) != null) {
                    if (!f.isAccessible()) {
                        f.setAccessible(true);
                    }
                    try {
                        return f.get(instance);
                    } catch (IllegalAccessException e) {
                        throw new JsonLdSerializationException("Unable to extract identifier of instance " + instance);
                    }
                }
            }
        }
        throw new JsonLdSerializationException("Instance " + instance + " contains no valid identifier field.");
    }

    /**
     * Retrieves an array of attribute names specifying (partial) order in which they should be (de)serialized.
     *
     * @param cls Class whose attribute order should be retrieved
     * @return Array declaring attribute order, possibly empty
     */
    public static String[] getAttributeOrder(Class<?> cls) {
        Objects.requireNonNull(cls);
        final JsonLdAttributeOrder order = cls.getDeclaredAnnotation(JsonLdAttributeOrder.class);
        return order != null ? order.value() : EMPTY_ARRAY;
    }
}
