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

import cz.cvut.kbss.jsonld.serialization.JsonGenerator;

import java.io.IOException;

/**
 * Represents a field value that should be serialized as a JSON boolean literal value.
 */
public class BooleanLiteralNode extends LiteralNode<Boolean> {

    public BooleanLiteralNode(Boolean value) {
        super(value);
    }

    public BooleanLiteralNode(String name, Boolean value) {
        super(name, value);
    }

    @Override
    protected void writeValue(JsonGenerator writer) throws IOException {
        writer.writeBoolean(value);
    }
}
