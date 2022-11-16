package cz.cvut.kbss.jsonld.serialization.datetime;

import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * Serializes datetime value as string in the ISO 8601 format (unless a different format is configured).
 */
public class IsoDateTimeSerializer extends DateTimeSerializer {

    private DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    JsonNode serialize(OffsetDateTime value, SerializationContext<TemporalAccessor> ctx) {
        return JsonNodeFactory.createTypedValueNode(ctx.getAttributeId(), value.format(formatter), XSD.DATETIME);
    }

    @Override
    public void configure(Configuration configuration) {
        if (configuration.has(ConfigParam.DATE_TIME_FORMAT)) {
            this.formatter = DateTimeFormatter.ofPattern(configuration.get(ConfigParam.DATE_TIME_FORMAT));
        }
    }
}
