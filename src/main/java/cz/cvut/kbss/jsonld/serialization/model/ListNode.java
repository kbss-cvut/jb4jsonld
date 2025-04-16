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
package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.serialization.JsonGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node serialized as a JSON-LD list.
 * <p>
 * I.e., it is serialized as an object with a single attribute - {@code @list} and its value is a JSON array.
 */
public class ListNode extends CollectionNode<List<JsonNode>> {

    public ListNode() {
    }

    public ListNode(String name) {
        super(name);
    }

    @Override
    List<JsonNode> initItems() {
        return new ArrayList<>();
    }

    @Override
    protected void writeValue(final JsonGenerator writer) throws IOException {
        writer.writeObjectStart();
        writer.writeFieldName(JsonLd.LIST);
        writer.writeArrayStart();
        items.forEach(item -> item.write(writer));
        writer.writeArrayEnd();
        writer.writeObjectEnd();
    }
}
