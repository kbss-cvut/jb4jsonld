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
import cz.cvut.kbss.jsonld.deserialization.util.ValueUtils;
import cz.cvut.kbss.jsonld.deserialization.util.XSDTypeCoercer;
import cz.cvut.kbss.jsonld.exception.MissingIdentifierException;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import java.net.URI;

class CollectionDeserializer extends Deserializer<JsonArray> {

    private final String property;

    CollectionDeserializer(InstanceBuilder instanceBuilder, DeserializerConfig config, String property) {
        super(instanceBuilder, config);
        this.property = property;
    }

    @Override
    void processValue(JsonArray value) {
        if (value.size() == 1 && value.get(0).getValueType() == JsonValue.ValueType.OBJECT) {
            final JsonObject obj = value.getJsonObject(0);
            if (!instanceBuilder.isPlural(property)) {
                resolvePropertyValue(obj);
                return;
            }
            if (obj.size() == 1 && obj.containsKey(JsonLd.LIST)) {
                assert obj.get(JsonLd.LIST).getValueType() == JsonValue.ValueType.ARRAY;
                processValue(obj.getJsonArray(JsonLd.LIST));
                return;
            }
        }
        instanceBuilder.openCollection(property);
        for (JsonValue item : value) {
            if (item.getValueType() == JsonValue.ValueType.OBJECT) {
                resolveValue(item.asJsonObject());
            } else {
                instanceBuilder.addValue(ValueUtils.literalValue(item));
            }
        }
        instanceBuilder.closeCollection();
    }

    private void resolveValue(JsonObject value) {
        final Class<?> targetType = instanceBuilder.getCurrentCollectionElementType();
        if (config.getDeserializers().hasCustomDeserializer(targetType)) {
            instanceBuilder.addValue(deserializeUsingCustomDeserializer(targetType, value));
        } else if (value.size() == 1 && value.containsKey(JsonLd.VALUE)) {
            instanceBuilder.addValue(ValueUtils.literalValue(ValueUtils.getValue(value)));
        } else if (value.size() == 1 && value.containsKey(JsonLd.ID)) {
            handleReferenceNodeInCollection(value, targetType);
        } else if (value.containsKey(JsonLd.LANGUAGE)) {
            assert value.containsKey(JsonLd.VALUE);
            instanceBuilder.addValue(
                    new LangString(ValueUtils.stringValue(ValueUtils.getValue(value)), ValueUtils.stringValue(value.get(JsonLd.LANGUAGE))));
        } else if (instanceBuilder.isCurrentCollectionProperties()) {
            // If we are deserializing an object into @Properties, just extract the identifier and put it into the map
            if (!value.containsKey(JsonLd.ID)) {
                throw new MissingIdentifierException(
                        "Cannot put an object without an identifier into @Properties. Object: " + value);
            }
            instanceBuilder.addValue(URI.create(ValueUtils.stringValue(value.get(JsonLd.ID))));
        } else {
            final Class<?> elementType = instanceBuilder.getCurrentCollectionElementType();
            new ObjectDeserializer(instanceBuilder, config, elementType).processValue(value);
        }
    }

    private void handleReferenceNodeInCollection(JsonObject value, Class<?> targetType) {
        assert value.size() == 1 && value.containsKey(JsonLd.ID);
        final String identifier = ValueUtils.stringValue(value.get(JsonLd.ID));
        if (targetType.isEnum()) {
            instanceBuilder.addValue(DataTypeTransformer.transformIndividualToEnumConstant(identifier,
                                                                                           (Class<? extends Enum>) targetType));
        } else {
            instanceBuilder.addNodeReference(identifier);
        }
    }

    private void resolvePropertyValue(JsonObject value) {
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

    private <T> T deserializeUsingCustomDeserializer(Class<T> targetType, JsonObject value) {
        final DeserializationContext<T> ctx = new DeserializationContext<>(targetType, config.getTargetResolver());
        assert config.getDeserializers().getDeserializer(ctx).isPresent();
        return config.getDeserializers().getDeserializer(ctx).get().deserialize(value, ctx);
    }

    private void extractLiteralValue(JsonObject value) {
        final JsonValue val = value.get(JsonLd.VALUE);
        if (value.containsKey(JsonLd.TYPE)) {
            instanceBuilder
                    .addValue(property, XSDTypeCoercer.coerceType(ValueUtils.stringValue(val), ValueUtils.stringValue(value.get(JsonLd.TYPE))));
        } else if (value.containsKey(JsonLd.LANGUAGE)) {
            instanceBuilder.addValue(property, new LangString(ValueUtils.stringValue(val), ValueUtils.stringValue(value.get(JsonLd.LANGUAGE))));
        } else {
            instanceBuilder.addValue(property, ValueUtils.literalValue(val));
        }
    }

    private void handleSingularReferenceNode(JsonObject value, Class<?> targetType) {
        assert value.size() == 1 && value.containsKey(JsonLd.ID);
        final String identifier = ValueUtils.stringValue(value.get(JsonLd.ID));
        if (targetType.isEnum()) {
            instanceBuilder.addValue(property, DataTypeTransformer.transformIndividualToEnumConstant(identifier,
                                                                                                     (Class<? extends Enum>) targetType));
        } else {
            instanceBuilder.addNodeReference(property, identifier);
        }
    }
}
