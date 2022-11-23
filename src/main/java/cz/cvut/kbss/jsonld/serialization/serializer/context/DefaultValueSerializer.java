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
public class DefaultValueSerializer implements ValueSerializer {

    private final ValueSerializer<MultilingualString> multilingualStringValueSerializer;
    private final ValueSerializer<Collection<MultilingualStringSerializer>> pluralMultilingualSerializer;

    public DefaultValueSerializer(ValueSerializer<MultilingualString> multilingualStringValueSerializer,
                                  ValueSerializer<Collection<MultilingualStringSerializer>> pluralMultilingualSerializer) {
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
                return pluralMultilingualSerializer.serialize((Collection<MultilingualStringSerializer>) col, ctx);
            } else {
                return serializeLiterals(col, ctx);
            }
        } else {
            if (SerializerUtils.isAnnotationReference(value, ctx)) {
                return serializeReference(value, ctx);
            } else if (value instanceof MultilingualString) {
                return multilingualStringValueSerializer.serialize((MultilingualString) value, ctx);
            } else {
                ctx.getJsonLdContext().registerTermMapping(ctx.getFieldName(), ctx.getTerm());
                return JsonNodeFactory.createLiteralNode(ctx.getFieldName(), value);
            }
        }
    }

    private static JsonNode serializeReferences(Collection<?> elems, SerializationContext<?> ctx) {
        ctx.getJsonLdContext().registerTermMapping(ctx.getFieldName(), ctx.getTerm());
        final CollectionNode<?> result = JsonNodeFactory.createCollectionNode(ctx.getFieldName(), elems);
        elems.forEach(
                e -> result.addItem(serializeReference(e, new SerializationContext<>(e, ctx.getJsonLdContext()))));
        return result;
    }

    private static JsonNode serializeReference(Object value, SerializationContext<?> ctx) {
        final ObjectNode node;
        if (ctx.getTerm() != null) {
            ctx.getJsonLdContext().registerTermMapping(ctx.getFieldName(), ctx.getTerm());
            node = JsonNodeFactory.createObjectNode(ctx.getFieldName());
        } else {
            node = JsonNodeFactory.createObjectNode();
        }
        node.addItem(JsonNodeFactory.createLiteralNode(JsonLd.ID, value));
        return node;
    }

    private static JsonNode serializeLiterals(Collection<?> elems, SerializationContext<?> ctx) {
        ctx.getJsonLdContext().registerTermMapping(ctx.getFieldName(), ctx.getTerm());
        final CollectionNode<?> result = JsonNodeFactory.createCollectionNode(ctx.getFieldName(), elems);
        elems.forEach(e -> result.addItem(JsonNodeFactory.createLiteralNode(e)));
        return result;
    }
}
