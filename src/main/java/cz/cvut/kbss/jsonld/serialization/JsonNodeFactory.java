/**
 * Copyright (C) 2020 Czech Technical University in Prague
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
import java.util.Date;
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
        BOOLEAN, NUMBER, STRING, TEMPORAL
    }

    public static LiteralNode<?> createLiteralNode(Object value) {
        return createLiteralNode(null, value);
    }

    public static LiteralNode<?> createLiteralNode(String name, Object value) {
        final LiteralType type = determineLiteralType(value);
        LiteralNode<?> node = null;
        switch (type) {
            case BOOLEAN:
                node = name != null ? new BooleanLiteralNode(name, (Boolean) value) : new BooleanLiteralNode(
                        (Boolean) value);
                break;
            case NUMBER:
                node = name != null ? new NumericLiteralNode<>(name, (Number) value) :
                       new NumericLiteralNode<>((Number) value);
                break;
            case STRING:
                node = name != null ? new StringLiteralNode(name, value.toString()) :
                       new StringLiteralNode(value.toString());
                break;
            case TEMPORAL:
                node = TemporalNodeFactory.createLiteralNode(name, value);
                break;
        }
        return node;
    }

    private static LiteralType determineLiteralType(Object value) {
        if (value instanceof Boolean) {
            return LiteralType.BOOLEAN;
        } else if (value instanceof Number) {
            return LiteralType.NUMBER;
        } else if (value instanceof Date) {
            return LiteralType.TEMPORAL;
        }
        return LiteralType.STRING;
    }

    /**
     * Creates a node for representing a single translation of a string value.
     *
     * @param value    String value
     * @param language Language tag for the value
     * @return A node representing the language tagged value
     */
    public static LangStringNode createLangStringNode(String value, String language) {
        return new LangStringNode(value, language);
    }

    /**
     * Creates a node for representing a single translation of a string value.
     * <p>
     * Usually, multiple translations are expected which are put into a collection. But if there is only one translation
     * in the multilingual string, this method may be used to directly serialize the attribute.
     *
     * @param name     Attribute name
     * @param value    String value
     * @param language Language tag for the value
     * @return A node representing the language tagged value
     */
    public static LangStringNode createLangStringNode(String name, String value, String language) {
        return new LangStringNode(name, value, language);
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
    public static CollectionNode createCollectionNode(Collection<?> value) {
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
    public static CollectionNode createCollectionNode(String name, Collection<?> value) {
        final CollectionType type = determineCollectionType(value);
        CollectionNode n = null;
        switch (type) {
            case LIST:
                n = name != null ? new ListNode(name) : new ListNode();
                break;
            case SET:
                n = name != null ? new SetNode(name) : new SetNode();
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

    public static CollectionNode createCollectionNodeFromArray() {
        return new SetNode();
    }

    public static CollectionNode createCollectionNodeFromArray(String name) {
        return new SetNode(name);
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
