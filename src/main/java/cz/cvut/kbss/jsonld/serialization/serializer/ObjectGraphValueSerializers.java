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
package cz.cvut.kbss.jsonld.serialization.serializer;

import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.ObjectPropertyValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Manager of value serializers aware of object graph traversal.
 * <p>
 * A new instance of this should be created every time a new object graph is being serialized.
 */
public class ObjectGraphValueSerializers implements ValueSerializers {

    private final ValueSerializers serializers;

    private final ObjectPropertyValueSerializer opSerializer;

    public ObjectGraphValueSerializers(ValueSerializers serializers, ObjectPropertyValueSerializer opSerializer) {
        this.serializers = Objects.requireNonNull(serializers);
        this.opSerializer = Objects.requireNonNull(opSerializer);
    }

    @Override
    public <T> boolean hasCustomSerializer(Class<T> type) {
        return serializers.hasCustomSerializer(type);
    }

    @Override
    public <T> Optional<ValueSerializer<T>> getSerializer(SerializationContext<T> ctx) {
        final Optional<ValueSerializer<T>> result = serializers.getSerializer(ctx);
        return result.isPresent() ? result :
               (BeanAnnotationProcessor.isObjectProperty(ctx.getField()) ? Optional.of(opSerializer) :
                Optional.empty());
    }

    @Override
    public <T> ValueSerializer<T> getOrDefault(SerializationContext<T> ctx) {
        final Optional<ValueSerializer<T>> result = serializers.getSerializer(ctx);
        return result.orElseGet(() -> (BeanAnnotationProcessor.isObjectProperty(ctx.getField()) ? opSerializer :
                                       serializers.getOrDefault(ctx)));
    }

    @Override
    public <T> void registerSerializer(Class<T> forType, ValueSerializer<? super T> serializer) {
        serializers.registerSerializer(forType, serializer);
    }

    @Override
    public ValueSerializer<String> getIdentifierSerializer() {
        return serializers.getIdentifierSerializer();
    }

    @Override
    public void registerIdentifierSerializer(ValueSerializer<String> idSerializer) {
        serializers.registerIdentifierSerializer(idSerializer);
    }

    @Override
    public ValueSerializer<Set<String>> getTypesSerializer() {
        return serializers.getTypesSerializer();
    }

    @Override
    public void registerTypesSerializer(ValueSerializer<Set<String>> typesSerializer) {
        serializers.registerTypesSerializer(typesSerializer);
    }
}
