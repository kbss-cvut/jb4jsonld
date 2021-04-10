/**
 * Copyright (C) 2020 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

/**
 * Serializes values.
 */
@FunctionalInterface
public interface ValueSerializer<T> {

    /**
     * Serializes the specified value, returning a JSON-LD node representing it.
     * <p>
     * Note that if the value is a singular, the returned node should also contain the identifier of the serialized attribute (available
     * through the provided serialization context).
     *
     * @param value Value to serialize
     * @param ctx   Serialization context
     * @return Serialization result
     */
    JsonNode serialize(T value, SerializationContext<T> ctx);
}
