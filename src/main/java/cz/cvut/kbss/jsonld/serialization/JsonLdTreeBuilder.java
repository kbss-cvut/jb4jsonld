package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.CompositeNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.traversal.InstanceVisitor;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Stack;

public class JsonLdTreeBuilder implements InstanceVisitor {

    private final Stack<CompositeNode> nodeStack = new Stack<>();
    private CompositeNode currentNode;

    @Override
    public void openInstance(Object instance) {
        if (currentNode != null && currentNode.isOpen()) {
            nodeStack.push(currentNode);
        }
        this.currentNode = new ObjectNode();
    }

    @Override
    public void closeInstance(Object instance) {
        currentNode.close();
        if (!nodeStack.empty()) {
            this.currentNode = nodeStack.pop();
        }
    }

    @Override
    public void visitKnownInstance(Field field, Object instance) {

    }

    @Override
    public void visitKnownInstance(Object instance) {

    }

    @Override
    public void visitField(Field field, Object value) {

    }

    @Override
    public void openCollection(Collection<?> collection) {

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
