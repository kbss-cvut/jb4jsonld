package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.environment.Generator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ObjectNodeTest extends AbstractNodeTest {

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void writeOutputsAllTheChildrenAsKeyValuePairs() throws Exception {
        final ObjectNode node = new ObjectNode();
        final List<JsonNode> children = generateChildren();
        children.forEach(node::addItem);
        node.write(serializerMock);

        final InOrder inOrder = inOrder(serializerMock);
        inOrder.verify(serializerMock).writeObjectStart();
        for (JsonNode n : children) {
            inOrder.verify(serializerMock).writeFieldName(n.getName());
            inOrder.verify(serializerMock).writeNumber((Number) ((NumericLiteralNode) n).getValue());
        }
        inOrder.verify(serializerMock).writeObjectEnd();
    }

    private List<JsonNode> generateChildren() {
        final List<JsonNode> nodes = new ArrayList<>();
        for (int i = 0; i < Generator.randomCount(10); i++) {
            nodes.add(new NumericLiteralNode<>(Generator.URI_BASE + i, i));
        }
        return nodes;
    }

    @Test
    public void writeOutputsEmptyObjectWhenThereAreNoChildren() throws Exception {
        final ObjectNode node = new ObjectNode();
        node.write(serializerMock);

        final InOrder inOrder = inOrder(serializerMock);
        inOrder.verify(serializerMock).writeObjectStart();
        inOrder.verify(serializerMock).writeObjectEnd();
        verify(serializerMock, never()).writeFieldName(anyString());
    }
}
