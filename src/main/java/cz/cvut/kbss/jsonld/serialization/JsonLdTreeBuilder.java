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
package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.CompositeNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.traversal.InstanceVisitor;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

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

    private final ValueSerializer literalSerializer;
    private final ValueSerializer annotationSerializer;

    public JsonLdTreeBuilder() {
        final MultilingualStringSerializer msSerializer = new MultilingualStringSerializer();
        this.literalSerializer = new LiteralValueSerializer(msSerializer);
        this.annotationSerializer = new AnnotationValueSerializer(msSerializer);
    }

    @Override
    public void openObject(SerializationContext<?> ctx) {
        final CompositeNode newCurrent = ctx.attributeId != null ? JsonNodeFactory.createObjectNode(ctx.attributeId) :
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
        currentNode.addItem(JsonNodeFactory.createObjectIdNode(JsonLd.ID, idCtx.value));
    }

    @Override
    public void visitTypes(SerializationContext<Collection<String>> typesCtx) {
        final CollectionNode typesNode = JsonNodeFactory.createCollectionNode(JsonLd.TYPE, typesCtx.value);
        typesCtx.value.forEach(type -> typesNode.addItem(JsonNodeFactory.createLiteralNode(type)));
        currentNode.addItem(typesNode);
    }

    @Override
    public void visitAttribute(SerializationContext<?> ctx) {
        if (ctx.value != null && !BeanAnnotationProcessor.isObjectProperty(ctx.field)) {
            assert currentNode != null;
            final List<JsonNode> nodes;
            if (BeanAnnotationProcessor.isAnnotationProperty(ctx.field)) {
                nodes = annotationSerializer.serialize(ctx.attributeId, ctx.value);
            } else {
                nodes = literalSerializer.serialize(ctx.attributeId, ctx.value);
            }
            nodes.forEach(node -> currentNode.addItem(node));
        }
    }

    @Override
    public void openCollection(SerializationContext<? extends Collection<?>> ctx) {
        final CollectionNode newCurrent =
                ctx.attributeId != null ? JsonNodeFactory.createCollectionNode(ctx.attributeId, ctx.value) :
                        JsonNodeFactory.createCollectionNode(ctx.value);
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
