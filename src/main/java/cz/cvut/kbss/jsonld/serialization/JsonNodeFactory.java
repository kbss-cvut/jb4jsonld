/*
 * JB4JSON-LD
 * Copyright (C) 2025 Czech Technical University in Prague
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.common.CollectionType;
import cz.cvut.kbss.jsonld.serialization.model.*;
import cz.cvut.kbss.jsonld.serialization.serializer.SerializerUtils;

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

    public static JsonNode createLiteralNode(Object value) {
        return createLiteralNode(null, value);
    }

    public static JsonNode createLiteralNode(String name, Object value) {
        final LiteralType type = determineLiteralType(value);
        return switch (type) {
            case BOOLEAN -> createBooleanLiteralNode(name, (Boolean) value);
            case NUMBER -> createNumericLiteralNode(name, (Number) value);
            default -> createStringLiteralNode(name, value.toString());
        };
    }

    private static LiteralType determineLiteralType(Object value) {
        if (value instanceof Boolean) {
            return LiteralType.BOOLEAN;
        } else if (value instanceof Number) {
            return LiteralType.NUMBER;
        }
        return LiteralType.STRING;
    }

    public static JsonNode createBooleanLiteralNode(String name, Boolean value) {
        return SerializerUtils.createdTypedValueNode(name, value.toString(), XSD.BOOLEAN);
    }

    public static NumericLiteralNode<Number> createNumericLiteralNode(String name, Number value) {
        return name != null ? new NumericLiteralNode<>(name, value) : new NumericLiteralNode<>(value);
    }

    public static StringLiteralNode createStringLiteralNode(String name, String value) {
        return name != null ? new StringLiteralNode(name, value) : new StringLiteralNode(value);
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
        return switch (type) {
            case LIST -> new ListNode(name);
            case SET -> createSetNode(name);
        };
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

    public static SetNode createSetNode(String name) {
        return new SetNode(name);
    }

    public static SetNode createCollectionNodeFromArray(String name) {
        return createSetNode(name);
    }

    public static SetNode createArrayNode() {
        return new SetNode();
    }

    public static ObjectNode createObjectNode() {
        return new ObjectNode();
    }

    public static ObjectNode createObjectNode(String name) {
        return new ObjectNode(name);
    }

    public static ObjectIdNode createObjectIdNode(String name, Object id) {
        return new ObjectIdNode(name, id.toString());
    }
}
