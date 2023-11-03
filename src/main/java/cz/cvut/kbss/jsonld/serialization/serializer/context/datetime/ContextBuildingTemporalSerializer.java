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
package cz.cvut.kbss.jsonld.serialization.serializer.context.datetime;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime.TemporalSerializer;

public class ContextBuildingTemporalSerializer extends TemporalSerializer {

    public ContextBuildingTemporalSerializer() {
        super(new ContextBuildingIsoDateTimeSerializer(), new ContextBuildingLocalDateSerializer(),
              new ContextBuildingTimeSerializer());
    }

    @Override
    public void configure(Configuration config) {
        assert config != null;
        this.dateTimeSerializer = config.is(ConfigParam.SERIALIZE_DATETIME_AS_MILLIS) ?
                                  new ContextBuildingEpochBasedDateTimeSerializer() :
                                  new ContextBuildingIsoDateTimeSerializer();
        dateTimeSerializer.configure(config);
    }
}
