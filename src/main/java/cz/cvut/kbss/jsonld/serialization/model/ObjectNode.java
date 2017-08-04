/**
 * Copyright (C) 2017 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.serialization.JsonGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a JSON object node.
 * <p>
 * I.e. it is a set of key-value pairs, which constitute the state of the object.
 */
public class ObjectNode extends CompositeNode {

    public ObjectNode() {
    }

    public ObjectNode(String name) {
        super(name);
    }

    @Override
    Collection<JsonNode> initItems() {
        return new ArrayList<>();
    }

    @Override
    void writeValue(JsonGenerator writer) throws IOException {
        writer.writeObjectStart();
        items.forEach(child -> child.write(writer));
        writer.writeObjectEnd();
    }

    @Override
    public String toString() {
        return super.toString() + items + "}";
    }
}
