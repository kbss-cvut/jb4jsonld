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
package cz.cvut.kbss.jsonld.serialization.serializer.context;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

/**
 * This is used to serialize {@link MultilingualString} values.
 */
public class ContextBuildingMultilingualStringSerializer implements ValueSerializer<MultilingualString> {

    @Override
    public ObjectNode serialize(MultilingualString value, SerializationContext<MultilingualString> ctx) {
        if (ctx.getTerm() != null) {
            registerTermMapping(ctx);
        }
        final ObjectNode node = JsonNodeFactory.createObjectNode(ctx.getFieldName());
        value.getValue().forEach((lang, text) -> node.addItem(JsonNodeFactory.createStringLiteralNode(lang != null ? lang : JsonLd.NONE, text)));
        return node;
    }

    static void registerTermMapping(SerializationContext<MultilingualString> ctx) {
        final ObjectNode mapping = JsonNodeFactory.createObjectNode(ctx.getFieldName());
        mapping.addItem(JsonNodeFactory.createStringLiteralNode(JsonLd.ID, ctx.getTerm()));
        mapping.addItem(JsonNodeFactory.createStringLiteralNode(JsonLd.CONTAINER, JsonLd.LANGUAGE));
        ctx.registerTermMapping(ctx.getFieldName(), mapping);
    }
}
