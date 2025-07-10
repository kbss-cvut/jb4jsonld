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
package cz.cvut.kbss.jsonld.serialization.serializer.compact;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.model.SetNode;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.util.Map;

/**
 * This is used to serialize {@link MultilingualString} values.
 */
public class MultilingualStringSerializer implements ValueSerializer<MultilingualString> {

    @Override
    public JsonNode serialize(MultilingualString value, SerializationContext<MultilingualString> ctx) {
        if (value.getValue().size() == 1) {
            final Map.Entry<String, String> entry = value.getValue().entrySet().iterator().next();
            return createNode(ctx.getTerm(), entry.getValue(), entry.getKey());
        }
        final SetNode collectionNode = JsonNodeFactory.createCollectionNodeFromArray(ctx.getTerm());
        addTranslationsToCollectionNode(value, collectionNode);
        return collectionNode;
    }

    private static JsonNode createNode(String attName, String value, String language) {
        final ObjectNode node = JsonNodeFactory.createObjectNode(attName);
        node.addItem(JsonNodeFactory.createStringLiteralNode(JsonLd.LANGUAGE, language != null ? language : JsonLd.NONE));
        node.addItem(JsonNodeFactory.createStringLiteralNode(JsonLd.VALUE, value));
        return node;
    }

    private void addTranslationsToCollectionNode(MultilingualString str, SetNode target) {
        str.getValue().forEach((lang, val) -> target.addItem(createNode(null, val, lang)));
    }
}
