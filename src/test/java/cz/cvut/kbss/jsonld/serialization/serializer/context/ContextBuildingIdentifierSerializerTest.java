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
package cz.cvut.kbss.jsonld.serialization.serializer.context;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.serialization.context.MappingJsonLdContext;
import cz.cvut.kbss.jsonld.serialization.model.ObjectIdNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContextBuildingIdentifierSerializerTest {

    private final ContextBuildingIdentifierSerializer sut = new ContextBuildingIdentifierSerializer();

    @Test
    void serializeUsesIdentifierTermRegisteredInContextToBuildJsonNode() {
        final String id = Generator.generateUri().toString();
        final MappingJsonLdContext jsonLdCtx = new MappingJsonLdContext();
        final String fieldName = "uri";
        jsonLdCtx.registerTermMapping(fieldName, JsonLd.ID);
        final SerializationContext<String> ctx = new SerializationContext<>(JsonLd.ID, null, id, jsonLdCtx);

        final ObjectIdNode result = sut.serialize(id, ctx);
        assertEquals(fieldName, result.getName());
        assertEquals(id, result.getIdentifier());
    }
}