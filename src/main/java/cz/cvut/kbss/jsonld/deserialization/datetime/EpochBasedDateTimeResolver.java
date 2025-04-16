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
package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jopa.datatype.DateTimeUtil;
import cz.cvut.kbss.jsonld.common.Configurable;
import jakarta.json.JsonNumber;

import java.time.Instant;
import java.time.OffsetDateTime;

/**
 * Resolves date time from the specified number of milliseconds since the Unix Epoch.
 */
class EpochBasedDateTimeResolver implements Configurable {

    OffsetDateTime resolve(JsonNumber value) {
        assert value != null;
        return DateTimeUtil.toDateTime(Instant.ofEpochMilli(value.longValue()));
    }
}
