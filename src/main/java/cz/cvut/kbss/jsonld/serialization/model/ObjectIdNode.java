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
package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.serialization.JsonGenerator;

import java.io.IOException;
import java.util.Objects;

/**
 * Represents a field value that should be serialized as an identifier of the referenced object.
 */
public class ObjectIdNode extends JsonNode {

    private final String identifier;

    public ObjectIdNode(String identifier) {
        this.identifier = Objects.requireNonNull(identifier);
    }

    public ObjectIdNode(String name, String identifier) {
        super(name);
        this.identifier = Objects.requireNonNull(identifier);
    }

    @Override
    protected void writeValue(JsonGenerator writer) throws IOException {
        writer.writeString(identifier);
    }

    @Override
    public String toString() {
        return super.toString() + identifier + "}";
    }
}
