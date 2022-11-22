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
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.model.SetNode;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.util.Map;

/**
 * This use used to serialize {@link MultilingualString} values.
 */
class MultilingualStringSerializer implements ValueSerializer<MultilingualString> {

    @Override
    public JsonNode serialize(MultilingualString value, SerializationContext<MultilingualString> ctx) {
        if (value.getValue().size() == 1) {
            final Map.Entry<String, String> entry = value.getValue().entrySet().iterator().next();
            return createNode(ctx.getTerm(), entry.getValue(), entry.getKey());
        }
        final SetNode collectionNode = ctx.getTerm() != null ? JsonNodeFactory.createCollectionNodeFromArray(ctx.getTerm()) : JsonNodeFactory.createCollectionNodeFromArray();
        addTranslationsToCollectionNode(value, collectionNode);
        return collectionNode;
    }

    private static JsonNode createNode(String attName, String value, String language) {
        final ObjectNode node =
                attName != null ? JsonNodeFactory.createObjectNode(attName) : JsonNodeFactory.createObjectNode();
        node.addItem(JsonNodeFactory.createLiteralNode(JsonLd.LANGUAGE, language != null ? language : JsonLd.NONE));
        node.addItem(JsonNodeFactory.createLiteralNode(JsonLd.VALUE, value));
        return node;
    }

    private void addTranslationsToCollectionNode(MultilingualString str, SetNode target) {
        str.getValue().forEach((lang, val) -> target.addItem(createNode(null, val, lang)));
    }
}
