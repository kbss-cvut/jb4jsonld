/**
 * Copyright (C) 2020 Czech Technical University in Prague
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

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jsonld.exception.AmbiguousTargetTypeException;
import cz.cvut.kbss.jsonld.exception.TargetTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resolves the type of instance into which a JSON-LD object will be deserialized.
 */
public class TargetClassResolver {

    private static final Logger LOG = LoggerFactory.getLogger(TargetClassResolver.class);

    private final TypeMap typeMap;

    private final boolean allowAssumingTargetType;

    public TargetClassResolver(TypeMap typeMap) {
        this.typeMap = typeMap;
        this.allowAssumingTargetType = false;
    }

    public TargetClassResolver(TypeMap typeMap, boolean allowAssumingTargetType) {
        this.typeMap = typeMap;
        this.allowAssumingTargetType = allowAssumingTargetType;
    }

    /**
     * Resolves object deserialization target class based on the specified type info.
     *
     * @param expectedClass Expected class as specified by deserialization return type of field type
     * @param types         Types of the JSON-LD object to deserialize
     * @return Resolved target class. It has to be a subtype of the {@code expectedClass}
     * @throws TargetTypeException If the resulting candidate is not assignable to the expected class
     */
    public <T> Class<? extends T> getTargetClass(Class<T> expectedClass, Collection<String> types) {
        if (types.isEmpty() && allowAssumingTargetType) {
            LOG.trace("Assuming target type to be " + expectedClass);
            return expectedClass;
        }
        final List<Class<?>> candidates = getTargetClassCandidates(types);
        final Class<?> targetCandidate;
        reduceTargetClassCandidates(expectedClass, candidates);
        ensureSingleCandidateClassRemains(types, candidates);
        if (candidates.isEmpty()) {
            if (doesExpectedClassMatchesTypes(expectedClass, types)) {
                targetCandidate = expectedClass;
            } else {
                throw new TargetTypeException(
                        "Neither " + expectedClass + " nor any of its subclasses matches the types " + types + ".");
            }
        } else {
            targetCandidate = candidates.get(0);
        }
        assert expectedClass.isAssignableFrom(targetCandidate);
        return (Class<? extends T>) targetCandidate;
    }

    private List<Class<?>> getTargetClassCandidates(Collection<String> types) {
        return types.stream().flatMap(t -> typeMap.get(t).stream()).collect(Collectors.toList());
    }

    private void reduceTargetClassCandidates(Class<?> expectedClass, List<Class<?>> candidates) {
        candidates.removeIf(c -> !expectedClass.isAssignableFrom(c));
        if (candidates.size() == 1) {
            return;
        }
        reduceToMostSpecificSubclasses(candidates);
    }

    private void reduceToMostSpecificSubclasses(List<Class<?>> candidates) {
        candidates.removeIf(cls -> candidates.stream().anyMatch(c -> !cls.equals(c) && cls.isAssignableFrom(c)));
    }

    private void ensureSingleCandidateClassRemains(Collection<String> types, List<Class<?>> candidates) {
        if (candidates.size() > 1) {
            throw new AmbiguousTargetTypeException(
                    "Object with types " + types + " matches multiple equivalent target classes: " + candidates);
        }
    }

    private boolean doesExpectedClassMatchesTypes(Class<?> expectedClass, Collection<String> types) {
        final OWLClass owlClass = expectedClass.getDeclaredAnnotation(OWLClass.class);
        return owlClass != null && types.contains(owlClass.iri());
    }
}
