package cz.cvut.kbss.jsonld.serialization.serializer.context;

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

/**
 * Default serializer for non-object property values.
 */
public class ContextBuildingDefaultValueSerializer implements ValueSerializer {

    private final ValueSerializer<MultilingualString> multilingualStringValueSerializer;
    private final ValueSerializer<Collection<MultilingualString>> pluralMultilingualSerializer;

    public ContextBuildingDefaultValueSerializer(ValueSerializer<MultilingualString> multilingualStringValueSerializer,
                                                 ValueSerializer<Collection<MultilingualString>> pluralMultilingualSerializer) {
        this.multilingualStringValueSerializer = multilingualStringValueSerializer;
        this.pluralMultilingualSerializer = pluralMultilingualSerializer;
    }

    @Override
    public JsonNode serialize(Object value, SerializationContext ctx) {
        if (value instanceof Collection) {
            final Collection<?> col = (Collection<?>) value;
            if (col.isEmpty()) {
                return JsonNodeFactory.createCollectionNode(ctx.getTerm(), col);
            }
            final Object elem = col.iterator().next();
            if (SerializerUtils.isAnnotationReference(elem, ctx)) {
                return serializeReferences(col, ctx);
            } else if (elem instanceof MultilingualString) {
                return pluralMultilingualSerializer.serialize((Collection<MultilingualString>) col, ctx);
            } else {
                return serializeLiterals(col, ctx);
            }
        } else {
            if (SerializerUtils.isAnnotationReference(value, ctx)) {
                return serializeReference(value, ctx);
            } else if (value instanceof MultilingualString) {
                return multilingualStringValueSerializer.serialize((MultilingualString) value, ctx);
            } else {
                if (ctx.getField() != null) {
                    ctx.registerTermMapping(ctx.getFieldName(), ctx.getTerm());
                }
                return JsonNodeFactory.createLiteralNode(ctx.getTerm(), value);
            }
        }
    }

    private static JsonNode serializeReferences(Collection<?> elems, SerializationContext<?> ctx) {
        ctx.registerTermMapping(ctx.getFieldName(), ctx.getTerm());
        final CollectionNode<?> result = JsonNodeFactory.createCollectionNode(ctx.getTerm(), elems);
        elems.forEach(
                e -> result.addItem(serializeReference(e, new SerializationContext<>(e, ctx.getJsonLdContext()))));
        return result;
    }

    private static JsonNode serializeReference(Object value, SerializationContext<?> ctx) {
        final ObjectNode node;
        if (ctx.getTerm() != null) {
            ctx.registerTermMapping(ctx.getFieldName(), ctx.getTerm());
            node = JsonNodeFactory.createObjectNode(ctx.getTerm());
        } else {
            node = JsonNodeFactory.createObjectNode();
        }
        node.addItem(JsonNodeFactory.createLiteralNode(JsonLd.ID, value));
        return node;
    }

    private static JsonNode serializeLiterals(Collection<?> elems, SerializationContext<?> ctx) {
        ctx.registerTermMapping(ctx.getFieldName(), ctx.getTerm());
        final CollectionNode<?> result = JsonNodeFactory.createCollectionNode(ctx.getTerm(), elems);
        elems.forEach(e -> result.addItem(JsonNodeFactory.createLiteralNode(e)));
        return result;
    }
}
