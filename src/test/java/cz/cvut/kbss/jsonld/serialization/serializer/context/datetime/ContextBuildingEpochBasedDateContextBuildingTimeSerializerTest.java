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
package cz.cvut.kbss.jsonld.serialization.serializer.context.datetime;

import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.TemporalEntity;
import cz.cvut.kbss.jsonld.serialization.context.JsonLdContext;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ContextBuildingEpochBasedDateContextBuildingTimeSerializerTest {

    private final ContextBuildingEpochBasedDateTimeSerializer sut = new ContextBuildingEpochBasedDateTimeSerializer();

    @Test
    void serializeRegistersTermIriInJsonLdContext() throws Exception {
        final JsonLdContext ctx = mock(JsonLdContext.class);
        final OffsetDateTime value = OffsetDateTime.now();
        final Field field = TemporalEntity.class.getDeclaredField("offsetDateTime");
        final SerializationContext<TemporalAccessor> serializationContext =
                new SerializationContext<>(Vocabulary.DATE_CREATED, field, value, ctx);
        sut.serialize(value, serializationContext);
        verify(ctx).registerTermMapping(field.getName(), Vocabulary.DATE_CREATED);
    }
}