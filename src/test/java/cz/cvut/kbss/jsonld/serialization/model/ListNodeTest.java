/*
 * JB4JSON-LD
 * Copyright (C) 2024 Czech Technical University in Prague
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

import static org.mockito.Mockito.inOrder;

public class ListNodeTest extends AbstractNodeTest {

    @Test
    void writeOutputsItemsInAnObjectWithSingleAttributeOfNameListAndValueAnArray() throws Exception {
        final ListNode node = new ListNode("http://krizik.felk.cvut.cz/ontologies/jsonld#list");
        final List<JsonNode> items = generateItems();
        items.forEach(node::addItem);
        node.write(serializerMock);

        final InOrder inOrder = inOrder(serializerMock);
        inOrder.verify(serializerMock).writeFieldName(node.getName());
        inOrder.verify(serializerMock).writeObjectStart();
        inOrder.verify(serializerMock).writeFieldName(JsonLd.LIST);
        inOrder.verify(serializerMock).writeArrayStart();
        for (JsonNode item : items) {
            inOrder.verify(serializerMock).writeNumber(((NumericLiteralNode<?>) item).getValue());
        }
        inOrder.verify(serializerMock).writeArrayEnd();
        inOrder.verify(serializerMock).writeObjectEnd();
    }

    private List<JsonNode> generateItems() {
        final List<JsonNode> nodes = new ArrayList<>();
        for (int i = 0; i < Generator.randomInt(10); i++) {
            nodes.add(new NumericLiteralNode<>(Generator.randomInt(Integer.MAX_VALUE)));
        }
        return nodes;
    }

    @Test
    void writeOutputsObjectWithSingleAttributeOfNameListAndValueAnEmptyArray() throws Exception {
        final ListNode node = new ListNode();
        node.write(serializerMock);

        final InOrder inOrder = inOrder(serializerMock);
        inOrder.verify(serializerMock).writeObjectStart();
        inOrder.verify(serializerMock).writeFieldName(JsonLd.LIST);
        inOrder.verify(serializerMock).writeArrayStart();
        inOrder.verify(serializerMock).writeArrayEnd();
        inOrder.verify(serializerMock).writeObjectEnd();
    }
}
