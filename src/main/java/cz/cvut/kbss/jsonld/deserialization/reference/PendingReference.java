package cz.cvut.kbss.jsonld.deserialization.reference;

/**
 * Represents a pending reference.
 * <p>
 * A pending reference represents a situation when the JSON-LD contains just an object with and {@code @id}, while the
 * mapped attribute expects a full-blown object. In this case, it is expected that somewhere in the JSON-LD, there is
 * the object's full serialization and this is only a reference to it.
 */
public interface PendingReference {

    /**
     * Applies the specified referenced object to this pending reference, resolving it.
     * <p>
     * Resolving the reference basically means inserting the referenced object into the place represented by this
     * instance (e.g., field value, collection).
     *
     * @param referencedObject The object referenced by this pending reference
     */
    void apply(Object referencedObject);
}
