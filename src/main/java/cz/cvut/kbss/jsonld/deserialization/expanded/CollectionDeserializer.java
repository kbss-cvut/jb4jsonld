/**
 * Copyright (C) 2022 Czech Technical University in Prague
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
import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.InstanceBuilder;
import cz.cvut.kbss.jsonld.deserialization.util.DataTypeTransformer;
import cz.cvut.kbss.jsonld.deserialization.util.LangString;
import cz.cvut.kbss.jsonld.deserialization.util.XSDTypeCoercer;
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
                resolvePropertyValue(map);
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
        final Class<?> targetType = instanceBuilder.getCurrentCollectionElementType();
        if (config.getDeserializers().hasCustomDeserializer(targetType)) {
            instanceBuilder.addValue(deserializeUsingCustomDeserializer(targetType, value));
        } else if (value.size() == 1 && value.containsKey(JsonLd.VALUE)) {
            instanceBuilder.addValue(value.get(JsonLd.VALUE));
        } else if (value.size() == 1 && value.containsKey(JsonLd.ID)) {
            handleReferenceNodeInCollection(value, targetType);
        } else if (value.containsKey(JsonLd.LANGUAGE)) {
            assert value.containsKey(JsonLd.VALUE);
            instanceBuilder.addValue(
                    new LangString(value.get(JsonLd.VALUE).toString(), value.get(JsonLd.LANGUAGE).toString()));
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

    private void handleReferenceNodeInCollection(Map<?, ?> value, Class<?> targetType) {
        assert value.size() == 1 && value.containsKey(JsonLd.ID);
        final String identifier = value.get(JsonLd.ID).toString();
        if (targetType.isEnum()) {
            instanceBuilder.addValue(DataTypeTransformer.transformIndividualToEnumConstant(identifier,
                                                                                           (Class<? extends Enum>) targetType));
        } else {
            instanceBuilder.addNodeReference(identifier);
        }
    }

    private void resolvePropertyValue(Map<?, ?> value) {
        final Class<?> targetType = instanceBuilder.getTargetType(property);
        if (config.getDeserializers().hasCustomDeserializer(targetType)) {
            instanceBuilder.addValue(property, deserializeUsingCustomDeserializer(targetType, value));
            return;
        }
        if (value.containsKey(JsonLd.VALUE)) {
            extractLiteralValue(value);
        } else if (value.size() == 1 && value.containsKey(JsonLd.ID)) {
            handleSingularReferenceNode(value, targetType);
        } else {
            new ObjectDeserializer(instanceBuilder, config, property).processValue(value);
        }
    }

    private <T> T deserializeUsingCustomDeserializer(Class<T> targetType, Map<?, ?> value) {
        final DeserializationContext<T> ctx = new DeserializationContext<>(targetType, config.getTargetResolver());
        assert config.getDeserializers().getDeserializer(ctx).isPresent();
        return config.getDeserializers().getDeserializer(ctx).get().deserialize(value, ctx);
    }

    private void extractLiteralValue(Map<?, ?> value) {
        final Object val = value.get(JsonLd.VALUE);
        if (value.containsKey(JsonLd.TYPE)) {
            instanceBuilder
                    .addValue(property, XSDTypeCoercer.coerceType(val.toString(), value.get(JsonLd.TYPE).toString()));
        } else if (value.containsKey(JsonLd.LANGUAGE)) {
            instanceBuilder.addValue(property, new LangString(val.toString(), value.get(JsonLd.LANGUAGE).toString()));
        } else {
            instanceBuilder.addValue(property, val);
        }
    }

    private void handleSingularReferenceNode(Map<?, ?> value, Class<?> targetType) {
        assert value.size() == 1 && value.containsKey(JsonLd.ID);
        final String identifier = value.get(JsonLd.ID).toString();
        if (targetType.isEnum()) {
            instanceBuilder.addValue(property, DataTypeTransformer.transformIndividualToEnumConstant(identifier,
                                                                                                     (Class<? extends Enum>) targetType));
        } else {
            instanceBuilder.addNodeReference(property, identifier);
        }
    }
}
