/**
 * Copyright (C) 2022 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.serialization.model.*;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JsonNodeFactoryTest {

    private static final String NAME = Generator.generateUri().toString();

    @Test
    void createCollectionCreatesListNodeFromListWithoutName() {
        final List<String> list = new ArrayList<>();
        final CollectionNode<?> node = JsonNodeFactory.createCollectionNode(list);
        assertThat(node, instanceOf(ListNode.class));
        assertTrue(node.isValueNode());
    }

    @Test
    void createCollectionCreatesListNodeFromListWithName() {
        final List<String> list = new ArrayList<>();
        final CollectionNode<?> node = JsonNodeFactory.createCollectionNode(NAME, list);
        assertThat(node, instanceOf(ListNode.class));
        assertEquals(NAME, node.getName());
        assertFalse(node.isValueNode());
    }

    @Test
    void createCollectionCreatesSetNodeFromSetWithoutName() {
        final Set<String> set = new HashSet<>();
        final CollectionNode<?> node = JsonNodeFactory.createCollectionNode(set);
        assertThat(node, instanceOf(SetNode.class));
        assertTrue(node.isValueNode());
    }

    @Test
    void createCollectionCreatesSetNodeFromSetWithName() {
        final Set<String> set = new HashSet<>();
        final CollectionNode<?> node = JsonNodeFactory.createCollectionNode(NAME, set);
        assertThat(node, instanceOf(SetNode.class));
        assertEquals(NAME, node.getName());
        assertFalse(node.isValueNode());
    }

    @Test
    void createCollectionFromArrayCreatesSetNodeWithName() {
        final CollectionNode<?> node = JsonNodeFactory.createCollectionNodeFromArray(NAME);
        assertThat(node, instanceOf(SetNode.class));
        assertEquals(NAME, node.getName());
        assertFalse(node.isValueNode());
    }

    @Test
    void createCollectionFromArrayCreatesSetNodeWithoutName() {
        final CollectionNode<?> node = JsonNodeFactory.createCollectionNodeFromArray();
        assertThat(node, instanceOf(SetNode.class));
        assertNull(node.getName());
        assertTrue(node.isValueNode());
    }

    @Test
    void createLiteralNodeCreatesBooleanNodeFromBooleanValueWithName() {
        final LiteralNode<?> node = JsonNodeFactory.createLiteralNode(NAME, false);
        assertThat(node, instanceOf(BooleanLiteralNode.class));
        assertFalse(node.isValueNode());
        assertEquals(NAME, node.getName());
        assertFalse((Boolean) node.getValue());
    }

    @Test
    void createLiteralNodeCreatesBooleanNodeFromBooleanValueWithoutName() {
        final LiteralNode<?> node = JsonNodeFactory.createLiteralNode(true);
        assertThat(node, instanceOf(BooleanLiteralNode.class));
        assertTrue(node.isValueNode());
        assertTrue((Boolean) node.getValue());
    }

    @Test
    void createLiteralNodeCreatesNumericNodeFromNumberValueWithName() {
        final long value = System.currentTimeMillis();
        final LiteralNode<?> node = JsonNodeFactory.createLiteralNode(NAME, value);
        assertThat(node, instanceOf(NumericLiteralNode.class));
        assertFalse(node.isValueNode());
        assertEquals(value, node.getValue());
    }

    @Test
    void createLiteralNodeCreatesNumericNodeFromNumberValueWithoutName() {
        final double value = Double.MIN_VALUE;
        final LiteralNode<?> node = JsonNodeFactory.createLiteralNode(value);
        assertThat(node, instanceOf(NumericLiteralNode.class));
        assertTrue(node.isValueNode());
        assertEquals(value, node.getValue());
    }

    @Test
    void createLiteralNodeCreatesStringNodeFromStringWithName() {
        final String value = "test";
        final LiteralNode<?> node = JsonNodeFactory.createLiteralNode(NAME, value);
        assertThat(node, instanceOf(StringLiteralNode.class));
        assertFalse(node.isValueNode());
        assertEquals(NAME, node.getName());
        assertEquals(value, node.getValue());
    }

    @Test
    void createLiteralNodeCreatesStringNodeFromStringWithoutName() {
        final String value = "test2";
        final LiteralNode<?> node = JsonNodeFactory.createLiteralNode(value);
        assertThat(node, instanceOf(StringLiteralNode.class));
        assertTrue(node.isValueNode());
        assertEquals(value, node.getValue());
    }

    @Test
    void createObjectIdNodeWithoutAttributeCreatesNamelessObjectIdNode() {
        final URI uri = Generator.generateUri();
        final ObjectIdNode node = JsonNodeFactory.createObjectIdNode(uri);
        assertNull(node.getName());
    }
}
