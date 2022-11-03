/**
 * Copyright (C) 2022 Czech Technical University in Prague
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

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.util.Collection;

class DefaultValueSerializer implements ValueSerializer {

    private final MultilingualStringSerializer multilingualStringSerializer;

    DefaultValueSerializer(MultilingualStringSerializer multilingualStringSerializer) {
        this.multilingualStringSerializer = multilingualStringSerializer;
    }

    @Override
    public JsonNode serialize(Object value, SerializationContext ctx) {
        final boolean annotationProperty = BeanAnnotationProcessor.isAnnotationProperty(ctx.getField());
        if (value instanceof Collection) {
            final Collection<?> col = (Collection<?>) value;
            final CollectionNode<?> node = JsonNodeFactory.createCollectionNode(ctx.getAttributeId(), col);
            col.forEach(item -> {
                if (annotationProperty && isReference(item)) {
                    node.addItem(serializeReference(null, item));
                } else if (item instanceof MultilingualString) {
                    node.addItem(multilingualStringSerializer.serialize((MultilingualString) item));
                } else {
                    node.addItem(JsonNodeFactory.createLiteralNode(item));
                }
            });
            return node;
        } else {
            if (annotationProperty && isReference(value)) {
                return serializeReference(ctx.getAttributeId(), value);
            } else if (value instanceof MultilingualString) {
                return multilingualStringSerializer.serialize(ctx.getAttributeId(), (MultilingualString) value);
            }
            return JsonNodeFactory.createLiteralNode(ctx.getAttributeId(), value);
        }
    }

    private boolean isReference(Object value) {
        return BeanClassProcessor.isIdentifierType(value.getClass()) && !(value instanceof String);
    }

    private JsonNode serializeReference(String attId, Object value) {
        final ObjectNode node =
                attId != null ? JsonNodeFactory.createObjectNode(attId) : JsonNodeFactory.createObjectNode();
        node.addItem(JsonNodeFactory.createObjectIdNode(JsonLd.ID, value));
        return node;
    }
}
