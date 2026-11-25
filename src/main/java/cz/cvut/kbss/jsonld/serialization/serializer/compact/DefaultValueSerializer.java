/*
 * JB4JSON-LD
 * Copyright (C) 2026 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.serialization.serializer.compact;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.exception.MissingIdentifierException;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.serializer.SerializerUtils;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.util.Collection;

public class DefaultValueSerializer implements ValueSerializer {

    private final MultilingualStringSerializer multilingualStringSerializer;

    public DefaultValueSerializer(MultilingualStringSerializer multilingualStringSerializer) {
        this.multilingualStringSerializer = multilingualStringSerializer;
    }

    @Override
    public JsonNode serialize(Object value, SerializationContext ctx) {
        final boolean annotationProperty = BeanAnnotationProcessor.isAnnotationProperty(ctx.getField());
        if (value instanceof Collection<?> col) {
            final CollectionNode<?> node = JsonNodeFactory.createCollectionNode(ctx.getTerm(), col);
            col.forEach(item -> {
                if (annotationProperty) {
                    if (BeanAnnotationProcessor.isMappedType(item.getClass())) {
                        node.addItem(serializeReference(null, BeanAnnotationProcessor.getInstanceIdentifier(item).orElseThrow(() -> MissingIdentifierException.create(item))));
                        return;
                    } else if (SerializerUtils.isAnnotationReference(item, ctx)) {
                        node.addItem(serializeReference(null, item));
                        return;
                    }
                }
                if (item instanceof MultilingualString) {
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
            if (annotationProperty) {
                if (BeanAnnotationProcessor.isMappedType(value.getClass())) {
                    return serializeReference(null, BeanAnnotationProcessor.getInstanceIdentifier(value).orElseThrow(() -> MissingIdentifierException.create(value)));
                } else if (SerializerUtils.isAnnotationReference(value, ctx)) {
                    return serializeReference(ctx.getTerm(), value);
                }
            }
            if (value instanceof MultilingualString) {
                return multilingualStringSerializer.serialize((MultilingualString) value, ctx);
            }
            return JsonNodeFactory.createLiteralNode(ctx.getTerm(), value);
        }
    }

    private static JsonNode serializeReference(String attId, Object value) {
        final ObjectNode node = JsonNodeFactory.createObjectNode(attId);
        node.addItem(JsonNodeFactory.createObjectIdNode(JsonLd.ID, value));
        return node;
    }
}
