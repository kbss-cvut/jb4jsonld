package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.serialization.context.ContextMappingSerializationContextFactory;
import cz.cvut.kbss.jsonld.serialization.context.JsonLdContext;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.traversal.ObjectGraphTraverser;

/**
 * JSON-LD serializer outputting compacted JSON-LD with context.
 */
public class ContextBuildingJsonLdSerializer extends JsonLdSerializer {

    ContextBuildingJsonLdSerializer(JsonGenerator jsonGenerator) {
        super(jsonGenerator);
    }

    ContextBuildingJsonLdSerializer(JsonGenerator jsonGenerator, Configuration configuration) {
        super(jsonGenerator, configuration);
    }

    @Override
    protected JsonNode buildJsonTree(Object root) {
        final JsonLdContext context = new JsonLdContext();
        final ObjectGraphTraverser traverser =
                new ObjectGraphTraverser(new ContextMappingSerializationContextFactory(context));
        traverser.setRequireId(configuration().is(ConfigParam.REQUIRE_ID));
        final JsonLdTreeBuilder treeBuilder =
                new JsonLdTreeBuilder(new ObjectGraphValueSerializers(serializers, traverser));
        traverser.setVisitor(treeBuilder);
        traverser.traverse(root);
        traverser.removeVisitor();
        treeBuilder.getTreeRoot().prependItem(context.getContextNode());
        return treeBuilder.getTreeRoot();
    }
}
