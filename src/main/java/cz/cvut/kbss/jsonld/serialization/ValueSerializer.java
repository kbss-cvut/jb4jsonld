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
package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.serialization.model.JsonNode;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Serializes value.
 */
interface ValueSerializer {

    /**
     * Serializes the specified field, returning a list of JSON-LD nodes representing it.
     * <p>
     * The result is a list because maps (e.g. {@link cz.cvut.kbss.jopa.model.annotations.Properties}) cannot be
     * serialized as a single attribute.
     *
     * @param attId Attribute identifier
     * @param value Value to serialize
     * @return Serialization result
     */
    List<JsonNode> serialize(String attId, Object value);
}
