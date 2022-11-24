package cz.cvut.kbss.jsonld.serialization.serializer.context.datetime;

import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.serializer.SerializerUtils;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime.LocalDateSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.LocalDate;
import java.time.temporal.TemporalAccessor;

public class ContextBuildingLocalDateSerializer extends LocalDateSerializer {

    @Override
    public JsonNode serialize(LocalDate value, SerializationContext<TemporalAccessor> ctx) {
        if (ctx.getTerm() != null) {
            final ObjectNode termDef =
                    SerializerUtils.createTypedTermDefinition(ctx.getFieldName(), ctx.getTerm(), XSD.DATE);
            ctx.registerTermMapping(ctx.getFieldName(), termDef);
        }
        return JsonNodeFactory.createLiteralNode(ctx.getTerm(), FORMATTER.format(value));
    }
}
