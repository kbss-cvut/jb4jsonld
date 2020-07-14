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
package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.exception.JsonLdSerializationException;
import cz.cvut.kbss.jsonld.serialization.JsonGenerator;

import java.io.IOException;
import java.util.Objects;

public abstract class JsonNode {

    private final String name;
    private final boolean valueNode;

    JsonNode() {
        this.name = null;
        this.valueNode = true;
    }

    public JsonNode(String name) {
        this.name = Objects.requireNonNull(name);
        this.valueNode = false;
    }

    public String getName() {
        return name;
    }

    public boolean isValueNode() {
        return valueNode;
    }

    public void write(JsonGenerator writer) {
        try {
            if (!valueNode) {
                writeKey(writer);
            }
            writeValue(writer);
        } catch (IOException e) {
            throw new JsonLdSerializationException("Exception during serialization of node " + this, e);
        }
    }

    void writeKey(JsonGenerator writer) throws IOException {
        writer.writeFieldName(name);
    }

    abstract void writeValue(JsonGenerator writer) throws IOException;

    @Override
    public String toString() {
        return name == null ? "{" : "{\"" + name + "\": ";
    }
}
