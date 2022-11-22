package cz.cvut.kbss.jsonld.serialization.serializer.datetime;

import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class LocalDateSerializer {

    public static JsonNode serialize(LocalDate value, SerializationContext<TemporalAccessor> ctx) {
        final ObjectNode node = JsonNodeFactory.createObjectNode(ctx.getTerm());
        node.addItem(JsonNodeFactory.createLiteralNode(JsonLd.TYPE, XSD.DATE));
        node.addItem(JsonNodeFactory.createLiteralNode(JsonLd.VALUE, DateTimeFormatter.ISO_DATE.format(value)));
        return node;
    }
}
