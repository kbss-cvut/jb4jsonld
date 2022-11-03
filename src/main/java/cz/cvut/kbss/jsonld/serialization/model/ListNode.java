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
    void writeValue(final JsonGenerator writer) throws IOException {
        writer.writeObjectStart();
        writer.writeFieldName(JsonLd.LIST);
        writer.writeArrayStart();
        items.forEach(item -> item.write(writer));
        writer.writeArrayEnd();
        writer.writeObjectEnd();
    }
}
