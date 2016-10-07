/**
 * Copyright (C) 2016 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.serialization.model.JsonNode;

/**
 * JSON-LD serializer outputting compacted context-less JSON.
 * <p>
 * This means that context info is not used and all attributes are mapped by their full URIs.
 */
public class CompactedJsonLdSerializer extends JsonLdSerializer {

    public CompactedJsonLdSerializer(JsonGenerator jsonGenerator) {
        super(jsonGenerator);
    }

    @Override
    JsonNode buildJsonTree(Object root) {
        final JsonLdTreeBuilder treeBuilder = new JsonLdTreeBuilder();
        traverser.addVisitor(treeBuilder);
        traverser.traverse(root);
        traverser.removeVisitor(treeBuilder);
        return treeBuilder.getTreeRoot();
    }
}
