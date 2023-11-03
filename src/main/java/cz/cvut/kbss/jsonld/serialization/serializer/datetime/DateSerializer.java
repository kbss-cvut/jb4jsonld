/*
 * JB4JSON-LD
 * Copyright (C) 2023 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.serialization.serializer.datetime;

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime.TemporalSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

public class DateSerializer implements ValueSerializer<Date> {

    private final TemporalSerializer temporalSerializer;

    public DateSerializer(TemporalSerializer temporalSerializer) {
        this.temporalSerializer = temporalSerializer;
    }

    @Override
    public JsonNode serialize(Date value, SerializationContext<Date> ctx) {
        final Instant instant = value.toInstant();
        final SerializationContext<TemporalAccessor> newCtx = new SerializationContext<>(ctx.getTerm(), ctx.getField(), instant, ctx.getJsonLdContext());
        return temporalSerializer.serialize(value.toInstant(), newCtx);
    }

    @Override
    public void configure(Configuration config) {
        temporalSerializer.configure(config);
    }
}
