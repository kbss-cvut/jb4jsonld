package cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime;

import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.SerializerUtils;
import cz.cvut.kbss.jsonld.serialization.serializer.datetime.DateTimeSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * Serializes datetime value as string in the ISO 8601 format (unless a different format is configured).
 */
public class IsoDateTimeSerializer extends DateTimeSerializer {

    protected DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public JsonNode serialize(OffsetDateTime value, SerializationContext<TemporalAccessor> ctx) {
        return SerializerUtils.createdTypedValueNode(ctx.getTerm(), formatter.format(value), XSD.DATETIME);
    }

    @Override
    public void configure(Configuration configuration) {
        if (configuration.has(ConfigParam.DATE_TIME_FORMAT)) {
            this.formatter = DateTimeFormatter.ofPattern(configuration.get(ConfigParam.DATE_TIME_FORMAT));
        }
    }
}
