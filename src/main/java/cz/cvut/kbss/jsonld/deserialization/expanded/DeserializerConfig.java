/**
 * Copyright (C) 2022 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.deserialization.expanded;

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.deserialization.ValueDeserializers;
import cz.cvut.kbss.jsonld.deserialization.util.TargetClassResolver;

class DeserializerConfig {

    private final Configuration configuration;
    private final TargetClassResolver targetResolver;
    private final ValueDeserializers deserializers;

    DeserializerConfig(Configuration configuration, TargetClassResolver targetResolver, ValueDeserializers deserializers) {
        this.configuration = configuration;
        this.targetResolver = targetResolver;
        this.deserializers = deserializers;
    }

    Configuration getConfiguration() {
        return configuration;
    }

    TargetClassResolver getTargetResolver() {
        return targetResolver;
    }

    ValueDeserializers getDeserializers() {
        return deserializers;
    }
}
