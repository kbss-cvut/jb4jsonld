package cz.cvut.kbss.jsonld.serialization.datetime;

import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

/**
 * Serializes datetime as the amount of milliseconds since epoch at UTC time zone.
 */
public class EpochBasedDateTimeSerializer extends DateTimeSerializer {

    @Override
    JsonNode serialize(OffsetDateTime value, SerializationContext<TemporalAccessor> ctx) {
        // TODO
        return null;
    }
}
