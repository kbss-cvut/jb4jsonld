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

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.CompositeNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.traversal.InstanceVisitor;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Builds an abstract representation of a JSON-LD tree, which is a result of object graph traversal by {@link
 * cz.cvut.kbss.jsonld.serialization.traversal.ObjectGraphTraverser}.
 */
public class JsonLdTreeBuilder implements InstanceVisitor {

    private final Stack<CompositeNode> nodeStack = new Stack<>();
    private CompositeNode currentNode;
    private Field visitedField;

    private final FieldSerializer literalSerializer = new LiteralFieldSerializer();
    private final FieldSerializer propertiesSerializer = new PropertiesFieldSerializer();
    private final InstanceTypeResolver typeResolver = new InstanceTypeResolver();

    @Override
    public void openInstance(Object instance) {
        final CompositeNode newCurrent = visitedField != null ? JsonNodeFactory.createObjectNode(attId(visitedField)) :
                JsonNodeFactory.createObjectNode();
        openNewNode(newCurrent);
        if (BeanClassProcessor.isIdentifierType(instance.getClass())) {
            currentNode.addItem(JsonNodeFactory.createObjectIdNode(JsonLd.ID, instance));
        } else {
            addTypes(instance);
        }
        this.visitedField = null;
    }

    private void openNewNode(CompositeNode newNode) {
        if (currentNode != null) {
            if (currentNode.isOpen()) {
                nodeStack.push(currentNode);
            }
            currentNode.addItem(newNode);
        }
        this.currentNode = newNode;
    }

    private String attId(Field field) {
        return BeanAnnotationProcessor.getAttributeIdentifier(field);
    }

    private void addTypes(Object instance) {
        final Set<String> types = typeResolver.resolveTypes(instance);
        assert !types.isEmpty();
        final CollectionNode typesNode = JsonNodeFactory.createCollectionNode(JsonLd.TYPE, types);
        types.forEach(type -> typesNode.addItem(JsonNodeFactory.createLiteralNode(type)));
        currentNode.addItem(typesNode);
    }

    @Override
    public void closeInstance(Object instance) {
        currentNode.close();
        if (!nodeStack.empty()) {
            this.currentNode = nodeStack.pop();
        }
    }

    @Override
    public void visitKnownInstance(String id, Object instance) {
        if (visitedField != null) {
            openNewNode(JsonNodeFactory.createObjectNode(attId(visitedField)));
        } else {
            openNewNode(JsonNodeFactory.createObjectNode());
        }
        currentNode.addItem(
                JsonNodeFactory.createLiteralNode(JsonLd.ID, id));
        closeInstance(instance);
        this.visitedField = null;
    }

    @Override
    public void visitField(Field field, Object value) {
        if (value == null || BeanAnnotationProcessor.isTypesField(field)) {
            return;
        }
        if (BeanAnnotationProcessor.isObjectProperty(field)) {
            this.visitedField = field;
        } else {
            assert currentNode != null;
            final List<JsonNode> nodes;
            if (BeanAnnotationProcessor.isPropertiesField(field)) {
                // A problem could be when the properties contain a property mapped by the model as well
                nodes = propertiesSerializer.serializeField(field, value);
            } else {
                nodes = literalSerializer.serializeField(field, value);
            }
            nodes.forEach(node -> currentNode.addItem(node));
        }
    }

    @Override
    public void openCollection(Collection<?> collection) {
        final CollectionNode newCurrent =
                visitedField != null ? JsonNodeFactory.createCollectionNode(attId(visitedField), collection) :
                        JsonNodeFactory.createCollectionNode(collection);
        openNewNode(newCurrent);
        this.visitedField = null;
    }

    @Override
    public void closeCollection(Collection<?> collection) {
        assert currentNode instanceof CollectionNode;
        closeInstance(collection);
    }

    public CompositeNode getTreeRoot() {
        return currentNode;
    }
}
