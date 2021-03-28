/**
 * Copyright (C) 2020 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.CompositeNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.traversal.InstanceVisitor;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.util.Collection;
import java.util.Stack;

/**
 * Builds an abstract representation of a JSON-LD tree, which is a result of object graph traversal by {@link
 * cz.cvut.kbss.jsonld.serialization.traversal.ObjectGraphTraverser}.
 */
public class JsonLdTreeBuilder implements InstanceVisitor {

    private final Stack<CompositeNode> nodeStack = new Stack<>();
    private CompositeNode currentNode;

    private final ValueSerializers serializers;

    public JsonLdTreeBuilder(ValueSerializers serializers) {
        this.serializers = serializers;
    }

    @Override
    public void openObject(SerializationContext<?> ctx) {
        final CompositeNode newCurrent =
                ctx.getAttributeId() != null ? JsonNodeFactory.createObjectNode(ctx.getAttributeId()) :
                        JsonNodeFactory.createObjectNode();
        openNewNode(newCurrent);
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
    public void closeObject(SerializationContext<?> ctx) {
        currentNode.close();
        if (!nodeStack.empty()) {
            this.currentNode = nodeStack.pop();
        }
    }

    @Override
    public void visitIdentifier(SerializationContext<String> idCtx) {
        assert currentNode.isOpen();
        currentNode.addItem(JsonNodeFactory.createObjectIdNode(JsonLd.ID, idCtx.getValue()));
    }

    @Override
    public void visitTypes(SerializationContext<Collection<String>> typesCtx) {
        final CollectionNode typesNode = JsonNodeFactory.createCollectionNode(JsonLd.TYPE, typesCtx.getValue());
        typesCtx.getValue().forEach(type -> typesNode.addItem(JsonNodeFactory.createLiteralNode(type)));
        currentNode.addItem(typesNode);
    }

    @Override
    public void visitAttribute(SerializationContext<?> ctx) {
        if (ctx.getValue() != null) {
            assert currentNode != null;
            final ValueSerializer serializer = serializers.getOrDefault(ctx);
            final JsonNode node = serializer.serialize(ctx.getValue(), ctx);
            if (node != null) {
                currentNode.addItem(node);
            }
        }
    }

    @Override
    public void openCollection(SerializationContext<? extends Collection<?>> ctx) {
        final CollectionNode newCurrent =
                ctx.getAttributeId() != null ? JsonNodeFactory.createCollectionNode(ctx.getAttributeId(),
                        ctx.getValue()) :
                        JsonNodeFactory.createCollectionNode(ctx.getValue());
        openNewNode(newCurrent);
    }

    @Override
    public void closeCollection(SerializationContext<?> ctx) {
        assert currentNode instanceof CollectionNode;
        closeObject(ctx);
    }

    public CompositeNode getTreeRoot() {
        return currentNode;
    }
}
