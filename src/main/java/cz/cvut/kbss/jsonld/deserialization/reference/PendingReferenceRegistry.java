/*
 * JB4JSON-LD
 * Copyright (C) 2025 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.deserialization.reference;

import cz.cvut.kbss.jsonld.exception.UnresolvedReferenceException;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Registry of pending references.
 */
public class PendingReferenceRegistry {

    private final Map<String, Set<PendingReference>> pendingReferences = new HashMap<>();

    /**
     * Registers a pending reference with the specified identifier.
     *
     * @param identifier   Reference identifier
     * @param targetObject Object which contains the referring attribute
     * @param targetField  Field representing the target attribute
     */
    public void addPendingReference(String identifier, Object targetObject, Field targetField) {
        assert identifier != null;
        assert targetObject != null;
        assert targetField != null;
        addReference(identifier, new SingularPendingReference(targetObject, targetField));
    }

    private void addReference(String identifier, PendingReference reference) {
        final Set<PendingReference> refs = pendingReferences.computeIfAbsent(identifier, (id) -> new LinkedHashSet<>());
        refs.add(reference);
    }

    /**
     * Registers a pending reference with the specified identifier.
     *
     * @param identifier   Reference identifier
     * @param targetObject Collection referencing the object
     */
    public void addPendingReference(String identifier, Collection targetObject) {
        assert identifier != null;
        assert targetObject != null;
        addReference(identifier, new CollectionPendingReference(targetObject));
    }

    /**
     * Resolves the pending references by replacing them with the specified full object.
     * <p>
     * This method goes through the pending references and sets the specified {@code referencedObject} on the
     * corresponding target objects.
     *
     * @param identifier       Identifier of the referenced object
     * @param referencedObject The referenced object
     * @throws cz.cvut.kbss.jsonld.exception.TargetTypeException If the {@code referencedObject} cannot be assigned to a
     *                                                           target field due to type mismatch
     */
    public void resolveReferences(String identifier, Object referencedObject) {
        assert identifier != null;
        assert referencedObject != null;
        final Set<PendingReference> refs = pendingReferences.remove(identifier);
        if (refs != null) {
            refs.forEach(pr -> pr.apply(referencedObject));
        }
    }

    /**
     * Checks whether any pending unresolved references are left.
     *
     * @throws UnresolvedReferenceException Thrown when pending references exist
     */
    public void verifyNoUnresolvedReferencesExist() {
        if (!pendingReferences.isEmpty()) {
            throw new UnresolvedReferenceException(
                    "There are unresolved references to objects " + pendingReferences.keySet());
        }
    }

    Map<String, Set<PendingReference>> getPendingReferences() {
        return Collections.unmodifiableMap(pendingReferences);
    }
}
