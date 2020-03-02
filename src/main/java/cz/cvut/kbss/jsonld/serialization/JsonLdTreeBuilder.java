/**
 * Copyright (C) 2017 Czech Technical University in Prague
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

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.CompositeNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.traversal.InstanceVisitor;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
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
    private final FieldSerializer annotationSerializer = new AnnotationFieldSerializer();
    private final FieldSerializer propertiesSerializer = new PropertiesFieldSerializer();

    @Override
    public void openInstance(Object instance) {
        final CompositeNode newCurrent = visitedField != null ? JsonNodeFactory.createObjectNode(attId(visitedField)) :
                                         JsonNodeFactory.createObjectNode();
        openNewNode(newCurrent);
        this.visitedField = null;
    }

    private String attId(Field field) {
        return BeanAnnotationProcessor.getAttributeIdentifier(field);
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

    @Override
    public void closeInstance(Object instance) {
        currentNode.close();
        if (!nodeStack.empty()) {
            this.currentNode = nodeStack.pop();
        }
    }

    @Override
    public void visitIdentifier(String identifier, Object instance) {
        assert currentNode.isOpen();
        currentNode.addItem(JsonNodeFactory.createObjectIdNode(JsonLd.ID, identifier));
    }

    @Override
    public void visitTypes(Collection<String> types, Object instance) {
        final CollectionNode typesNode = JsonNodeFactory.createCollectionNode(JsonLd.TYPE, types);
        types.forEach(type -> typesNode.addItem(JsonNodeFactory.createLiteralNode(type)));
        currentNode.addItem(typesNode);
    }

    @Override
    public void visitKnownInstance(String id, Object instance) {
        if (visitedField != null) {
            openNewNode(JsonNodeFactory.createObjectNode(attId(visitedField)));
        } else {
            openNewNode(JsonNodeFactory.createObjectNode());
        }
        currentNode.addItem(JsonNodeFactory.createLiteralNode(JsonLd.ID, id));
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
            if (BeanAnnotationProcessor.isAnnotationProperty(field)) {
                nodes = annotationSerializer.serializeField(field, value);
            } else if (BeanAnnotationProcessor.isPropertiesField(field)) {
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
