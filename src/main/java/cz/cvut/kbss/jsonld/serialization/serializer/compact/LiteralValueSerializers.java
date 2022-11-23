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
package cz.cvut.kbss.jsonld.serialization.serializer.compact;

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.serialization.JsonLdSerializer;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializers;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime.TemporalAmountSerializer;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime.TemporalSerializer;
import cz.cvut.kbss.jsonld.serialization.serializer.datetime.DateSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.*;
import java.util.*;

/**
 * Manages serializers for a single {@link JsonLdSerializer} instance.
 */
public class LiteralValueSerializers implements ValueSerializers {

    private final Map<Class<?>, ValueSerializer<?>> serializers = new HashMap<>();

    private final ValueSerializer<?> defaultSerializer = new DefaultValueSerializer(new MultilingualStringSerializer());

    public LiteralValueSerializers() {
        initBuiltInSerializers();
    }

    private void initBuiltInSerializers() {
        final TemporalSerializer ts = new TemporalSerializer();
        // Register the same temporal serializer for each of the types it supports (needed for key-based map access)
        serializers.put(LocalDate.class, ts);
        serializers.put(LocalTime.class, ts);
        serializers.put(OffsetTime.class, ts);
        serializers.put(LocalDateTime.class, ts);
        serializers.put(OffsetDateTime.class, ts);
        serializers.put(ZonedDateTime.class, ts);
        serializers.put(Instant.class, ts);
        serializers.put(Date.class, new DateSerializer(ts));
        final TemporalAmountSerializer tas = new TemporalAmountSerializer();
        serializers.put(Duration.class, tas);
        serializers.put(Period.class, tas);
    }

    @Override
    public <T> boolean hasCustomSerializer(Class<T> type) {
        return serializers.containsKey(type);
    }

    @Override
    public <T> Optional<ValueSerializer<T>> getSerializer(SerializationContext<T> ctx) {
        return Optional.ofNullable((ValueSerializer<T>) serializers.get(ctx.getValue().getClass()));
    }

    @Override
    public <T> ValueSerializer<T> getOrDefault(SerializationContext<T> ctx) {
        return (ValueSerializer<T>) serializers.getOrDefault(ctx.getValue().getClass(), defaultSerializer);
    }

    @Override
    public <T> void registerSerializer(Class<T> forType, ValueSerializer<T> serializer) {
        Objects.requireNonNull(forType);
        Objects.requireNonNull(serializer);
        serializers.put(forType, serializer);
    }

    @Override
    public void configure(Configuration configuration) {
        serializers.values().forEach(vs -> vs.configure(configuration));
    }

    /**
     * Serializes the specified value as a JSON object with value ({@link JsonLd#VALUE}) and type ({@link
     * JsonLd#TYPE}).
     *
     * @param term  Term to identify the object in the enclosing object
     * @param value Value to serialize
     * @param type  Value type to use
     * @return Resulting JSON node
     */
    public static JsonNode serializeValueWithType(String term, String value, String type) {
        final ObjectNode node = JsonNodeFactory.createObjectNode(term);
        node.addItem(JsonNodeFactory.createLiteralNode(JsonLd.TYPE, type));
        node.addItem(JsonNodeFactory.createLiteralNode(JsonLd.VALUE, value));
        return node;
    }
}
