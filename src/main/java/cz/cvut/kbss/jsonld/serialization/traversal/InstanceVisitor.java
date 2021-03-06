package cz.cvut.kbss.jsonld.serialization.traversal;

import java.util.Collection;

public interface InstanceVisitor {

    /**
     * Called when a new instance is discovered by the object graph traverser.
     * <p>
     * The instances attributes will be processed immediately after this method returns.
     *
     * @param ctx Current serialization context
     */
    void openObject(SerializationContext<?> ctx);

    /**
     * Called when the graph traverser is done with traversing the current instance.
     *
     * @param ctx Current serialization context
     * @see #openObject(SerializationContext)
     */
    void closeObject(SerializationContext<?> ctx);

    /**
     * Called when an attribute is processed by the object graph traverser.
     * <p>
     * Note that identifiers ({@link cz.cvut.kbss.jopa.model.annotations.Id}) and types ({@link cz.cvut.kbss.jopa.model.annotations.Types})
     * are processed separately and are not visited as attributes. Also, when processing {@link cz.cvut.kbss.jopa.model.annotations.Properties}, this method
     * is invoked for each property in the map.
     *
     * @param ctx Current serialization context
     */
    void visitAttribute(SerializationContext<?> ctx);

    /**
     * Called when the identifier of an instance (JSON-LD {@code @id} attribute) is encountered.
     * <p>
     * This may be invoked out of order, at the beginning of processing an object.
     *
     * @param ctx Current serialization context
     */
    void visitIdentifier(SerializationContext<String> ctx);

    /**
     * Called when the types of an instance (JSON-LD {@code @type} attribute) are serialized.
     *
     * @param ctx Current serialization context
     */
    void visitTypes(SerializationContext<Collection<String>> ctx);

    /**
     * Called when a collection is encountered by the object traverser.
     * <p>
     * This can be either when a top-level collection is discovered, or when an object's attribute is a collection.
     *
     * @param ctx Current serialization context
     */
    void openCollection(SerializationContext<? extends Collection<?>> ctx);

    /**
     * Called after the last collection item is processed.
     *
     * @param ctx Current serialization context
     * @see #openCollection(SerializationContext)
     */
    void closeCollection(SerializationContext<?> ctx);
}
