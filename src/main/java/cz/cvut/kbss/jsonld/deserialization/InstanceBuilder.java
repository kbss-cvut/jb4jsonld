/**
 * Copyright (C) 2020 Czech Technical University in Prague
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

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.Types;
import cz.cvut.kbss.jsonld.common.CollectionType;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;

import java.util.List;

/**
 * Builds instances from parsed JSON-LD.
 */
public interface InstanceBuilder {

    /**
     * Creates new instance to fill filled mapped by the specified property.
     * <p>
     * The instance type is determined from the declared type of the mapped field, which is taken from the currently
     * open object, and from the specified types. Therefore, another object has to be already open before this method
     * can be called.
     * <p>
     * The new instance also becomes the currently open object.
     * <p>
     * This method should also verify cardinality, i.e. multiple objects cannot be set for the same property, if the
     * field it maps to is singular.
     * <p>
     * This method assumes that the property is mapped, i.e. that {@link #isPropertyMapped(String)} returned true.
     *
     * @param id       Identifier of the object being open
     * @param property Property identifier (IRI)
     * @param types    Types of the object being open
     * @throws IllegalStateException If there is no {@link OWLClass} instance open
     */
    void openObject(String id, String property, List<String> types);

    /**
     * Creates new instance of the specified class.
     * <p>
     * If there is a collection currently open, it adds the new instance to it.
     * <p>
     * The new instance also becomes the currently open object.
     * <p>
     * This method is intended for creating top level objects or adding objects to collections. Use {@link
     * #openObject(String, String, List)} for opening objects as values of attributes.
     *
     * @param id  Identifier of the object being open
     * @param cls Java type of the object being open
     * @see #openObject(String, String, List)
     */
    <T> void openObject(String id, Class<T> cls);

    /**
     * Closes the most recently open object.
     */
    void closeObject();

    /**
     * Creates new instance of appropriate collection and sets it as value of the specified property of the currently
     * open object.
     * <p>
     * The collection type is determined from the declared type of the mapped field, which is taken from the currently
     * open object. Therefore, another object has to be already open before this method can be called.
     * <p>
     * The new collection also becomes the currently open object.
     * <p>
     * This method should also verify cardinality, i.e. a collection cannot be set as value of a field mapped by {@code
     * property}, if the field is singular.
     * <p>
     * This method assumes that the property is mapped, i.e. that {@link #isPropertyMapped(String)} returned true.
     *
     * @param property Property identifier (IRI)
     * @throws IllegalStateException If there is no {@link OWLClass} instance open
     */
    void openCollection(String property);

    /**
     * Creates new instance of the specified collection type.
     * <p>
     * If there is a collection currently open, it adds the new collection as its new item.
     * <p>
     * The new collection also becomes the currently open object.
     * <p>
     * This method is intended for creating top level collections or nesting collections. Use {@link
     * #openCollection(String)} for opening collections as values of attributes.
     *
     * @param collectionType Type of the JSON collection to instantiate in Java
     * @see #openCollection(String)
     */
    void openCollection(CollectionType collectionType);

    /**
     * Closes the most recently open collection.
     */
    void closeCollection();

    /**
     * Adds the specified value of the specified property to the currently open object.
     * <p>
     * This method is intended for non-composite JSON values like String, Number Boolean and {@code null}. It can also
     * handle IRIs of objects already parsed by the deserializer, which are serialized as Strings in JSON-LD. In this
     * case, the field is filled with the deserialized object.
     * <p>
     * This method should also verify cardinality and correct typing, e.g. multiple values cannot be set for the same
     * property, if the field it maps to is singular.
     * <p>
     * This method assumes that the property is mapped, i.e. that {@link #isPropertyMapped(String)} returned true.
     *
     * @param property Property identifier (IRI)
     * @param value    The value to set
     * @throws IllegalStateException If there is no {@link OWLClass} instance open
     */
    void addValue(String property, Object value);

    /**
     * Adds the specified value to the currently open collection.
     * <p>
     * This method is intended for non-composite JSON values like String, Number Boolean and {@code null}. It can also
     * handle IRIs of objects already parsed by the deserializer, which are serialized as Strings in JSON-LD. In this
     * case, the deserialized object is added to the collection.
     *
     * @param value The value to add
     */
    void addValue(Object value);

    /**
     * Adds a reference to a node to the currently open object.
     * <p>
     * This methods is invoked in case the deserializer encounters an object with a single attribute - @id. This assumes
     * that the node either references an already encountered object (known instance) or that the node is a value of a
     * plain identifier-valued object property.
     * <p>
     * This method should also verify cardinality and correct typing, e.g. multiple values cannot be set for the same
     * property, if the field it maps to is singular.
     * <p>
     * It is assumed that the property is mapped, i.e. that {@link #isPropertyMapped(String)} returned true.
     *
     * @param property Property identifier (IRI)
     * @param nodeId   Identifier (IRI) of the node, i.e. value of the @id attribute
     */
    void addNodeReference(String property, String nodeId);

    /**
     * Adds the specified value to the currently open collection.
     * <p>
     * This methods is invoked in case the deserializer encounters an object with a single attribute - @id. This assumes
     * that the node either references an already encountered object (known instance) or that the node is a value of a
     * plain identifier-valued object property.
     *
     * @param nodeId Identifier (IRI) of the node, i.e. value of the @id attribute
     */
    void addNodeReference(String nodeId);

    /**
     * Returns current root of the deserialized object graph.
     *
     * @return Object graph root, it can be a {@link OWLClass} instance, or a {@link java.util.Collection}
     */
    Object getCurrentRoot();

    /**
     * Returns the declared type of elements of the current instance, if it is a collection.
     *
     * @return Collection element type
     * @throws JsonLdDeserializationException If the current instance is not a collection
     */
    Class<?> getCurrentCollectionElementType();

    /**
     * Checks whether the current collection context represents a {@link cz.cvut.kbss.jopa.model.annotations.Properties}
     * attribute.
     *
     * @return {@code true} if the current context is a collection representing a {@link
     * cz.cvut.kbss.jopa.model.annotations.Properties} field
     */
    boolean isCurrentCollectionProperties();

    /**
     * Gets the Java type of the current object context.
     *
     * @return Java class of the instance currently being built
     */
    Class<?> getCurrentContextType();

    /**
     * Checks whether the specified property is mapped to a plural field.
     * <p>
     * This method assumes that the property is mapped, i.e. that {@link #isPropertyMapped(String)} returned true.
     * <p>
     * Note that {@link Types} and {@link cz.cvut.kbss.jopa.model.annotations.Properties} fields are always treated as
     * plural.
     *
     * @param property Property identifier (IRI)
     * @return Whether mapped field is collection-valued or not
     */
    boolean isPlural(String property);

    /**
     * Checks whether the specified property is mapped by the class representing the current instance context.
     * <p>
     * Returns true also for the {@link cz.cvut.kbss.jsonld.JsonLd#TYPE} property, even though the target instance may
     * not contain a {@link Types} field. The builder has to be able to handle types no matter whether a types field is
     * present or not.
     *
     * @param property Property identifier (IRI)
     * @return Whether the property is mapped in the current instance context
     */
    boolean isPropertyMapped(String property);

    /**
     * Checks whether the specified property can be deserialized.
     * <p>
     * A property cannot be deserialized if it is not mapped or if the mapped field's access is read-only.
     *
     * @param property Property identifier (IRI)
     * @return Whether property can be deserialized
     * @see #isPropertyMapped(String)
     * @see cz.cvut.kbss.jsonld.annotation.JsonLdProperty.Access#READ_ONLY
     */
    boolean isPropertyDeserializable(String property);
}
