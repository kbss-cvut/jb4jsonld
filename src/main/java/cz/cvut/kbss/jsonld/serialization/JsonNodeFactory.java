/**
 * Copyright (C) 2022 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.common.CollectionType;
import cz.cvut.kbss.jsonld.serialization.model.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Factory for constructing {@link JsonNode} instances.
 */
public class JsonNodeFactory {

    private JsonNodeFactory() {
        throw new AssertionError();
    }

    private enum LiteralType {
        BOOLEAN, NUMBER, STRING
    }

    public static LiteralNode<?> createLiteralNode(Object value) {
        return createLiteralNode(null, value);
    }

    public static LiteralNode<?> createLiteralNode(String name, Object value) {
        final LiteralType type = determineLiteralType(value);
        switch (type) {
            case BOOLEAN:
                return createBooleanLiteralNode(name, (Boolean) value);
            case NUMBER:
                return createNumericLiteralNode(name, (Number) value);
            default:
                return createStringLiteralNode(name, value.toString());
        }
    }

    private static LiteralType determineLiteralType(Object value) {
        if (value instanceof Boolean) {
            return LiteralType.BOOLEAN;
        } else if (value instanceof Number) {
            return LiteralType.NUMBER;
        }
        return LiteralType.STRING;
    }

    public static BooleanLiteralNode createBooleanLiteralNode(String name, Boolean value) {
        return name != null ? new BooleanLiteralNode(name, value) : new BooleanLiteralNode(value);
    }

    public static NumericLiteralNode<Number> createNumericLiteralNode(String name, Number value) {
        return name != null ? new NumericLiteralNode<>(name, value) : new NumericLiteralNode<>(value);
    }

    public static StringLiteralNode createStringLiteralNode(String name, String value) {
        return name != null ? new StringLiteralNode(name, value) : new StringLiteralNode(value);
    }

    /**
     * Creates collection node for the specified collection.
     * <p>
     * The node is without name, so it cannot be used as attribute.
     *
     * @param value The collection. It is used only to determine the type of the target node, no values are added to the
     *              result
     * @return An empty collection node
     */
    public static CollectionNode<?> createCollectionNode(Collection<?> value) {
        return createCollectionNode(null, value);
    }

    /**
     * Creates collection node with the specified name, for the specified collection.
     *
     * @param name  Name of the node (attribute)
     * @param value The collection. It is used only to determine the type of the target node, no values are added to the
     *              result
     * @return An empty collection node
     */
    public static CollectionNode<?> createCollectionNode(String name, Collection<?> value) {
        final CollectionType type = determineCollectionType(value);
        CollectionNode<?> n = null;
        switch (type) {
            case LIST:
                n = name != null ? new ListNode(name) : new ListNode();
                break;
            case SET:
                n = name != null ? createSetNode(name) : createSetNode();
                break;
        }
        return n;
    }

    private static CollectionType determineCollectionType(Collection<?> collection) {
        if (collection instanceof List) {
            return CollectionType.LIST;
        } else if (collection instanceof Set) {
            return CollectionType.SET;
        } else {
            throw new IllegalArgumentException("Unsupported collection type " + collection.getClass());
        }
    }

    public static SetNode createSetNode() {
        return new SetNode();
    }

    public static SetNode createSetNode(String name) {
        return new SetNode(name);
    }

    public static SetNode createCollectionNodeFromArray() {
        return createSetNode();
    }

    public static SetNode createCollectionNodeFromArray(String name) {
        return createSetNode(name);
    }

    public static ObjectNode createObjectNode() {
        return new ObjectNode();
    }

    public static ObjectNode createObjectNode(String name) {
        return new ObjectNode(name);
    }

    public static ObjectIdNode createObjectIdNode(Object id) {
        return new ObjectIdNode(id.toString());
    }

    public static ObjectIdNode createObjectIdNode(String name, Object id) {
        return new ObjectIdNode(name, id.toString());
    }
}
