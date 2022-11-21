package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.serialization.context.ContextMappingSerializationContextFactory;
import cz.cvut.kbss.jsonld.serialization.context.JsonLdContext;
import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.traversal.ObjectGraphTraverser;

import java.util.Collection;

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
        if (root instanceof Collection) {
            return buildObjectWithContextAndGraph(traverser, context, (Collection<?>) root);
        }
        traverser.traverse(root);
        treeBuilder.getTreeRoot().prependItem(context.getContextNode());
        return treeBuilder.getTreeRoot();
    }

    private JsonNode buildObjectWithContextAndGraph(ObjectGraphTraverser traverser, JsonLdContext context,
                                                    Collection<?> items) {
        final CollectionNode<?> graph = JsonNodeFactory.createCollectionNodeFromArray(JsonLd.GRAPH);
        items.stream().map(item -> {
            final JsonLdTreeBuilder treeBuilder =
                    new JsonLdTreeBuilder(new ObjectGraphValueSerializers(serializers, traverser));
            traverser.setVisitor(treeBuilder);
            traverser.traverse(item);
            return treeBuilder.getTreeRoot();
        }).forEach(graph::addItem);
        final ObjectNode result = JsonNodeFactory.createObjectNode();
        result.addItem(context.getContextNode());
        result.addItem(graph);
        return result;
    }
}
