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
package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.inOrder;

public class ListNodeTest extends AbstractNodeTest {

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    void writeOutputsItemsInAnObjectWithSingleAttributeOfNameListAndValueAnArray() throws Exception {
        final CollectionNode node = new ListNode("http://krizik.felk.cvut.cz/ontologies/jsonld#list");
        final List<JsonNode> items = generateItems();
        items.forEach(node::addItem);
        node.write(serializerMock);

        final InOrder inOrder = inOrder(serializerMock);
        inOrder.verify(serializerMock).writeFieldName(node.getName());
        inOrder.verify(serializerMock).writeObjectStart();
        inOrder.verify(serializerMock).writeFieldName(JsonLd.LIST);
        inOrder.verify(serializerMock).writeArrayStart();
        for (JsonNode item : items) {
            inOrder.verify(serializerMock).writeNumber((Number) ((NumericLiteralNode) item).getValue());
        }
        inOrder.verify(serializerMock).writeArrayEnd();
        inOrder.verify(serializerMock).writeObjectEnd();
    }

    private List<JsonNode> generateItems() {
        final List<JsonNode> nodes = new ArrayList<>();
        for (int i = 0; i < Generator.randomCount(10); i++) {
            nodes.add(new NumericLiteralNode<>(Generator.randomCount(Integer.MAX_VALUE)));
        }
        return nodes;
    }

    @Test
    void writeOutputsObjectWithSingleAttributeOfNameListAndValueAnEmptyArray() throws Exception {
        final CollectionNode node = new ListNode();
        node.write(serializerMock);

        final InOrder inOrder = inOrder(serializerMock);
        inOrder.verify(serializerMock).writeObjectStart();
        inOrder.verify(serializerMock).writeFieldName(JsonLd.LIST);
        inOrder.verify(serializerMock).writeArrayStart();
        inOrder.verify(serializerMock).writeArrayEnd();
        inOrder.verify(serializerMock).writeObjectEnd();
    }
}
