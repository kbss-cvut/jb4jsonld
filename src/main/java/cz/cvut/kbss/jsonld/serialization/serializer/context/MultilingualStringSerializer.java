package cz.cvut.kbss.jsonld.serialization.serializer.context;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

/**
 * This is used to serialize {@link MultilingualString} values.
 */
public class MultilingualStringSerializer implements ValueSerializer<MultilingualString> {

    @Override
    public JsonNode serialize(MultilingualString value, SerializationContext<MultilingualString> ctx) {
        if (ctx.getTerm() != null) {
            registerTermMapping(ctx);
        }
        final ObjectNode node = ctx.getField() != null ? JsonNodeFactory.createObjectNode(ctx.getFieldName()) : JsonNodeFactory.createObjectNode();
        value.getValue().forEach((lang, text) -> node.addItem(JsonNodeFactory.createLiteralNode(lang, text)));
        return node;
    }

    static void registerTermMapping(SerializationContext<MultilingualString> ctx) {
        final ObjectNode mapping = JsonNodeFactory.createObjectNode();
        mapping.addItem(JsonNodeFactory.createLiteralNode(JsonLd.ID, ctx.getTerm()));
        mapping.addItem(JsonNodeFactory.createLiteralNode(JsonLd.TYPE, JsonLd.LANGUAGE));
        ctx.getJsonLdContext().registerTermMapping(ctx.getFieldName(), mapping);
    }
}
