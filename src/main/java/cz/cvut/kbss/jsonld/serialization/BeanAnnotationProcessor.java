package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.jsonld.Constants;
import cz.cvut.kbss.jsonld.exception.JsonLdSerializationException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class BeanAnnotationProcessor {

    private BeanAnnotationProcessor() {
        throw new AssertionError();
    }

    /**
     * Resolves ontological type of the specified object, as specified by the {@link OWLClass} annotation.
     *
     * @param object The object to resolve
     * @return Resolved OWL type or {@code null} if the object's class is not annotated with {@link OWLClass}
     */
    public static Set<String> getOwlClasses(Object object) {
        Objects.requireNonNull(object);
        final Class<?> cls = object.getClass();
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

    private static boolean isFieldTransient(Field field) {
        return Modifier.isStatic(field.getModifiers()) || (
                field.getDeclaredAnnotation(OWLAnnotationProperty.class) == null &&
                        field.getDeclaredAnnotation(OWLDataProperty.class) == null &&
                        field.getDeclaredAnnotation(OWLObjectProperty.class) == null &&
                        field.getDeclaredAnnotation(Id.class) == null);
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
     * Resolves JSON-LD attribute identifier of the specified field.
     * <p>
     * For OWL properties, this will be their IRI. For id fields it will be the {@link
     * cz.cvut.kbss.jsonld.Constants#JSON_LD_ID} string.
     *
     * @param field The field to resolve
     * @return JSON-LD attribute identifier
     */
    public static String getAttributeIdentifier(Field field) {
        if (field.getDeclaredAnnotation(Id.class) != null) {
            return Constants.JSON_LD_ID;
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
        throw new JsonLdSerializationException("Field " + field + " is not JSON-LD serializable.");
    }

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
}
