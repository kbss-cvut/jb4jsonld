/*
 * JB4JSON-LD
 * Copyright (C) 2023 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.exception.JsonLdSerializationException;
import cz.cvut.kbss.jsonld.serialization.JsonGenerator;

import java.io.IOException;

public abstract class JsonNode {

    private final String name;
    private final boolean valueNode;

    JsonNode() {
        this.name = null;
        this.valueNode = true;
    }

    public JsonNode(String name) {
        this.name = name;
        this.valueNode = name == null;
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

    protected void writeKey(JsonGenerator writer) throws IOException {
        writer.writeFieldName(name);
    }

    abstract protected void writeValue(JsonGenerator writer) throws IOException;

    @Override
    public String toString() {
        return name == null ? "{" : "{\"" + name + "\": ";
    }
}
