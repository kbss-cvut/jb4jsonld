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
package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NodeReferenceContextTest {

    @Mock
    private InstanceContext ownerMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void setIdentifierValueTransformsValueToTargetType() throws Exception {
        final InstanceContext<?> ctx = new NodeReferenceContext<>(ownerMock, Collections.emptyMap());
        when(ownerMock.getItemType()).thenReturn(URL.class);
        final String id = Generator.generateUri().toString();
        ctx.setIdentifierValue(id);
        assertEquals(new URL(id), ctx.getInstance());
    }

    @Test
    void setIdentifierValueTransformsValueToTargetPropertyType() throws Exception {
        final Field field = Organization.class.getDeclaredField("country");
        final InstanceContext<?> ctx = new NodeReferenceContext<>(ownerMock, field, Collections.emptyMap());
        final String id = Generator.generateUri().toString();
        ctx.setIdentifierValue(id);
        assertEquals(URI.create(id), ctx.instance);
    }

    @Test
    void setIdentifierValueSetValueOfTypeStringWhenTargetIsString() {
        final InstanceContext<?> ctx = new NodeReferenceContext<>(ownerMock, Collections.emptyMap());
        when(ownerMock.getItemType()).thenReturn(String.class);
        final String id = Generator.generateUri().toString();
        ctx.setIdentifierValue(id);
        assertEquals(id, ctx.getInstance());
    }

    @Test
    void closeAddsValueToOpenCollection() {
        final InstanceContext<?> ctx = new NodeReferenceContext<>(ownerMock, Collections.emptyMap());
        when(ownerMock.getItemType()).thenReturn(URI.class);
        final String id = Generator.generateUri().toString();
        ctx.setIdentifierValue(id);
        ctx.close();
        verify(ownerMock).addItem(URI.create(id));
    }

    @Test
    void closeSetsFieldValueForCorrectProperty() throws Exception {
        final Field field = Organization.class.getDeclaredField("country");
        final InstanceContext<?> ctx = new NodeReferenceContext<>(ownerMock, field, Collections.emptyMap());
        final String id = Generator.generateUri().toString();
        ctx.setIdentifierValue(id);
        ctx.close();
        verify(ownerMock).setFieldValue(field, URI.create(id));
    }

    @Test
    void isPropertyMappedReturnsTrueForId() throws Exception {
        final Field field = Organization.class.getDeclaredField("country");
        final InstanceContext<?> ctx = new NodeReferenceContext<>(ownerMock, field, Collections.emptyMap());
        assertTrue(ctx.isPropertyMapped(JsonLd.ID));
        assertFalse(ctx.isPropertyMapped(Vocabulary.USERNAME));
    }

    @Test
    void supportsReturnsIsPropertyMappedResult() throws Exception {
        final Field field = Organization.class.getDeclaredField("country");
        final InstanceContext<?> ctx = new NodeReferenceContext<>(ownerMock, field, Collections.emptyMap());
        assertTrue(ctx.isPropertyMapped(JsonLd.ID));
        assertTrue(ctx.supports(JsonLd.ID));
        assertFalse(ctx.isPropertyMapped(Vocabulary.USERNAME));
        assertFalse(ctx.supports(Vocabulary.USERNAME));
    }
}
