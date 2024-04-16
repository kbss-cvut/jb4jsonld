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
package cz.cvut.kbss.jsonld.serialization.serializer.datetime;

import cz.cvut.kbss.jopa.datatype.DateTimeUtil;
import cz.cvut.kbss.jsonld.common.Configurable;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

public abstract class DateTimeSerializer implements Configurable {

    public abstract JsonNode serialize(OffsetDateTime value, SerializationContext<TemporalAccessor> ctx);

    public JsonNode serialize(LocalDateTime value, SerializationContext<TemporalAccessor> ctx) {
        return serialize(DateTimeUtil.toDateTime(value), ctx);
    }

    public JsonNode serialize(Instant value, SerializationContext<TemporalAccessor> ctx) {
        return serialize(DateTimeUtil.toDateTime(value), ctx);
    }
}
