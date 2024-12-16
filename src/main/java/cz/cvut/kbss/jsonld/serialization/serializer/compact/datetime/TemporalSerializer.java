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
package cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.exception.UnsupportedTemporalTypeException;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.serializer.datetime.DateTimeSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.List;

/**
 * Serializes Java 8 date/time values represented by the {@link TemporalAccessor} interface.
 */
public class TemporalSerializer implements ValueSerializer<TemporalAccessor> {

    protected DateTimeSerializer dateTimeSerializer;
    private final LocalDateSerializer dateSerializer;
    private final TimeSerializer timeSerializer;

    public TemporalSerializer() {
        this(new IsoDateTimeSerializer(), new LocalDateSerializer(), new TimeSerializer());
    }

    protected TemporalSerializer(DateTimeSerializer dateTimeSerializer, LocalDateSerializer dateSerializer,
                                 TimeSerializer timeSerializer) {
        this.dateTimeSerializer = dateTimeSerializer;
        this.dateSerializer = dateSerializer;
        this.timeSerializer = timeSerializer;
    }

    @Override
    public JsonNode serialize(TemporalAccessor value, SerializationContext<TemporalAccessor> ctx) {
        if (value instanceof LocalDate) {
            return dateSerializer.serialize((LocalDate) value, ctx);
        } else if (value instanceof OffsetTime) {
            return timeSerializer.serialize((OffsetTime) value, ctx);
        } else if (value instanceof LocalTime) {
            return timeSerializer.serialize((LocalTime) value, ctx);
        } else if (value instanceof OffsetDateTime) {
            return dateTimeSerializer.serialize((OffsetDateTime) value, ctx);
        } else if (value instanceof LocalDateTime) {
            return dateTimeSerializer.serialize((LocalDateTime) value, ctx);
        } else if (value instanceof Instant) {
            return dateTimeSerializer.serialize((Instant) value, ctx);
        } else if (value instanceof ZonedDateTime) {
            return dateTimeSerializer.serialize(((ZonedDateTime) value).toOffsetDateTime(), ctx);
        }
        throw new UnsupportedTemporalTypeException(
                "Temporal type " + value.getClass() + " serialization is not supported.");
    }

    @Override
    public void configure(Configuration config) {
        assert config != null;
        this.dateTimeSerializer = config.is(ConfigParam.SERIALIZE_DATETIME_AS_MILLIS) ?
                                  new EpochBasedDateTimeSerializer() : new IsoDateTimeSerializer();
        dateTimeSerializer.configure(config);
    }

    /**
     * Gets the Java types supported by this serializer.
     *
     * @return List of Java classes
     */
    public static List<Class<? extends TemporalAccessor>> getSupportedTypes() {
        return List.of(LocalDate.class, OffsetTime.class, LocalTime.class, OffsetDateTime.class, LocalDateTime.class,
                       Instant.class, ZonedDateTime.class);
    }
}
