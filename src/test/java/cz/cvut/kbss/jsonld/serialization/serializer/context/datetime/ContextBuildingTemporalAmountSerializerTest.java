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
import cz.cvut.kbss.jsonld.environment.Generator;
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
import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ContextBuildingTemporalAmountSerializerTest {

    private final ContextBuildingTemporalAmountSerializer sut = new ContextBuildingTemporalAmountSerializer();

    @Test
    void serializeRegistersTermDefinitionWithIdAndTypeInJsonLdContext() throws Exception {
        final JsonLdContext ctx = mock(JsonLdContext.class);
        final Duration value = Duration.ofSeconds(Generator.randomInt(10000));
        final Field field = TemporalEntity.class.getDeclaredField("duration");
        final String property = Generator.generateUri().toString();
        final SerializationContext<TemporalAmount> serializationContext =
                new SerializationContext<>(property, field, value, ctx);
        sut.serialize(value, serializationContext);
        final ArgumentCaptor<ObjectNode> captor = ArgumentCaptor.forClass(ObjectNode.class);
        verify(ctx).registerTermMapping(eq(field.getName()), captor.capture());
        assertThat(captor.getValue().getItems(), hasItems(
                JsonNodeFactory.createLiteralNode(JsonLd.ID, property),
                JsonNodeFactory.createLiteralNode(JsonLd.TYPE, XSD.DURATION)
        ));
    }

    @Test
    void serializeReturnsLiteralNodeWithStringSerialization() throws Exception {
        final Period value = Period.ofMonths(Generator.randomInt(100));
        final Field field = TemporalEntity.class.getDeclaredField("period");
        final String property = Generator.generateUri().toString();
        final SerializationContext<TemporalAmount> serializationContext =
                new SerializationContext<>(property, field, value, DummyJsonLdContext.INSTANCE);

        final JsonNode result = sut.serialize(value, serializationContext);
        assertEquals(new StringLiteralNode(field.getName(), value.toString()), result);
    }
}