/*
 * JB4JSON-LD
 * Copyright (C) 2023 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class ObjectNodeTest extends AbstractNodeTest {

    @Test
    void writeOutputsAllTheChildrenAsKeyValuePairs() throws Exception {
        final ObjectNode node = new ObjectNode();
        final List<JsonNode> children = generateChildren();
        children.forEach(node::addItem);
        node.write(serializerMock);

        final InOrder inOrder = inOrder(serializerMock);
        inOrder.verify(serializerMock).writeObjectStart();
        for (JsonNode n : children) {
            inOrder.verify(serializerMock).writeFieldName(n.getName());
            inOrder.verify(serializerMock).writeNumber(((NumericLiteralNode<?>) n).getValue());
        }
        inOrder.verify(serializerMock).writeObjectEnd();
    }

    private List<JsonNode> generateChildren() {
        final List<JsonNode> nodes = new ArrayList<>();
        for (int i = 0; i < Generator.randomInt(10); i++) {
            nodes.add(new NumericLiteralNode<>(Generator.URI_BASE + i, i));
        }
        return nodes;
    }

    @Test
    void writeOutputsEmptyObjectWhenThereAreNoChildren() throws Exception {
        final ObjectNode node = new ObjectNode();
        node.write(serializerMock);

        final InOrder inOrder = inOrder(serializerMock);
        inOrder.verify(serializerMock).writeObjectStart();
        inOrder.verify(serializerMock).writeObjectEnd();
        verify(serializerMock, never()).writeFieldName(anyString());
    }

    @Test
    void prependItemAddsSpecifiedItemToBeginningOfItemsCollection() throws Exception {
        final ObjectNode node = new ObjectNode();
        final List<JsonNode> children = generateChildren();
        children.forEach(node::addItem);
        node.write(serializerMock);
        final StringLiteralNode
                toPrepend = new StringLiteralNode(JsonLd.CONTEXT, "Context value " + Generator.randomInt(1000));
        node.prependItem(toPrepend);

        node.write(serializerMock);
        final InOrder inOrder = inOrder(serializerMock);
        inOrder.verify(serializerMock).writeObjectStart();
        inOrder.verify(serializerMock).writeFieldName(toPrepend.getName());
        inOrder.verify(serializerMock).writeString(toPrepend.getValue());
        for (JsonNode n : children) {
            inOrder.verify(serializerMock).writeFieldName(n.getName());
            inOrder.verify(serializerMock).writeNumber(((NumericLiteralNode<?>) n).getValue());
        }
        inOrder.verify(serializerMock).writeObjectEnd();
    }
}
