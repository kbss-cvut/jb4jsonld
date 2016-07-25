package cz.cvut.kbss.jsonld.serialization.traversal;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Visitor for the object graph traversal.
 */
public interface InstanceVisitor {

    /**
     * Called when a new instance is discovered by the object graph traverser.
     * <p>
     * The instances attributes will be processed immediately after this method returns.
     *
     * @param instance The visited instance
     */
    void openInstance(Object instance);

    /**
     * Called when the graph traverser is done with traversing the specified instance.
     *
     * @param instance The visited instance
     * @see #openInstance(Object)
     */
    void closeInstance(Object instance);

    /**
     * Called when an already known instance is encountered again by the object graph traverser.
     * <p>
     * This version is for attributes.
     *
     * @param field    Field whose value is visited
     * @param instance The visited instance
     */
    void visitKnownInstance(Field field, Object instance);

    /**
     * Called when an already known instance is encountered again by the object graph traverser.
     *
     * @param instance The instance
     */
    void visitKnownInstance(Object instance);

    /**
     * Called when the object graph traverser discovers a JSON-LD-serializable field in an instance.
     *
     * @param field The visited field
     * @param value Value of the visited field
     */
    void visitField(Field field, Object value);

    /**
     * Called when a collection is encountered by the object traverser.
     * <p>
     * This can be either when a top-level collection is discovered, or when an object's attribute is a collection.
     *
     * @param collection The visited collection
     */
    void openCollection(Collection<?> collection);

    /**
     * Called after the last collection item is processed.
     *
     * @param collection The visited collection
     * @see #openCollection(Collection)
     */
    void closeCollection(Collection<?> collection);
}
