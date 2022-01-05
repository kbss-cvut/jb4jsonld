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

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.verify;

public class NumericLiteralNodeTest extends AbstractNodeTest {

    @Test
    void writeValueWritesTheValueAsNumber() throws Exception {
        final String name = "test";
        final long value = System.currentTimeMillis();
        final JsonNode node = new NumericLiteralNode<>(name, value);
        node.write(serializerMock);
        verify(serializerMock).writeNumber(value);
    }
}
