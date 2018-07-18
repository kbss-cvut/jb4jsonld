/**
 * Copyright (C) 2017 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.deserialization.expanded;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.deserialization.InstanceBuilder;
import cz.cvut.kbss.jsonld.exception.MissingIdentifierException;

import java.net.URI;
import java.util.List;
import java.util.Map;

class CollectionDeserializer extends Deserializer<List<?>> {

    private final String property;

    CollectionDeserializer(InstanceBuilder instanceBuilder, DeserializerConfig config, String property) {
        super(instanceBuilder, config);
        this.property = property;
    }

    @Override
    void processValue(List<?> value) {
        if (value.size() == 1 && value.get(0) instanceof Map) {
            final Map<?, ?> map = (Map<?, ?>) value.get(0);
            if (!instanceBuilder.isPlural(property)) {
                resolvePropertyValue(property, map);
                return;
            }
            if (map.size() == 1 && map.containsKey(JsonLd.LIST)) {
                assert map.get(JsonLd.LIST) instanceof List;
                processValue((List<?>) map.get(JsonLd.LIST));
                return;
            }
        }
        instanceBuilder.openCollection(property);
        for (Object item : value) {
            if (item instanceof Map) {
                resolveValue((Map<?, ?>) item);
            } else {
                instanceBuilder.addValue(item);
            }
        }
        instanceBuilder.closeCollection();
    }

    private void resolveValue(Map<?, ?> value) {
        if (value.size() == 1 && value.containsKey(JsonLd.VALUE)) {
            instanceBuilder.addValue(value.get(JsonLd.VALUE));
        } else if (value.size() == 1 && value.containsKey(JsonLd.ID)) {
            instanceBuilder.addNodeReference(value.get(JsonLd.ID).toString());
        } else if (instanceBuilder.isCurrentCollectionProperties()) {
            // If we are deserializing an object into @Properties, just extract the identifier and put it into the map
            if (!value.containsKey(JsonLd.ID)) {
                throw new MissingIdentifierException(
                        "Cannot put an object without an identifier into @Properties. Object: " + value);
            }
            instanceBuilder.addValue(URI.create(value.get(JsonLd.ID).toString()));
        } else {
            final Class<?> elementType = instanceBuilder.getCurrentCollectionElementType();
            new ObjectDeserializer(instanceBuilder, config, elementType).processValue(value);
        }
    }

    private void resolvePropertyValue(String property, Map<?, ?> value) {
        if (value.size() == 1 && value.containsKey(JsonLd.VALUE)) {
            instanceBuilder.addValue(property, value.get(JsonLd.VALUE));
        } else if (value.size() == 1 && value.containsKey(JsonLd.ID)) {
            instanceBuilder.addNodeReference(property, value.get(JsonLd.ID).toString());
        } else {
            new ObjectDeserializer(instanceBuilder, config, property).processValue(value);
        }
    }
}
