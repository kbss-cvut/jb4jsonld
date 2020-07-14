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
     *
     * @param id       Identifier of the instance
     * @param instance The instance
     */
    void visitKnownInstance(String id, Object instance);

    /**
     * Called when the identifier of an instance (JSON-LD {@code @id} attribute) is encountered.
     * <p>
     * This may be invoked out of order, at the beginning of processing an object.
     *
     * @param identifier Instance identifier
     * @param instance   The serialized instance
     */
    void visitIdentifier(String identifier, Object instance);

    /**
     * Called when the object graph traverser discovers a JSON-LD-serializable field in an instance.
     *
     * @param field The visited field
     * @param value Value of the visited field
     */
    void visitField(Field field, Object value);

    /**
     * Called when the types of an instance (JSON-LD {@code @type} attribute) are serialized.
     *
     * @param types    JSON-LD types of the instance
     * @param instance The serialized instance
     */
    void visitTypes(Collection<String> types, Object instance);

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
