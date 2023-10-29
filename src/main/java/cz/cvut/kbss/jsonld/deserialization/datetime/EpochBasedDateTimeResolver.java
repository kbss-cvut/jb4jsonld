package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jopa.datatype.DateTimeUtil;
import cz.cvut.kbss.jsonld.common.Configurable;
import jakarta.json.JsonNumber;

import java.time.Instant;
import java.time.OffsetDateTime;

/**
 * Resolves date time from the specified number of milliseconds since the Unix Epoch.
 */
class EpochBasedDateTimeResolver implements Configurable {

    OffsetDateTime resolve(JsonNumber value) {
        assert value != null;
        return DateTimeUtil.toDateTime(Instant.ofEpochMilli(value.longValue()));
    }
}
