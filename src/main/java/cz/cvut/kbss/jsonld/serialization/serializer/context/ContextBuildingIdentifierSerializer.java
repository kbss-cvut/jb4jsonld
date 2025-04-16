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
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.ObjectIdNode;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

public class ContextBuildingIdentifierSerializer implements ValueSerializer<String> {

    @Override
    public ObjectIdNode serialize(String value, SerializationContext<String> ctx) {
        if (ctx.getField() != null) {
            ctx.registerTermMapping(ctx.getFieldName(), JsonLd.ID);
            return JsonNodeFactory.createObjectIdNode(ctx.getTerm(), value);
        }
        return JsonNodeFactory.createObjectIdNode(ctx.getMappedTerm(JsonLd.ID).orElse(ctx.getTerm()), value);
    }
}
