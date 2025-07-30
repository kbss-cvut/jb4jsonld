/*
 * JB4JSON-LD
 * Copyright (C) 2025 Czech Technical University in Prague
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

import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.TemporalEntity;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.context.DummyJsonLdContext;
import cz.cvut.kbss.jsonld.serialization.context.JsonLdContext;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.model.StringLiteralNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ContextBuildingIsoDateContextBuildingTimeSerializerTest {

    private final ContextBuildingIsoDateTimeSerializer sut = new ContextBuildingIsoDateTimeSerializer();

    @Test
    void serializeRegistersTermDefinitionWithIdAndTypeInJsonLdContext() throws Exception {
        final JsonLdContext ctx = mock(JsonLdContext.class);
        final OffsetDateTime value = OffsetDateTime.now();
        final Field field = TemporalEntity.class.getDeclaredField("offsetDateTime");
        final SerializationContext<TemporalAccessor> serializationContext =
                new SerializationContext<>(Vocabulary.DATE_CREATED, field, value, ctx);
        sut.serialize(value, serializationContext);
        final ArgumentCaptor<ObjectNode> captor = ArgumentCaptor.forClass(ObjectNode.class);
        verify(ctx).registerTermMapping(eq(field.getName()), captor.capture());
        assertThat(captor.getValue().getItems(), hasItems(
                JsonNodeFactory.createStringLiteralNode(JsonLd.ID, Vocabulary.DATE_CREATED),
                JsonNodeFactory.createStringLiteralNode(JsonLd.TYPE, XSD.DATETIME)
        ));
    }

    @Test
    void serializeReturnsLiteralNodeWithStringSerialization() throws Exception {
        final OffsetDateTime value = OffsetDateTime.now();
        final Field field = TemporalEntity.class.getDeclaredField("offsetDateTime");
        final SerializationContext<TemporalAccessor> serializationContext =
                new SerializationContext<>(Vocabulary.DATE_CREATED, field, value, DummyJsonLdContext.INSTANCE);

        final JsonNode result = sut.serialize(value, serializationContext);
        assertEquals(new StringLiteralNode(field.getName(), DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value)),
                     result);
    }
}