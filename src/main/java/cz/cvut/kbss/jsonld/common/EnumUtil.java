package cz.cvut.kbss.jsonld.common;

import cz.cvut.kbss.jopa.model.annotations.Individual;
import cz.cvut.kbss.jsonld.exception.InvalidEnumMappingException;
import cz.cvut.kbss.jsonld.exception.JsonLdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * Utilities for mapping enum constants.
 */
public class EnumUtil {

    private static final Logger LOG = LoggerFactory.getLogger(EnumUtil.class);

    /**
     * Resolves an individual mapped by the specified enum constant.
     * <p>
     * This method looks for the value of the {@link Individual} annotation.
     *
     * @param value Value to map to individual
     * @return Matching individual identifier
     * @throws InvalidEnumMappingException When no matching individual is found
     */
    public static String resolveMappedIndividual(Enum<?> value) {
        return findMatchingConstant(value.getDeclaringClass(), (e, iri) -> e == value, (e, iri) -> iri).orElseThrow(
                () -> new InvalidEnumMappingException("Missing individual mapping for enum constant " + value));
    }

    /**
     * Finds an enum constant matching the specified filtering predicate and transforms it using the specified mapper.
     * <p>
     * Note that the enum constants are also filtered so that only those annotated with {@link Individual} are
     * accepted.
     *
     * @param enumType Enum class
     * @param filter   BiFilter to apply to the processed enum constants and their individual IRIs
     * @param mapper   BiFunction mapping enum constant and its individual IRI to the required target type
     * @param <T>      Target type
     * @param <E>      Enum type
     * @return Matching enum constant transformed according to the specified mapper. Empty {@code Optional} if no
     * constant of the specified enum type matches the filter predicate
     */
    public static <T, E extends Enum<?>> Optional<T> findMatchingConstant(Class<E> enumType,
                                                                          BiPredicate<E, String> filter,
                                                                          BiFunction<E, String, T> mapper) {
        try {
            for (Field f : enumType.getDeclaredFields()) {
                if (!f.isEnumConstant()) {
                    continue;
                }
                final Individual individual = f.getAnnotation(Individual.class);
                final E constant = (E) f.get(null);
                if (individual == null) {
                    LOG.warn(
                            "Enum constant {} is missing individual mapping, yet it can be used as object property value.",
                            constant);
                    continue;
                }
                if (filter.test(constant, individual.iri())) {
                    return Optional.of(mapper.apply(constant, individual.iri()));
                }
            }
        } catch (IllegalAccessException e) {
            // This should not happen
            throw new JsonLdException("Unable to access enum constant!", e);
        }
        return Optional.empty();
    }
}
