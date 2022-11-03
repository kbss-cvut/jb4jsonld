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
package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.environment.Generator;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class SetNodeTest extends AbstractNodeTest {

    @Test
    void writeOutputsItemsAsSimpleJsonArray() throws Exception {
        final SetNode node = new SetNode();
        final List<JsonNode> items = generateItems();
        items.forEach(node::addItem);
        node.write(serializerMock);

        final InOrder inOrder = inOrder(serializerMock);
        inOrder.verify(serializerMock).writeArrayStart();
        for (JsonNode item : items) {
            inOrder.verify(serializerMock).writeString(((StringLiteralNode) item).getValue());
        }
        inOrder.verify(serializerMock).writeArrayEnd();
        verify(serializerMock, never()).writeFieldName(anyString());
    }

    private List<JsonNode> generateItems() {
        final List<JsonNode> nodes = new ArrayList<>();
        for (int i = 0; i < Generator.randomCount(10); i++) {
            nodes.add(new StringLiteralNode("item" + i));
        }
        return nodes;
    }

    @Test
    void writeOutputsEmptyArrayWhenThereAreNoItems() throws Exception {
        final SetNode node = new SetNode();
        node.write(serializerMock);

        final InOrder inOrder = inOrder(serializerMock);
        inOrder.verify(serializerMock).writeArrayStart();
        inOrder.verify(serializerMock).writeArrayEnd();
    }
}
