package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class LiteralFieldSerializer implements FieldSerializer {

    @Override
    public List<JsonNode> serializeField(Field field, Object value) {
        final String attName = BeanAnnotationProcessor.getAttributeIdentifier(field);
        final JsonNode result;
        if (value instanceof Collection) {
            final Collection<?> col = (Collection<?>) value;
            final CollectionNode node = JsonNodeFactory.createCollectionNode(attName, col);
            col.forEach(obj -> node.addItem(JsonNodeFactory.createLiteralNode(obj)));
            result = node;
        } else {
            result = JsonNodeFactory.createLiteralNode(attName, value);
        }
        return Collections.singletonList(result);
    }
}
