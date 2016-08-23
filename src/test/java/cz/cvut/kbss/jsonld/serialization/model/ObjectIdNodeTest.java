/**
 * Copyright (C) 2016 Czech Technical University in Prague
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
