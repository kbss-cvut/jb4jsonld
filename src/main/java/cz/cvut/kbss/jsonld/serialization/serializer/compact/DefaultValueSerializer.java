/**
 * Copyright (C) 2022 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.serialization.serializer.compact;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.serializer.SerializerUtils;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.util.Collection;

class DefaultValueSerializer implements ValueSerializer {

    private final MultilingualStringSerializer multilingualStringSerializer;

    DefaultValueSerializer(MultilingualStringSerializer multilingualStringSerializer) {
        this.multilingualStringSerializer = multilingualStringSerializer;
    }

    @Override
    public JsonNode serialize(Object value, SerializationContext ctx) {
        if (value instanceof Collection) {
            final Collection<?> col = (Collection<?>) value;
            final CollectionNode<?> node = JsonNodeFactory.createCollectionNode(ctx.getTerm(), col);
            col.forEach(item -> {
                if (SerializerUtils.isAnnotationReference(item, ctx)) {
                    node.addItem(serializeReference(null, item));
                } else if (item instanceof MultilingualString) {
                    node.addItem(multilingualStringSerializer.serialize((MultilingualString) item,
                                                                        new SerializationContext<>(
                                                                                (MultilingualString) item,
                                                                                ctx.getJsonLdContext())));
                } else {
                    node.addItem(JsonNodeFactory.createLiteralNode(item));
                }
            });
            return node;
        } else {
            if (SerializerUtils.isAnnotationReference(value, ctx)) {
                return serializeReference(ctx.getTerm(), value);
            } else if (value instanceof MultilingualString) {
                return multilingualStringSerializer.serialize((MultilingualString) value, ctx);
            }
            return JsonNodeFactory.createLiteralNode(ctx.getTerm(), value);
        }
    }

    private static JsonNode serializeReference(String attId, Object value) {
        final ObjectNode node =
                attId != null ? JsonNodeFactory.createObjectNode(attId) : JsonNodeFactory.createObjectNode();
        node.addItem(JsonNodeFactory.createObjectIdNode(JsonLd.ID, value));
        return node;
    }
}
