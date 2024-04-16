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

import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.serializer.SerializerUtils;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime.LocalDateSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.LocalDate;
import java.time.temporal.TemporalAccessor;

public class ContextBuildingLocalDateSerializer extends LocalDateSerializer {

    @Override
    public JsonNode serialize(LocalDate value, SerializationContext<TemporalAccessor> ctx) {
        if (ctx.getTerm() != null) {
            final ObjectNode termDef =
                    SerializerUtils.createTypedTermDefinition(ctx.getFieldName(), ctx.getTerm(), XSD.DATE);
            ctx.registerTermMapping(ctx.getFieldName(), termDef);
        }
        return JsonNodeFactory.createLiteralNode(ctx.getTerm(), FORMATTER.format(value));
    }
}
