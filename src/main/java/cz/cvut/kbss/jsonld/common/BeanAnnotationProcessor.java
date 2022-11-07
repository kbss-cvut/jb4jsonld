/**
 * Copyright (C) 2022 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.common;

import cz.cvut.kbss.jopa.model.annotations.Properties;
import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.annotation.JsonLdAttributeOrder;
import cz.cvut.kbss.jsonld.exception.JsonLdSerializationException;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BeanAnnotationProcessor {

    private static final String[] EMPTY_ARRAY = new String[0];
    private static final Predicate<Field> ALWAYS_TRUE = field -> true;

    private static PropertyAccessResolver propertyAccessResolver = new JsonLdPropertyAccessResolver();

    private BeanAnnotationProcessor() {
        throw new AssertionError();
    }

    /**
     * Sets property access resolver, overriding the default one.
     *
     * @param resolver Resolver to set
     */
    public static void setPropertyAccessResolver(PropertyAccessResolver resolver) {
        propertyAccessResolver = Objects.requireNonNull(resolver);
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
        return expandIriIfNecessary(owlClass.iri(), cls);
    }

    /**
     * Attempts to expand the specified IRI in case it is compacted (see {@link IdentifierUtil#isCompactIri(String)}) using JOPA namespace declarations.
     * <p>
     * If the IRI is not compact or no matching namespace is found, the original IRI is returned.
     *
     * @param iri            IRI to expand (if necessary and possible)
     * @param declaringClass Class in/on which the IRI is declared. It is used as base for namespace search
     * @return Expanded IRI if it was possible to expand it, original argument if not
     * @see IdentifierUtil#isCompactIri(String)
     * @see Namespaces
     * @see Namespace
     */
    public static String expandIriIfNecessary(String iri, Class<?> declaringClass) {
        Objects.requireNonNull(declaringClass);
        return IdentifierUtil.isCompactIri(iri) ? expandIri(iri, declaringClass).orElse(iri) : iri;
    }

    /**
     * Attempts to expand the specified compact IRI by finding a corresponding {@link Namespace} annotation in the specified class's ancestor hierarchy.
     * <p>
     * That is, it tries to find a {@link Namespace} annotation with matching prefix on the specified class or any of its ancestors. If such an annotation
     * is found, its namespace is concatenated with the suffix from the specified {@code iri} to produce the expanded version of the IRI.
     * <p>
     * If no matching {@link Namespace} annotation is found, an empty {@link Optional} is returned.
     *
     * @param iri            Compact IRI to expand
     * @param declaringClass Class in which the IRI was declared. Used to start search for namespace declaration
     * @return Expanded IRI if a matching namespace declaration is found, empty {@code Optional} if not
     */
    private static Optional<String> expandIri(String iri, Class<?> declaringClass) {
        assert IdentifierUtil.isCompactIri(iri);

        final int colonIndex = iri.indexOf(':');
        final String prefix = iri.substring(0, colonIndex);
        final String suffix = iri.substring(colonIndex + 1);
        Optional<String> ns = resolveNamespace(declaringClass, prefix);
        if (ns.isPresent()) {
            return ns.map(v -> v + suffix);
        }
        if (declaringClass.getPackage() != null) {
            ns = resolveNamespace(declaringClass.getPackage(), prefix);
            if (ns.isPresent()) {
                return ns.map(v -> v + suffix);
            }
        }
        return declaringClass.getSuperclass() != null ? expandIri(iri, declaringClass.getSuperclass()) :
                Optional.empty();
    }

    private static Optional<String> resolveNamespace(AnnotatedElement annotated, String prefix) {
        Namespace ns = annotated.getDeclaredAnnotation(Namespace.class);
        if (ns != null && ns.prefix().equals(prefix)) {
            return Optional.of(ns.namespace());
        }
        Namespaces namespaces = annotated.getDeclaredAnnotation(Namespaces.class);
        if (namespaces != null) {
            final Optional<Namespace> namespace =
                    Arrays.stream(namespaces.value()).filter(n -> n.prefix().equals(prefix)).findAny();
            if (namespace.isPresent()) {
                return namespace.map(Namespace::namespace);
            }
        }
        return Optional.empty();
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
                classes.add(expandIriIfNecessary(owlClass.iri(), c));
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
     *     <ul>
     *         <li>Non-static</li>
     *         <li>Annotated with one of the following annotations: {@link Id}, {@link OWLAnnotationProperty}, {@link
     * OWLDataProperty}, {@link OWLObjectProperty}</li>
     * <li>Not configured with {@link cz.cvut.kbss.jsonld.annotation.JsonLdProperty.Access#WRITE_ONLY} access</li>
     *     </ul>
     *
     * @param object Object whose fields should be discovered
     * @return List of discovered fields
     */
    public static List<Field> getSerializableFields(Object object) {
        Objects.requireNonNull(object);
        final Class<?> cls = object.getClass();
        return getMarshallableFields(cls, propertyAccessResolver::isReadable);
    }

    private static List<Field> getMarshallableFields(Class<?> cls, Predicate<Field> filter) {
        final List<Class<?>> classes = getAncestors(cls);
        final Set<Field> fields = new HashSet<>();
        for (Class<?> c : classes) {
            for (Field f : c.getDeclaredFields()) {
                if (!isFieldTransient(f) && filter.test(f)) {
                    fields.add(f);
                }
            }
        }
        return new ArrayList<>(fields);
    }

    /**
     * Gets all fields which can be serialized or deserialized from the specified class (or its supertypes).
     * <p>
     * is does not take into account property access configuration, just the fact that a field is:
     *
     * <ul>
     *    <li>Non-static</li>
     *    <li>Annotated with one of the following annotations: {@link Id}, {@link OWLAnnotationProperty}, {@link
     *    OWLDataProperty}, {@link OWLObjectProperty}</li>
     * </ul>
     *
     * @param cls Class to check
     * @return List of marshallable fields
     */
    public static List<Field> getMarshallableFields(Class<?> cls) {
        Objects.requireNonNull(cls);
        return getMarshallableFields(cls, ALWAYS_TRUE);
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
    public static Map<String, Field> mapFieldsForDeserialization(Class<?> cls) {
        Objects.requireNonNull(cls);
        final List<Field> fields = getMarshallableFields(cls);
        return fields.stream().filter(f -> !isPropertiesField(f)).collect(Collectors.toMap(
                BeanAnnotationProcessor::getAttributeIdentifier, Function.identity()));
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
        final List<Field> fields = getMarshallableFields(cls);
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
        final List<Field> fields = getMarshallableFields(cls);
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
        final List<Field> fields = getMarshallableFields(cls);
        return fields.stream().filter(BeanAnnotationProcessor::isTypesField).findFirst();
    }

    /**
     * Checks whether the specified class contains a {@link Types} field.
     *
     * @param cls The class to examine
     * @return Whether the class has types field
     */
    public static boolean hasTypesField(Class<?> cls) {
        return getTypesField(cls).isPresent();
    }

    /**
     * Checks whether the specified field is mapped to an OWL object property.
     *
     * @param field The field to examine
     * @return Whether field has a {@link OWLObjectProperty} annotation
     */
    public static boolean isObjectProperty(Field field) {
        return field != null && field.getDeclaredAnnotation(OWLObjectProperty.class) != null;
    }

    /**
     * Checks whether the specified field is mapped to an OWL annotation property.
     *
     * @param field The field to examine
     * @return Whether field has a {@link OWLAnnotationProperty} annotation
     */
    public static boolean isAnnotationProperty(Field field) {
        return field != null && field.getDeclaredAnnotation(OWLAnnotationProperty.class) != null;
    }

    /**
     * Checks whether the specified field is an identifier field.
     *
     * @param field The field to examine
     * @return Whether the field has a {@link Id} annotation
     */
    public static boolean isInstanceIdentifier(Field field) {
        return field != null && field.getDeclaredAnnotation(Id.class) != null;
    }

    /**
     * Checks whether the specified field is a {@link Types} field.
     *
     * @param field The field to examine
     * @return Whether the field has a {@link Types} annotation
     */
    public static boolean isTypesField(Field field) {
        return field != null && field.getDeclaredAnnotation(Types.class) != null;
    }

    /**
     * Checks whether deserialization can write into the specified field.
     *
     * @param field The field to examine
     * @return Write access status
     */
    public static boolean isWriteable(Field field) {
        return propertyAccessResolver.isWriteable(field);
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
            return expandIriIfNecessary(dp.iri(), field.getDeclaringClass());
        }
        final OWLObjectProperty op = field.getDeclaredAnnotation(OWLObjectProperty.class);
        if (op != null) {
            return expandIriIfNecessary(op.iri(), field.getDeclaringClass());
        }
        final OWLAnnotationProperty ap = field.getDeclaredAnnotation(OWLAnnotationProperty.class);
        if (ap != null) {
            return expandIriIfNecessary(ap.iri(), field.getDeclaringClass());
        }
        if (field.getDeclaredAnnotation(Types.class) != null) {
            return JsonLd.TYPE;
        }
        throw new JsonLdSerializationException("Field " + field + " is not JSON-LD serializable.");
    }

    public static Optional<Field> getIdentifierField(Class<?> cls) {
        return getMarshallableFields(cls, f -> f.isAnnotationPresent(Id.class)).stream().findFirst();
    }

    /**
     * Resolves value of the identifier attribute (i.e. annotated with {@link Id}) of the specified instance.
     *
     * @param instance Instance to get identifier value from
     * @return Identifier value
     */
    public static Optional<Object> getInstanceIdentifier(Object instance) {
        Objects.requireNonNull(instance);
        final List<Class<?>> classes = getAncestors(instance.getClass());
        for (Class<?> cls : classes) {
            for (Field f : cls.getDeclaredFields()) {
                if (f.getDeclaredAnnotation(Id.class) != null) {
                    if (!f.isAccessible()) {
                        f.setAccessible(true);
                    }
                    try {
                        return Optional.ofNullable(f.get(instance));
                    } catch (IllegalAccessException e) {
                        throw new JsonLdSerializationException("Unable to extract identifier of instance " + instance);
                    }
                }
            }
        }
        // No identifier field, a blank node should be generated
        return Optional.empty();
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
