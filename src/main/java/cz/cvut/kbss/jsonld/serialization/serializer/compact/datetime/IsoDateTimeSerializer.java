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
package cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime;

import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.SerializerUtils;
import cz.cvut.kbss.jsonld.serialization.serializer.datetime.DateTimeSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * Serializes datetime value as string in the ISO 8601 format (unless a different format is configured).
 */
public class IsoDateTimeSerializer extends DateTimeSerializer {

    protected DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public JsonNode serialize(OffsetDateTime value, SerializationContext<TemporalAccessor> ctx) {
        return SerializerUtils.createdTypedValueNode(ctx.getTerm(), formatter.format(value), XSD.DATETIME);
    }

    @Override
    public void configure(Configuration configuration) {
        if (configuration.has(ConfigParam.DATE_TIME_FORMAT)) {
            this.formatter = DateTimeFormatter.ofPattern(configuration.get(ConfigParam.DATE_TIME_FORMAT));
        }
    }
}
