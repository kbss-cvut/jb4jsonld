package cz.cvut.kbss.jsonld.serialization.traversal;

import java.lang.reflect.Field;

/**
 * Visitor for the object graph traversal.
 */
public interface InstanceVisitor {

    /**
     * Called when a new instance is discovered by the object graph traverser.
     *
     * @param instance The visited instance
     */
    void visitInstance(Object instance);

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
}
