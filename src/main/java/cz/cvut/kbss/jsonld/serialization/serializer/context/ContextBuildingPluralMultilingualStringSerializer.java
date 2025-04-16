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
import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.util.*;

public class ContextBuildingPluralMultilingualStringSerializer implements ValueSerializer<Collection<MultilingualString>> {

    @Override
    public ObjectNode serialize(Collection<MultilingualString> value,
                              SerializationContext<Collection<MultilingualString>> ctx) {
        if (ctx.getTerm() != null) {
            registerTermMapping(ctx);
        }
        final Map<String, Set<String>> allValues = new HashMap<>();
        value.forEach(ms -> ms.getValue().forEach((lang, text) -> {
            allValues.putIfAbsent(lang, new LinkedHashSet<>());
            allValues.get(lang).add(text);
        }));
        final ObjectNode node = JsonNodeFactory.createObjectNode(ctx.getTerm());
        allValues.forEach((lang, texts) -> {
            final String langKey = lang != null ? lang : JsonLd.NONE;
            if (texts.size() == 1) {
                node.addItem(JsonNodeFactory.createLiteralNode(langKey, texts.iterator().next()));
            } else {
                final CollectionNode<?> translations = JsonNodeFactory.createCollectionNodeFromArray(langKey);
                texts.forEach(t -> translations.addItem(JsonNodeFactory.createLiteralNode(t)));
                node.addItem(translations);
            }
        });
        return node;
    }

    static void registerTermMapping(SerializationContext<Collection<MultilingualString>> ctx) {
        final ObjectNode mapping = JsonNodeFactory.createObjectNode(ctx.getFieldName());
        mapping.addItem(JsonNodeFactory.createLiteralNode(JsonLd.ID, ctx.getTerm()));
        mapping.addItem(JsonNodeFactory.createLiteralNode(JsonLd.CONTAINER, JsonLd.LANGUAGE));
        ctx.registerTermMapping(ctx.getFieldName(), mapping);
    }
}
