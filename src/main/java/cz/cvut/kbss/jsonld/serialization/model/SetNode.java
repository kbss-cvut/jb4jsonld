/*
 * JB4JSON-LD
 * Copyright (C) 2024 Czech Technical University in Prague
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
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents a node serialized as a simple JSON array.
 * <p>
 * Note that in JSON-LD, the JSON array represents a set and is thus unordered.
 */
public class SetNode extends CollectionNode<Set<JsonNode>> {

    public SetNode() {
    }

    public SetNode(String name) {
        super(name);
    }

    @Override
    Set<JsonNode> initItems() {
        return new LinkedHashSet<>();
    }

    @Override
    protected void writeValue(final JsonGenerator writer) throws IOException {
        writer.writeArrayStart();
        items.forEach(item -> item.write(writer));
        writer.writeArrayEnd();
    }
}
