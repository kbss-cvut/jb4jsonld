package cz.cvut.kbss.jsonld.serialization.model;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Mockito.verify;

public class BooleanLiteralNodeTest extends AbstractNodeTest {

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void writeValueWritesTheValueAsBoolean() throws IOException {
        final String name = "test";
        final boolean value = false;
        final JsonNode node = new BooleanLiteralNode(name, value);
        node.write(serializerMock);
        verify(serializerMock).writeBoolean(value);
    }
}
