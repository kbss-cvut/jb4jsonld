package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class AnnotationFieldSerializer implements FieldSerializer {

    @Override
    public List<JsonNode> serializeField(Field field, Object value) {
        final String attName = BeanAnnotationProcessor.getAttributeIdentifier(field);
        if (value instanceof Collection) {
            final Collection<?> col = (Collection<?>) value;
            final CollectionNode node = JsonNodeFactory.createCollectionNode(attName, col);
            col.forEach(item -> {
                if (isReference(item)) {
                    final ObjectNode n = JsonNodeFactory.createObjectNode();
                    n.addItem(JsonNodeFactory.createObjectIdNode(JsonLd.ID, item));
                    node.addItem(n);
                } else {
                    node.addItem(JsonNodeFactory.createLiteralNode(item));
                }
            });
            return Collections.singletonList(node);
        } else {
            if (isReference(value)) {
                return serializeReference(attName, value);
            }
            return Collections.singletonList(JsonNodeFactory.createLiteralNode(attName, value));
        }
    }

    private boolean isReference(Object value) {
        return BeanClassProcessor.isIdentifierType(value.getClass()) && !(value instanceof String);
    }

    private List<JsonNode> serializeReference(String attId, Object value) {
        final ObjectNode node = JsonNodeFactory.createObjectNode(attId);
        node.addItem(JsonNodeFactory.createObjectIdNode(JsonLd.ID, value));
        return Collections.singletonList(node);
    }
}
