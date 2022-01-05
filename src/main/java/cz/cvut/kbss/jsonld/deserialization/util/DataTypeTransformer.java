/**
 * Copyright (C) 2022 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.deserialization.util;

import cz.cvut.kbss.jopa.model.MultilingualString;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;

/**
 * Provides transformations of values to various target types.
 */
public class DataTypeTransformer {

    private static final Map<TransformationRuleIdentifier<?, ?>, Function> rules = new HashMap<>();

    static {
        rules.put(new TransformationRuleIdentifier<>(String.class, URI.class), (src) -> URI.create(src.toString()));
        rules.put(new TransformationRuleIdentifier<>(String.class, URL.class), (src) -> {
            try {
                return new URL(src.toString());
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Invalid URL " + src, e);
            }
        });
        rules.put(new TransformationRuleIdentifier<>(String.class, Date.class), (src) -> {
            try {
                // This format corresponds to the one produced by java.util.Date#toString()
                return new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US).parse(src.toString());
            } catch (ParseException e) {
                throw new IllegalArgumentException("Unable to parse date " + src, e);
            }
        });
        rules.put(new TransformationRuleIdentifier<>(Integer.class, Long.class), (src) -> ((Integer) src).longValue());
        rules.put(new TransformationRuleIdentifier<>(Integer.class, Float.class),
                (src) -> ((Integer) src).floatValue());
        rules.put(new TransformationRuleIdentifier<>(Integer.class, Double.class),
                (src) -> ((Integer) src).doubleValue());
        rules.put(new TransformationRuleIdentifier<>(Long.class, Float.class), (src) -> ((Long) src).floatValue());
        rules.put(new TransformationRuleIdentifier<>(Long.class, Double.class), (src) -> ((Long) src).doubleValue());
        rules.put(new TransformationRuleIdentifier<>(Long.class, Date.class), (src) -> new Date((Long) src));
        rules.put(new TransformationRuleIdentifier<>(ZonedDateTime.class, LocalDateTime.class),
                src -> ((ZonedDateTime) src).toLocalDateTime());
        rules.put(new TransformationRuleIdentifier<>(LangString.class, MultilingualString.class), src -> {
            final LangString ls = (LangString) src;
            return new MultilingualString(Collections.singletonMap(ls.getLanguage(), ls.getValue()));
        });
        rules.put(new TransformationRuleIdentifier<>(String.class, MultilingualString.class),
                src -> new MultilingualString(Collections.singletonMap(null, src.toString())));
    }

    /**
     * Registers transformation rule for the specified source and target types.
     * <p>
     * Overrides any previously defined rule for the source and target classes.
     *
     * @param sourceClass Source class
     * @param targetClass Target class
     * @param rule        The rule to apply
     * @param <T>         Source type
     * @param <R>         Target type
     */
    public static <T, R> void registerTransformationRule(Class<T> sourceClass, Class<R> targetClass,
                                                         Function<T, R> rule) {
        Objects.requireNonNull(sourceClass);
        Objects.requireNonNull(targetClass);
        Objects.requireNonNull(rule);
        rules.put(new TransformationRuleIdentifier<>(sourceClass, targetClass), rule);
    }

    public static <T> T transformValue(Object value, Class<T> targetClass) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(targetClass);
        final Class<?> sourceClass = value.getClass();
        if (targetClass.isAssignableFrom(sourceClass)) {
            return targetClass.cast(value);
        }
        if (targetClass.isEnum()) {
            return (T) transformToEnumConstant(value, (Class<? extends Enum>) targetClass);
        }
        if (targetClass.equals(String.class)) {
            return targetClass.cast(value.toString());
        }
        final TransformationRuleIdentifier<?, ?> identifier = new TransformationRuleIdentifier<>(sourceClass,
                targetClass);
        return rules.containsKey(identifier) ? (T) rules.get(identifier).apply(value) : null;
    }

    private static <T extends Enum<T>> T transformToEnumConstant(Object value, Class<T> targetClass) {
        return Enum.valueOf(targetClass, value.toString());
    }

    public static class TransformationRuleIdentifier<S, T> {
        private final Class<S> sourceType;
        private final Class<T> targetType;

        public TransformationRuleIdentifier(Class<S> sourceType, Class<T> targetType) {
            this.sourceType = Objects.requireNonNull(sourceType);
            this.targetType = Objects.requireNonNull(targetType);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TransformationRuleIdentifier<?, ?> that = (TransformationRuleIdentifier<?, ?>) o;

            return sourceType.equals(that.sourceType) && targetType.equals(that.targetType);

        }

        @Override
        public int hashCode() {
            int result = sourceType.hashCode();
            result = 31 * result + targetType.hashCode();
            return result;
        }
    }
}
