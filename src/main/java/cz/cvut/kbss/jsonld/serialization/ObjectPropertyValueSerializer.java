/**
 * Copyright (C) 2022 Czech Technical University in Prague
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
import cz.cvut.kbss.jsonld.serialization.traversal.ObjectGraphTraverser;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

/**
 * Value serializer for object property values.
 */
public class ObjectPropertyValueSerializer implements ValueSerializer {

    private final ObjectGraphTraverser graphTraverser;

    public ObjectPropertyValueSerializer(ObjectGraphTraverser graphTraverser) {
        this.graphTraverser = graphTraverser;
    }

    @Override
    public JsonNode serialize(Object value, SerializationContext ctx) {
        graphTraverser.traverse(ctx);
        return null;
    }
}
