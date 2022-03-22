package cz.cvut.kbss.jsonld.integration;

import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.deserialization.JsonLdDeserializer;
import cz.cvut.kbss.jsonld.environment.model.TemporalEntity;
import cz.cvut.kbss.jsonld.serialization.JsonLdSerializer;
import cz.cvut.kbss.jsonld.serialization.util.BufferedJsonGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TemporalValuesHandlingTest {

    private BufferedJsonGenerator jsonWriter;

    private JsonLdSerializer serializer;

    private JsonLdDeserializer deserializer;

    @BeforeEach
    void setUp() {
        this.jsonWriter = new BufferedJsonGenerator();
        this.serializer = JsonLdSerializer.createCompactedJsonLdSerializer(jsonWriter);
        final Configuration configuration = new Configuration();
        configuration.set(ConfigParam.SCAN_PACKAGE, "cz.cvut.kbss.jsonld");
        configuration.set(ConfigParam.REQUIRE_ID, Boolean.toString(false));
        this.deserializer = JsonLdDeserializer.createExpandedDeserializer(configuration);
    }

    @Test
    void serializationAndDeserializationAreCompatibleForTemporalAccessorValues() throws Exception {
        final TemporalEntity original = new TemporalEntity();
        original.initTemporalAccessorValues();
        final TemporalEntity result = serializeAndDeserialize(original);
        assertEquals(original.getOffsetDateTime(), result.getOffsetDateTime());
        assertEquals(original.getLocalDateTime(), result.getLocalDateTime());
        assertEquals(original.getZonedDateTime(), result.getZonedDateTime());
        assertEquals(original.getInstant(), result.getInstant());
        assertEquals(original.getTimestamp(), result.getTimestamp());
        assertEquals(original.getOffsetTime(), result.getOffsetTime());
        assertEquals(original.getLocalTime(), result.getLocalTime());
        assertEquals(original.getLocalDate(), result.getLocalDate());
    }

    private TemporalEntity serializeAndDeserialize(TemporalEntity original) throws IOException {
        serializer.serialize(original);
        final String jsonLd = jsonWriter.getResult();
        final Object jsonObject = JsonUtils.fromString(jsonLd);
        return deserializer.deserialize(JsonLdProcessor.expand(jsonObject), TemporalEntity.class);
    }

    @Test
    void serializationAndDeserializationAreCompatibleForTemporalAmountValues() throws Exception {
        final TemporalEntity original = new TemporalEntity();
        original.initTemporalAmountValues();
        final TemporalEntity result = serializeAndDeserialize(original);
        assertEquals(original.getDuration(), result.getDuration());
        assertEquals(original.getPeriod(), result.getPeriod());
    }
}
