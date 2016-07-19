package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.exception.JsonLdSerializationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class JsonNodeTest extends AbstractNodeTest {

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void writeWritesFirstKeyAndThenValueOfTheNode() throws Exception {
        final String name = "test";
        final String value = "testValue";
        final JsonNode node = spy(new StringLiteral(name, value));
        node.write(serializerMock);
        final InOrder inOrder = inOrder(node);

        inOrder.verify(node).writeKey(serializerMock);
        inOrder.verify(node).writeValue(serializerMock);
    }

    @Test(expected = JsonLdSerializationException.class)
    public void ioExceptionInJsonSerializationThrowsJsonLdSerializationException() throws Exception {
        final String name = "test";
        final String value = "testValue";
        final JsonNode node = spy(new StringLiteral(name, value));
        doThrow(new IOException()).when(node).writeValue(serializerMock);
        node.write(serializerMock);
    }

    @Test
    public void writeDoesNotWriteKeyWhenNodeIsNotAttribute() throws Exception {
        final String value = "testValue";
        final JsonNode node = spy(new StringLiteral(value));
        node.write(serializerMock);
        verify(node).writeValue(serializerMock);
        verify(node, never()).writeKey(serializerMock);
        verify(serializerMock, never()).writeFieldName(anyString());
    }
}
