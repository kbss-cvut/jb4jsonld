/*
 * JB4JSON-LD
 * Copyright (C) 2025 Czech Technical University in Prague
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.serialization.context.JsonLdContextFactory;
import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.CompositeNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializers;
import cz.cvut.kbss.jsonld.serialization.traversal.InstanceVisitor;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.util.Collection;
import java.util.Set;
import java.util.Stack;

/**
 * Builds an abstract representation of a JSON-LD tree, which is a result of object graph traversal by {@link
 * cz.cvut.kbss.jsonld.serialization.traversal.ObjectGraphTraverser}.
 */
public class JsonLdTreeBuilder implements InstanceVisitor {

    private final Stack<CompositeNode<?>> nodeStack = new Stack<>();
    private CompositeNode<?> currentNode;

    private final ValueSerializers serializers;
    private final JsonLdContextFactory jsonLdContextFactory;

    public JsonLdTreeBuilder(ValueSerializers serializers, JsonLdContextFactory jsonLdContextFactory) {
        this.serializers = serializers;
        this.jsonLdContextFactory = jsonLdContextFactory;
    }

    @Override
    public void visitIndividual(SerializationContext<?> ctx) {
        final ValueSerializer s = serializers.getIndividualSerializer();
        final JsonNode node = s.serialize(ctx.getValue(), ctx);
        if (currentNode != null) {
            currentNode.addItem(node);
        } else {
            assert node instanceof CompositeNode;
            currentNode = (CompositeNode<?>) node;
        }
    }

    @Override
    public boolean visitObject(SerializationContext<?> ctx) {
        if (serializers.hasCustomSerializer(ctx.getValue().getClass())) {
            final ValueSerializer serializer = serializers.getSerializer(ctx).get();
            final JsonNode node = serializer.serialize(ctx.getValue(), ctx);
            if (node != null) {
                if (currentNode != null) {
                    currentNode.addItem(node);
                } else {
                    assert node instanceof CompositeNode;
                    currentNode = (CompositeNode<?>) node;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public void openObject(SerializationContext<?> ctx) {
        final ObjectNode newCurrent = JsonNodeFactory.createObjectNode(ctx.getTerm());
        openNewNode(newCurrent);
        // Prepare to create new JSON-LD context when an object is open
        ctx.setJsonLdContext(jsonLdContextFactory.createJsonLdContext(ctx.getJsonLdContext()));
    }

    private void openNewNode(CompositeNode<?> newNode) {
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
        if (!ctx.isCurrentEmpty()) {
            currentNode.prependItem(ctx.getContextNode());
        }
        closeCurrentNode();
    }

    private void closeCurrentNode() {
        currentNode.close();
        if (!nodeStack.empty()) {
            this.currentNode = nodeStack.pop();
        }
    }

    @Override
    public void visitIdentifier(SerializationContext<String> idCtx) {
        assert currentNode.isOpen();
        currentNode.addItem(serializers.getIdentifierSerializer().serialize(idCtx.getValue(), idCtx));
    }

    @Override
    public void visitTypes(SerializationContext<Set<String>> typesCtx) {
        currentNode.addItem(serializers.getTypesSerializer().serialize(typesCtx.getValue(), typesCtx));
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
        final CollectionNode<?> newCurrent =JsonNodeFactory.createCollectionNode(ctx.getTerm(), ctx.getValue());
        openNewNode(newCurrent);
    }

    @Override
    public void closeCollection(SerializationContext<?> ctx) {
        assert currentNode instanceof CollectionNode;
        closeCurrentNode();
    }

    public CompositeNode<?> getTreeRoot() {
        return currentNode;
    }
}
