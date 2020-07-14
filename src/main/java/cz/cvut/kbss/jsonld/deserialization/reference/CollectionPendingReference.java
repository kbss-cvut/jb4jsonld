package cz.cvut.kbss.jsonld.deserialization.reference;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents a collection-based pending reference.
 * <p>
 * That is, a collection containing a reference to an object.
 */
public class CollectionPendingReference implements PendingReference {

    private final Collection targetCollection;

    public CollectionPendingReference(Collection targetCollection) {
        this.targetCollection = Objects.requireNonNull(targetCollection);
    }

    @Override
    public void apply(Object referencedObject) {
        assert referencedObject != null;
        // Note, that this will not preserve ordering in Lists. If it becomes a problem, we'll need to provide a specialized
        // version for lists where a placeholder would be put into the list and replaced by the referenced object later
        // to ensure correct order
        targetCollection.add(referencedObject);
    }
}
