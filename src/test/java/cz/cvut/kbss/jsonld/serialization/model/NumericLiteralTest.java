package cz.cvut.kbss.jsonld.serialization.model;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.verify;

public class NumericLiteralTest extends AbstractNodeTest {

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void writeValueWritesTheValueAsNumber() throws Exception {
        final String name = "test";
        final long value = System.currentTimeMillis();
        final JsonNode node = new NumericLiteral<>(name, value);
        node.write(serializerMock);
        verify(serializerMock).writeNumber(value);
    }
}
