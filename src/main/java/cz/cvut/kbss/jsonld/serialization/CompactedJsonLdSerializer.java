/**
 * Copyright (C) 2022 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.serialization.context.DummyJsonLdContext;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.ObjectGraphValueSerializers;
import cz.cvut.kbss.jsonld.serialization.traversal.ObjectGraphTraverser;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContextFactory;

/**
 * JSON-LD serializer outputting compacted context-less JSON-LD.
 * <p>
 * This means that context info is not used and all attributes are mapped by their full URIs.
 */
public class CompactedJsonLdSerializer extends JsonLdSerializer {

    public CompactedJsonLdSerializer(JsonGenerator jsonGenerator) {
        super(jsonGenerator);
    }

    public CompactedJsonLdSerializer(JsonGenerator jsonGenerator, Configuration configuration) {
        super(jsonGenerator, configuration);
    }

    @Override
    protected JsonNode buildJsonTree(Object root) {
        final ObjectGraphTraverser traverser = new ObjectGraphTraverser(new SerializationContextFactory(
                DummyJsonLdContext.INSTANCE));
        traverser.setRequireId(configuration().is(ConfigParam.REQUIRE_ID));
        final JsonLdTreeBuilder treeBuilder =
                new JsonLdTreeBuilder(new ObjectGraphValueSerializers(serializers, traverser));
        traverser.setVisitor(treeBuilder);
        traverser.traverse(root);
        return treeBuilder.getTreeRoot();
    }
}
