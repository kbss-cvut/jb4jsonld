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
package cz.cvut.kbss.jsonld.deserialization.util;

import cz.cvut.kbss.jopa.datatype.DatatypeTransformer;
import cz.cvut.kbss.jopa.datatype.util.Pair;
import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jsonld.common.EnumUtil;
import cz.cvut.kbss.jsonld.exception.InvalidEnumMappingException;

import java.time.*;
import java.util.*;
import java.util.function.Function;

/**
 * Provides transformation of values to various target types.
 */
public class DataTypeTransformer {

    private static final Map<Pair, Function<Object, ?>> CUSTOM_TRANSFORMERS = initCustomTransformers();

    private static Map<Pair, Function<Object, ?>> initCustomTransformers() {
        final Map<Pair, Function<Object, ?>> map = new HashMap<>();
        map.put(new Pair<>(LangString.class, MultilingualString.class), src -> {
            final LangString ls = (LangString) src;
            return new MultilingualString(Collections.singletonMap(ls.getLanguage().orElse(null), ls.getValue()));
        });
        map.put(new Pair<>(String.class, MultilingualString.class),
                src -> new MultilingualString(Collections.singletonMap(null, src.toString())));
        map.put(new Pair<>(OffsetDateTime.class, LocalDateTime.class), src -> ((OffsetDateTime) src).toLocalDateTime());
        map.put(new Pair<>(OffsetDateTime.class, ZonedDateTime.class), src -> ((OffsetDateTime) src).toZonedDateTime());
        map.put(new Pair<>(OffsetDateTime.class, Instant.class), src -> ((OffsetDateTime) src).toInstant());
        map.put(new Pair<>(OffsetDateTime.class, Date.class), src -> Date.from(((OffsetDateTime) src).toInstant()));
        map.put(new Pair<>(OffsetTime.class, LocalTime.class), src -> ((OffsetTime) src).toLocalTime());
        return map;
    }

    public static <T> T transformValue(Object value, Class<T> targetClass) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(targetClass);
        final Class<?> sourceClass = value.getClass();
        if (targetClass.isAssignableFrom(sourceClass)) {
            return targetClass.cast(value);
        }
        if (targetClass.isEnum()) {
            return (T) transformLiteralToEnumConstant(value, (Class) targetClass);
        }
        final Pair<Class<?>, Class<?>> key = new Pair<>(sourceClass, targetClass);
        if (CUSTOM_TRANSFORMERS.containsKey(key)) {
            return targetClass.cast(CUSTOM_TRANSFORMERS.get(key).apply(value));
        }
        return DatatypeTransformer.transform(value, targetClass);
    }

    private static <T extends Enum<T>> T transformLiteralToEnumConstant(Object value, Class<T> targetClass) {
        return Enum.valueOf(targetClass, value.toString());
    }

    /**
     * Transforms the specified individual identifier to the corresponding enum constant.
     * <p>
     * This transformation uses the {@link cz.cvut.kbss.jopa.model.annotations.Individual} mapping of enum constants.
     *
     * @param identifier  Individual identifier
     * @param targetClass Target enum
     * @param <T>         Enum type
     * @return Matching enum constant
     * @throws InvalidEnumMappingException When no matching enum constant is found for the specified identifier
     */
    public static <T extends Enum<T>> T transformIndividualToEnumConstant(String identifier, Class<T> targetClass) {
        return EnumUtil.findMatchingConstant(targetClass, (e, iri) -> iri.equals(identifier), (e, iri) -> e)
                       .orElseThrow(() -> new InvalidEnumMappingException(
                               "No matching constant found for individual <" + identifier + "> in target class " + targetClass));
    }
}
