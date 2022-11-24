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
        final ObjectNode node = ctx.getField() != null ? JsonNodeFactory.createObjectNode(ctx.getTerm()) : JsonNodeFactory.createObjectNode();
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
        final ObjectNode mapping = JsonNodeFactory.createObjectNode();
        mapping.addItem(JsonNodeFactory.createLiteralNode(JsonLd.ID, ctx.getTerm()));
        mapping.addItem(JsonNodeFactory.createLiteralNode(JsonLd.TYPE, JsonLd.LANGUAGE));
        ctx.registerTermMapping(ctx.getFieldName(), mapping);
    }
}
