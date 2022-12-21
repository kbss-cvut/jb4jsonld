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
package cz.cvut.kbss.jsonld.serialization.traversal;

import java.util.Collection;
import java.util.Set;

public interface InstanceVisitor {

    /**
     * Visits the instance represented by the specified context.
     * <p>
     * Called before a new instance is open by the object graph traverser.
     *
     * @param ctx Current serialization context
     * @return Whether the object should be processed or not
     * @see #openObject(SerializationContext)
     */
    boolean visitObject(SerializationContext<?> ctx);

    /**
     * Called when a new instance is discovered by the object graph traverser.
     * <p>
     * The instance attributes will be processed immediately after this method returns.
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
     * are processed separately and are not visited as attributes. Also, when processing {@link cz.cvut.kbss.jopa.model.annotations.Properties},
     * this method is invoked for each property in the map.
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
    void visitTypes(SerializationContext<Set<String>> ctx);

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
