package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.environment.Generator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class SetNodeTest extends AbstractNodeTest {

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void writeOutputsItemsAsSimpleJsonArray() throws Exception {
        final CollectionNode node = new SetNode();
        final List<JsonNode> items = generateItems();
        items.forEach(node::addItem);
        node.write(serializerMock);

        final InOrder inOrder = inOrder(serializerMock);
        inOrder.verify(serializerMock).writeArrayStart();
        for (JsonNode item : items) {
            inOrder.verify(serializerMock).writeString(((StringLiteral) item).getValue());
        }
        inOrder.verify(serializerMock).writeArrayEnd();
        verify(serializerMock, never()).writeFieldName(anyString());
    }

    private List<JsonNode> generateItems() {
        final List<JsonNode> nodes = new ArrayList<>();
        for (int i = 0; i < Generator.randomCount(10); i++) {
            nodes.add(new StringLiteral("item" + i));
        }
        return nodes;
    }

    @Test
    public void writeOutputsEmptyArrayWhenThereAreNoItems() throws Exception {
        final CollectionNode node = new SetNode();
        node.write(serializerMock);

        final InOrder inOrder = inOrder(serializerMock);
        inOrder.verify(serializerMock).writeArrayStart();
        inOrder.verify(serializerMock).writeArrayEnd();
    }
}
