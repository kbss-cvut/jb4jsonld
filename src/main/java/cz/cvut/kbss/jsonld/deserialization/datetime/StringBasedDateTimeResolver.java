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
package cz.cvut.kbss.jsonld.deserialization.datetime;

import cz.cvut.kbss.jopa.datatype.xsd.XsdDateTimeMapper;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.common.Configurable;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

class StringBasedDateTimeResolver implements Configurable {

    private DateTimeFormatter formatter;

    OffsetDateTime resolve(String value) {
        assert value != null;
        return formatter != null ? OffsetDateTime.parse(value, formatter) : XsdDateTimeMapper.map(value);
    }

    @Override
    public void configure(Configuration configuration) {
        if (configuration.has(ConfigParam.DATE_TIME_FORMAT)) {
            formatter = DateTimeFormatter.ofPattern(configuration.get(ConfigParam.DATE_TIME_FORMAT));
        }
    }
}
