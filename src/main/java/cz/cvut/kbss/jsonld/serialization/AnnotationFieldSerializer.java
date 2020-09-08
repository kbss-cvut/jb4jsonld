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

import cz.cvut.kbss.jopa.model.MultilingualString;
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

    private final MultilingualStringSerializer multilingualStringSerializer;

    AnnotationFieldSerializer(
            MultilingualStringSerializer multilingualStringSerializer) {
        this.multilingualStringSerializer = multilingualStringSerializer;
    }

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
                } else if (item instanceof MultilingualString) {
                    node.addItem(multilingualStringSerializer.serialize((MultilingualString) item));
                } else {
                    node.addItem(JsonNodeFactory.createLiteralNode(item));
                }
            });
            return Collections.singletonList(node);
        } else {
            if (isReference(value)) {
                return serializeReference(attName, value);
            } else if (value instanceof MultilingualString) {
                return Collections.singletonList(multilingualStringSerializer.serialize(attName,
                        (MultilingualString) value));
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
