/**
 * Copyright (C) 2020 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.exception.JsonLdSerializationException;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class JsonNodeTest extends AbstractNodeTest {

    @Test
    void writeWritesFirstKeyAndThenValueOfTheNode() throws Exception {
        final String name = "test";
        final String value = "testValue";
        final JsonNode node = spy(new StringLiteralNode(name, value));
        node.write(serializerMock);
        final InOrder inOrder = inOrder(node);

        inOrder.verify(node).writeKey(serializerMock);
        inOrder.verify(node).writeValue(serializerMock);
    }

    @Test
    void ioExceptionInJsonSerializationThrowsJsonLdSerializationException() throws Exception {
        final String name = "test";
        final String value = "testValue";
        final JsonNode node = spy(new StringLiteralNode(name, value));
        doThrow(new IOException()).when(node).writeValue(serializerMock);
        assertThrows(JsonLdSerializationException.class, () -> node.write(serializerMock));
    }

    @Test
    void writeDoesNotWriteKeyWhenNodeIsNotAttribute() throws Exception {
        final String value = "testValue";
        final JsonNode node = spy(new StringLiteralNode(value));
        node.write(serializerMock);
        verify(node).writeValue(serializerMock);
        verify(node, never()).writeKey(serializerMock);
        verify(serializerMock, never()).writeFieldName(anyString());
    }
}
