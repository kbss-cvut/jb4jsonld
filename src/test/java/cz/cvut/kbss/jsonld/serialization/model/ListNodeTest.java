package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.Constants;
import cz.cvut.kbss.jsonld.environment.Generator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.inOrder;

public class ListNodeTest extends AbstractNodeTest {

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void writeOutputsItemsInAnObjectWithSingleAttributeOfNameListAndValueAnArray() throws Exception {
        final CollectionNode node = new ListNode("http://krizik.felk.cvut.cz/ontologies/jsonld#list");
        final List<JsonNode> items = generateItems();
        items.forEach(node::addItem);
        node.write(serializerMock);

        final InOrder inOrder = inOrder(serializerMock);
        inOrder.verify(serializerMock).writeFieldName(node.getName());
        inOrder.verify(serializerMock).writeObjectStart();
        inOrder.verify(serializerMock).writeFieldName(Constants.JSON_LD_LIST);
        inOrder.verify(serializerMock).writeArrayStart();
        for (JsonNode item : items) {
            inOrder.verify(serializerMock).writeNumber((Number) ((NumericLiteral) item).getValue());
        }
        inOrder.verify(serializerMock).writeArrayEnd();
        inOrder.verify(serializerMock).writeObjectEnd();
    }

    private List<JsonNode> generateItems() {
        final List<JsonNode> nodes = new ArrayList<>();
        for (int i = 0; i < Generator.randomCount(10); i++) {
            nodes.add(new NumericLiteral<>(Generator.randomCount(Integer.MAX_VALUE)));
        }
        return nodes;
    }

    @Test
    public void writeOutputsObjectWithSingleAttributeOfNameListAndValueAnEmptyArray() throws Exception {
        final CollectionNode node = new ListNode();
        node.write(serializerMock);

        final InOrder inOrder = inOrder(serializerMock);
        inOrder.verify(serializerMock).writeObjectStart();
        inOrder.verify(serializerMock).writeFieldName(Constants.JSON_LD_LIST);
        inOrder.verify(serializerMock).writeArrayStart();
        inOrder.verify(serializerMock).writeArrayEnd();
        inOrder.verify(serializerMock).writeObjectEnd();
    }
}
