/**
 * Copyright (C) 2020 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.deserialization.util;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jsonld.common.BeanAnnotationProcessor;
import cz.cvut.kbss.jsonld.exception.AmbiguousTargetTypeException;
import cz.cvut.kbss.jsonld.exception.TargetTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resolves the type of instance into which a JSON-LD object will be deserialized.
 */
public class TargetClassResolver {

    private static final Logger LOG = LoggerFactory.getLogger(TargetClassResolver.class);

    private final TypeMap typeMap;

    private final TargetClassResolverConfig config;

    public TargetClassResolver(TypeMap typeMap) {
        this.typeMap = typeMap;
        this.config = new TargetClassResolverConfig();
    }

    public TargetClassResolver(TypeMap typeMap, TargetClassResolverConfig config) {
        this.typeMap = typeMap;
        this.config = config;
    }

    /**
     * Resolves object deserialization target class based on the specified type info.
     *
     * @param <T>           The type of the expected target class
     * @param expectedClass Expected class as specified by deserialization return type of field type
     * @param types         Types of the JSON-LD object to deserialize
     * @return Resolved target class. It has to be a subtype of the {@code expectedClass}
     * @throws TargetTypeException If the resulting candidate is not assignable to the expected class or it cannot be
     *                             determined
     */
    public <T> Class<? extends T> getTargetClass(Class<T> expectedClass, Collection<String> types) {
        if (types.isEmpty() && config.shouldAllowAssumingTargetType()) {
            LOG.trace("Assuming target type to be " + expectedClass);
            return expectedClass;
        }
        final List<Class<?>> candidates = getTargetClassCandidates(types);
        final Class<?> targetCandidate;
        reduceTargetClassCandidates(expectedClass, candidates);
        final List<Class<?>> reducedCandidates = new ArrayList<>(candidates);
        reduceToMostSpecificSubclasses(candidates);
        if (candidates.isEmpty()) {
            if (doesExpectedClassMatchesTypes(expectedClass, types)) {
                targetCandidate = expectedClass;
            } else {
                throw new TargetTypeException(
                        "Neither " + expectedClass + " nor any of its subclasses matches the types " + types + ".");
            }
        } else {
            targetCandidate = selectFinalTargetClass(candidates, reducedCandidates, types);
        }
        assert expectedClass.isAssignableFrom(targetCandidate);
        return (Class<? extends T>) targetCandidate;
    }

    private List<Class<?>> getTargetClassCandidates(Collection<String> types) {
        return types.stream().flatMap(t -> typeMap.get(t).stream()).collect(Collectors.toList());
    }

    private void reduceTargetClassCandidates(Class<?> expectedClass, List<Class<?>> candidates) {
        candidates.removeIf(c -> !expectedClass.isAssignableFrom(c) || Modifier.isAbstract(c.getModifiers()));
    }

    private void reduceToMostSpecificSubclasses(List<Class<?>> candidates) {
        candidates.removeIf(cls -> candidates.stream().anyMatch(c -> !cls.equals(c) && cls.isAssignableFrom(c)));
    }

    private Class<?> selectFinalTargetClass(List<Class<?>> mostSpecificCandidates, List<Class<?>> candidates,
                                            Collection<String> types) {
        assert mostSpecificCandidates.size() > 0;
        if (mostSpecificCandidates.size() > 1) {
            if (!config.isOptimisticTypeResolutionEnabled()) {
                throw ambiguousTargetType(types, mostSpecificCandidates);
            }
            if (config.shouldPreferSuperclass()) {
                return selectTargetClassWithSuperclassPreference(mostSpecificCandidates, candidates);
            }
        }
        return pickOne(mostSpecificCandidates);
    }

    private static Class<?> pickOne(List<Class<?>> candidates) {
        return candidates.size() == 1 ? candidates.get(0) :
               candidates.stream().filter(BeanAnnotationProcessor::hasPropertiesField).findFirst()
                         .orElse(candidates.get(0));
    }

    private Class<?> selectTargetClassWithSuperclassPreference(List<Class<?>> mostSpecificCandidates,
                                                               List<Class<?>> candidates) {
        candidates.removeAll(mostSpecificCandidates);
        reduceToMostGeneralSuperclasses(candidates);
        return pickOne(candidates);
    }

    private void reduceToMostGeneralSuperclasses(List<Class<?>> candidates) {
        candidates.removeIf(cls -> candidates.stream().anyMatch(c -> !cls.equals(c) && c.isAssignableFrom(cls)));
    }

    private static AmbiguousTargetTypeException ambiguousTargetType(Collection<String> types,
                                                                    List<Class<?>> candidates) {
        return new AmbiguousTargetTypeException(
                "Object with types " + types + " matches multiple equivalent target classes: " + candidates);
    }

    private boolean doesExpectedClassMatchesTypes(Class<?> expectedClass, Collection<String> types) {
        final OWLClass owlClass = expectedClass.getDeclaredAnnotation(OWLClass.class);
        return owlClass != null && types.contains(owlClass.iri());
    }
}
