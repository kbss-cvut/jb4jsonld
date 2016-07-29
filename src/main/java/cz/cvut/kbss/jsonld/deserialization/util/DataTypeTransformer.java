package cz.cvut.kbss.jsonld.deserialization.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

/**
 * Provides transformations of values to various target types.
 */
public class DataTypeTransformer {

    private static Map<TransformationRuleIdentifier<?, ?>, Function> rules = new HashMap<>();

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

    public static Object transformValue(Object value, Class<?> targetClass) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(targetClass);
        final Class<?> sourceClass = value.getClass();
        final TransformationRuleIdentifier<?, ?> identifier = new TransformationRuleIdentifier<>(sourceClass,
                targetClass);
        return rules.containsKey(identifier) ? rules.get(identifier).apply(value) : null;
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

            if (!sourceType.equals(that.sourceType)) return false;
            return targetType.equals(that.targetType);

        }

        @Override
        public int hashCode() {
            int result = sourceType.hashCode();
            result = 31 * result + targetType.hashCode();
            return result;
        }
    }
}
