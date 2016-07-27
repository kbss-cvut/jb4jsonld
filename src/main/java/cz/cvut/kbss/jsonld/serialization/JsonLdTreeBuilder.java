package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.Constants;
import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.CompositeNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.traversal.InstanceVisitor;

import java.lang.reflect.Field;
import java.util.Collection;
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

    @Override
    public void openInstance(Object instance) {
        final CompositeNode newCurrent = visitedField != null ? JsonNodeFactory.createObjectNode(attId(visitedField)) :
                                         JsonNodeFactory.createObjectNode();
        openNewNode(newCurrent);
        addTypes(instance);
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
        final Set<String> types = BeanAnnotationProcessor.getOwlClasses(instance);
        final CollectionNode typesNode = JsonNodeFactory.createCollectionNode(Constants.JSON_LD_TYPE, types);
        for (String type : types) {
            typesNode.addItem(JsonNodeFactory.createLiteralNode(type));
        }
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
    public void visitKnownInstance(Object instance) {
        if (visitedField != null) {
            currentNode.addItem(JsonNodeFactory
                    .createObjectIdNode(attId(visitedField), BeanAnnotationProcessor.getInstanceIdentifier(instance)));
        } else {
            currentNode.addItem(JsonNodeFactory.createObjectIdNode(instance));
        }
        this.visitedField = null;
    }

    @Override
    public void visitField(Field field, Object value) {
        if (value == null) {
            return;
        }
        if (BeanAnnotationProcessor.isObjectProperty(field)) {
            this.visitedField = field;
        } else {
            assert currentNode != null;
            final String attName = attId(field);
            final JsonNode attNode = createLiteralAttribute(attName, value);
            currentNode.addItem(attNode);
        }
    }

    private JsonNode createLiteralAttribute(String attName, Object value) {
        final JsonNode result;
        if (value instanceof Collection) {
            final Collection<?> col = (Collection<?>) value;
            final CollectionNode node = JsonNodeFactory.createCollectionNode(attName, col);
            col.forEach(obj -> node.addItem(JsonNodeFactory.createLiteralNode(obj)));
            result = node;
        } else {
            result = JsonNodeFactory.createLiteralNode(attName, value);
        }
        return result;
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
