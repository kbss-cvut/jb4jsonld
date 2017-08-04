/**
 * Copyright (C) 2017 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.serialization.model.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class JsonNodeFactoryTest {

    private static final String NAME = "http://krizik.felk.cvut.cz/ontologies/jsonld#attribute";

    @Test
    public void createCollectionCreatesListNodeFromListWithoutName() {
        final List<String> list = new ArrayList<>();
        final CollectionNode node = JsonNodeFactory.createCollectionNode(list);
        assertTrue(node instanceof ListNode);
        assertTrue(node.isValueNode());
    }

    @Test
    public void createCollectionCreatesListNodeFromListWithName() {
        final List<String> list = new ArrayList<>();
        final CollectionNode node = JsonNodeFactory.createCollectionNode(NAME, list);
        assertTrue(node instanceof ListNode);
        assertEquals(NAME, node.getName());
        assertFalse(node.isValueNode());
    }

    @Test
    public void createCollectionCreatesSetNodeFromSetWithoutName() {
        final Set<String> set = new HashSet<>();
        final CollectionNode node = JsonNodeFactory.createCollectionNode(set);
        assertTrue(node instanceof SetNode);
        assertTrue(node.isValueNode());
    }

    @Test
    public void createCollectionCreatesSetNodeFromSetWithName() {
        final Set<String> set = new HashSet<>();
        final CollectionNode node = JsonNodeFactory.createCollectionNode(NAME, set);
        assertTrue(node instanceof SetNode);
        assertEquals(NAME, node.getName());
        assertFalse(node.isValueNode());
    }

    @Test
    public void createCollectionFromArrayCreatesSetNodeWithName() {
        final CollectionNode node = JsonNodeFactory.createCollectionNodeFromArray(NAME);
        assertTrue(node instanceof SetNode);
        assertEquals(NAME, node.getName());
        assertFalse(node.isValueNode());
    }

    @Test
    public void createCollectionFromArrayCreatesSetNodeWithoutName() {
        final CollectionNode node = JsonNodeFactory.createCollectionNodeFromArray();
        assertTrue(node instanceof SetNode);
        assertNull(node.getName());
        assertTrue(node.isValueNode());
    }

    @Test
    public void createLiteralNodeCreatesBooleanNodeFromBooleanValueWithName() {
        final LiteralNode node = JsonNodeFactory.createLiteralNode(NAME, false);
        assertTrue(node instanceof BooleanLiteralNode);
        assertFalse(node.isValueNode());
        assertEquals(NAME, node.getName());
        assertFalse((Boolean) node.getValue());
    }

    @Test
    public void createLiteralNodeCreatesBooleanNodeFromBooleanValueWithoutName() {
        final LiteralNode node = JsonNodeFactory.createLiteralNode(true);
        assertTrue(node instanceof BooleanLiteralNode);
        assertTrue(node.isValueNode());
        assertTrue((Boolean) node.getValue());
    }

    @Test
    public void createLiteralNodeCreatesNumericNodeFromNumberValueWithName() {
        final long value = System.currentTimeMillis();
        final LiteralNode node = JsonNodeFactory.createLiteralNode(NAME, value);
        assertTrue(node instanceof NumericLiteralNode);
        assertFalse(node.isValueNode());
        assertEquals(value, node.getValue());
    }

    @Test
    public void createLiteralNodeCreatesNumericNodeFromNumberValueWithoutName() {
        final double value = Double.MIN_VALUE;
        final LiteralNode node = JsonNodeFactory.createLiteralNode(value);
        assertTrue(node instanceof NumericLiteralNode);
        assertTrue(node.isValueNode());
        assertEquals(value, node.getValue());
    }

    @Test
    public void createLiteralNodeCreatesStringNodeFromStringWithName() {
        final String value = "test";
        final LiteralNode node = JsonNodeFactory.createLiteralNode(NAME, value);
        assertTrue(node instanceof StringLiteralNode);
        assertFalse(node.isValueNode());
        assertEquals(NAME, node.getName());
        assertEquals(value, node.getValue());
    }

    @Test
    public void createLiteralNodeCreatesStringNodeFromStringWithoutName() {
        final String value = "test2";
        final LiteralNode node = JsonNodeFactory.createLiteralNode(value);
        assertTrue(node instanceof StringLiteralNode);
        assertTrue(node.isValueNode());
        assertEquals(value, node.getValue());
    }
}
