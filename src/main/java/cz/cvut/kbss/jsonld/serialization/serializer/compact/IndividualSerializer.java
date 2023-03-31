package cz.cvut.kbss.jsonld.serialization.serializer.compact;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.common.EnumUtil;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

/**
 * Serializes individuals.
 * <p>
 * That is, either a plain identifier value of an object property attribute or an enum constant mapped to an
 * individual.
 */
public class IndividualSerializer implements ValueSerializer {

    @Override
    public JsonNode serialize(Object value, SerializationContext ctx) {
        assert BeanClassProcessor.isIdentifierType(value.getClass()) || value.getClass().isEnum();
        if (BeanClassProcessor.isIdentifierType(value.getClass())) {
            final ObjectNode node = JsonNodeFactory.createObjectNode(ctx.getTerm());
            node.addItem(JsonNodeFactory.createObjectIdNode(idAttribute(ctx), value));
            return node;
        } else {
            return serializeEnumConstant((Enum<?>) value, ctx);
        }
    }

    private String idAttribute(SerializationContext<?> ctx) {
        return ctx.getJsonLdContext().getMappedTerm(JsonLd.ID).orElse(JsonLd.ID);
    }

    protected JsonNode serializeEnumConstant(Enum<?> constant, SerializationContext<?> ctx) {
        final String iri = EnumUtil.resolveMappedIndividual(constant);
        final ObjectNode node = JsonNodeFactory.createObjectNode(ctx.getTerm());
        node.addItem(JsonNodeFactory.createObjectIdNode(idAttribute(ctx), iri));
        return node;
    }
}
