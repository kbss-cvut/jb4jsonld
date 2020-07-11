package cz.cvut.kbss.jsonld.deserialization.reference;

import cz.cvut.kbss.jsonld.exception.UnresolvedReferenceException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        final Set<PendingReference> refs = pendingReferences.computeIfAbsent(identifier, (id) -> new HashSet<>());
        refs.add(new PendingReference(targetObject, targetField));
    }

    /**
     * Resolves the pending references by replacing them with the specified full object.
     * <p>
     * This method goes through the pending references and sets the specified {@code referencedObject} on the corresponding target objects.
     *
     * @param identifier       Identifier of the referenced object
     * @param referencedObject The referenced object
     * @throws cz.cvut.kbss.jsonld.exception.TargetTypeException If the {@code referencedObject} cannot be assigned to a target field due to type mismatch
     */
    public void resolveReferences(String identifier, Object referencedObject) {
        // TODO
        // TODO Do not forget that the target field may represent a collection
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
}
