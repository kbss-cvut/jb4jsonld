package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jsonld.common.CollectionType;
import cz.cvut.kbss.jsonld.exception.UnknownPropertyException;

/**
 * Builds instances from parsed JSON-LD.
 */
public interface JsonLdDeserializer {

    /**
     * Creates new instance to fill filled mapped by the specified property.
     * <p>
     * The instance type is determined from the declared type of the mapped field, which is taken from the currently
     * open object. Therefore, another object has to be already open before this method can be called.
     * <p>
     * The new instance also becomes the currently open object.
     * <p>
     * This method should also verify cardinality, i.e. multiple objects cannot be set for the same property, if the
     * field it maps to is singular.
     *
     * @param property Property identifier (IRI)
     * @throws IllegalStateException    If there is no {@link OWLClass} instance open
     * @throws UnknownPropertyException If a matching field for the specified property is not found
     */
    void openObject(String property);

    /**
     * Creates new instance of the specified class.
     * <p>
     * If there is a collection currently open, it adds the new instance to it.
     * <p>
     * The new instance also becomes the currently open object.
     * <p>
     * This method is intended for creating top level objects or adding objects to collections. Use {@link
     * #openObject(String)} for opening objects as values of attributes.
     *
     * @param cls Java type of the object being open
     * @see #openObject(String)
     */
    <T> void openObject(Class<T> cls);

    /**
     * Closes the most recently open object.
     */
    void closeObject();

    /**
     * Creates new instance of appropriate collection and sets it as value of the specified property of the
     * currently open object.
     * <p>
     * The collection type is determined from the declared type of the mapped field, which is taken from the currently
     * open object. Therefore, another object has to be already open before this method can be called.
     * <p>
     * The new collection also becomes the currently open object.
     * <p>
     * This method should also verify cardinality, i.e. a collection cannot be set as value of a
     * field mapped by {@code property}, if the field is singular.
     *
     * @param property Property identifier (IRI)
     * @throws IllegalStateException    If there is no {@link OWLClass} instance open
     * @throws UnknownPropertyException If a matching field for the specified property is not found
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
     *
     * @param property Property identifier (IRI)
     * @param value    The value to set
     * @throws IllegalStateException    If there is no {@link OWLClass} instance open
     * @throws UnknownPropertyException If a matching field for the specified property is not found
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
     * Returns current root of the deserialized object graph.
     *
     * @return Object graph root, it can be a {@link OWLClass} instance, or a {@link java.util.Collection}
     */
    Object getCurrentRoot();
}
