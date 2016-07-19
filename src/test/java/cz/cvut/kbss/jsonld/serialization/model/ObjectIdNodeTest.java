package cz.cvut.kbss.jsonld.serialization.model;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.net.URI;

import static org.mockito.Mockito.inOrder;

public class ObjectIdNodeTest extends AbstractNodeTest {

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void writeValueOutputsTheValueAsObjectWithIdFieldAndStringValue() throws Exception {
        final String name = "test";
        final URI value = URI.create("http://krizik.felk.cvut.cz/ontologies/test/John117");
        final JsonNode node = new ObjectIdNode(name, value);
        node.write(serializerMock);

        final InOrder inOrder = inOrder(serializerMock);
        inOrder.verify(serializerMock).writeFieldName(name);
        inOrder.verify(serializerMock).writeString(value.toString());
    }
}
