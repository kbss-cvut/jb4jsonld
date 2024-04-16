/*
 * JB4JSON-LD
 * Copyright (C) 2024 Czech Technical University in Prague
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package cz.cvut.kbss.jsonld.integration;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.deserialization.JsonLdDeserializer;
import cz.cvut.kbss.jsonld.environment.TestUtil;
import cz.cvut.kbss.jsonld.environment.model.TemporalEntity;
import cz.cvut.kbss.jsonld.serialization.JsonLdSerializer;
import cz.cvut.kbss.jsonld.serialization.util.BufferedJsonGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    private TemporalEntity serializeAndDeserialize(TemporalEntity original) throws Exception {
        serializer.serialize(original);
        final String jsonLd = jsonWriter.getResult();
        return deserializer.deserialize(TestUtil.parseAndExpand(jsonLd), TemporalEntity.class);
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
