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
