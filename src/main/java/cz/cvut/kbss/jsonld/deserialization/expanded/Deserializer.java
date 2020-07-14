/**
 * Copyright (C) 2020 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.deserialization.expanded;

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.deserialization.InstanceBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

abstract class Deserializer<X> {

    final InstanceBuilder instanceBuilder;
    final DeserializerConfig config;

    Deserializer(InstanceBuilder instanceBuilder, DeserializerConfig config) {
        this.instanceBuilder = instanceBuilder;
        this.config = config;
    }

    Configuration configuration() {
        return config.getConfiguration();
    }

    <T> Class<? extends T> resolveTargetClass(Object jsonRoot, Class<T> resultClass) {
        if (BeanClassProcessor.isIdentifierType(resultClass)) {
            return resultClass;
        }
        final List<String> types = getObjectTypes(jsonRoot);
        return config.getTargetResolver().getTargetClass(resultClass, types);
    }

    List<String> getObjectTypes(Object jsonLdObject) {
        assert jsonLdObject instanceof Map;
        final Object types = ((Map<?, ?>) jsonLdObject).get(JsonLd.TYPE);
        if (types == null) {
            return Collections.emptyList();
        }
        assert types instanceof List;
        return (List<String>) types;
    }

    abstract void processValue(X value);
}
