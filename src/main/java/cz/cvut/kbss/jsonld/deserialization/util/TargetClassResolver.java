package cz.cvut.kbss.jsonld.deserialization.util;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jsonld.exception.AmbiguousTargetTypeException;
import cz.cvut.kbss.jsonld.exception.TargetTypeException;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resolves the type of instance into which a JSON-LD object will be deserialized.
 */
public class TargetClassResolver {

    private final TypeMap typeMap;

    public TargetClassResolver(TypeMap typeMap) {
        this.typeMap = typeMap;
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
