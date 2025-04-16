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

import java.util.Optional;

/**
 * Represents a pending reference.
 * <p>
 * A pending reference represents a situation when the JSON-LD contains just an object with and {@code @id}, while the
 * mapped attribute expects a full-blown object. In this case, it is expected that somewhere in the JSON-LD, there is
 * the object's full serialization and this is only a reference to it.
 */
public interface PendingReference {

    /**
     * Applies the specified referenced object to this pending reference, resolving it.
     * <p>
     * Resolving the reference basically means inserting the referenced object into the place represented by this
     * instance (e.g., field value, collection).
     *
     * @param referencedObject The object referenced by this pending reference
     */
    void apply(Object referencedObject);

    /**
     * Gets the target type of the pending reference.
     * <p>
     * If the target type cannot be reliably determined (for instance, because it is a collection), this method should
     * return {@link Optional#empty()}.
     *
     * @return The target type
     */
    default Optional<Class<?>> getTargetType() {
        return Optional.empty();
    }
}
