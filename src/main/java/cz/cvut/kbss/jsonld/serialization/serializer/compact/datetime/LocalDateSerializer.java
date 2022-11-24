package cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime;

import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.SerializerUtils;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class LocalDateSerializer {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE;

    public JsonNode serialize(LocalDate value, SerializationContext<TemporalAccessor> ctx) {
        return SerializerUtils.createdTypedValueNode(ctx.getTerm(), FORMATTER.format(value), XSD.DATE);
    }
}
