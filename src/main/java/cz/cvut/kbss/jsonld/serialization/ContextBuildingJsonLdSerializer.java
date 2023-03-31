package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.exception.AmbiguousTermMappingException;
import cz.cvut.kbss.jsonld.serialization.context.JsonLdContext;
import cz.cvut.kbss.jsonld.serialization.context.JsonLdContextFactory;
import cz.cvut.kbss.jsonld.serialization.context.MappingJsonLdContextFactory;
import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.CompositeNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.serializer.LiteralValueSerializers;
import cz.cvut.kbss.jsonld.serialization.serializer.ObjectGraphValueSerializers;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializers;
import cz.cvut.kbss.jsonld.serialization.serializer.context.*;
import cz.cvut.kbss.jsonld.serialization.serializer.context.datetime.ContextBuildingTemporalAmountSerializer;
import cz.cvut.kbss.jsonld.serialization.serializer.context.datetime.ContextBuildingTemporalSerializer;
import cz.cvut.kbss.jsonld.serialization.serializer.datetime.DateSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.ObjectGraphTraverser;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContextFactory;

import java.time.*;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

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
    protected ValueSerializers initSerializers() {
        final ValueSerializer<MultilingualString> mlsSerializer = new ContextBuildingMultilingualStringSerializer();
        final ValueSerializer<Collection<MultilingualString>> mlsColSerializer =
                new ContextBuildingPluralMultilingualStringSerializer();
        final LiteralValueSerializers valueSerializers =
                new LiteralValueSerializers(new ContextBuildingDefaultValueSerializer(mlsSerializer, mlsColSerializer));
        valueSerializers.registerIdentifierSerializer(new ContextBuildingIdentifierSerializer());
        valueSerializers.registerTypesSerializer(new ContextBuildingTypesSerializer());
        valueSerializers.registerIndividualSerializer(new ContextBuildingIndividualSerializer());
        final ContextBuildingTemporalSerializer ts = new ContextBuildingTemporalSerializer();
        valueSerializers.registerSerializer(LocalDate.class, ts);
        // Register the same temporal serializer for each of the types it supports (needed for key-based map access)
        valueSerializers.registerSerializer(LocalDate.class, ts);
        valueSerializers.registerSerializer(LocalTime.class, ts);
        valueSerializers.registerSerializer(OffsetTime.class, ts);
        valueSerializers.registerSerializer(LocalDateTime.class, ts);
        valueSerializers.registerSerializer(OffsetDateTime.class, ts);
        valueSerializers.registerSerializer(ZonedDateTime.class, ts);
        valueSerializers.registerSerializer(Instant.class, ts);
        valueSerializers.registerSerializer(Date.class, new DateSerializer(ts));
        final ContextBuildingTemporalAmountSerializer tas = new ContextBuildingTemporalAmountSerializer();
        valueSerializers.registerSerializer(Duration.class, tas);
        valueSerializers.registerSerializer(Period.class, tas);
        return valueSerializers;
    }

    @Override
    protected JsonNode buildJsonTree(Object root) {
        final JsonLdContextFactory jsonLdContextFactory = new MappingJsonLdContextFactory();
        final JsonLdContext rootContext = jsonLdContextFactory.createJsonLdContext();
        final ObjectGraphTraverser traverser =
                new ObjectGraphTraverser(new SerializationContextFactory(rootContext));
        traverser.setRequireId(configuration().is(ConfigParam.REQUIRE_ID));
        if (root instanceof Collection) {
            return buildObjectWithContextAndGraph(traverser, rootContext, jsonLdContextFactory, (Collection<?>) root);
        }
        final JsonLdTreeBuilder treeBuilder = initTreeBuilder(traverser, jsonLdContextFactory);
        traverser.setVisitor(treeBuilder);
        traverser.traverse(root);
        ensureContextNodeNotPresent(treeBuilder.getTreeRoot(), rootContext.getContextNode());
        treeBuilder.getTreeRoot().prependItem(rootContext.getContextNode());
        return treeBuilder.getTreeRoot();
    }

    private void ensureContextNodeNotPresent(CompositeNode<?> root, JsonNode rootCtx) {
        final Optional<JsonNode> ctxNode =
                root.getItems().stream().filter(n -> JsonLd.CONTEXT.equals(n.getName())).findAny();
        if (ctxNode.isPresent()) {
            throw new AmbiguousTermMappingException(
                    "Unable to build context hierarchy. Attempted to add two root contexts. Original root context: " + rootCtx + ", conflicting: " + ctxNode.get());
        }
    }

    private JsonLdTreeBuilder initTreeBuilder(ObjectGraphTraverser traverser,
                                              JsonLdContextFactory jsonLdContextFactory) {
        final ContextBuildingObjectPropertyValueSerializer opSerializer = new ContextBuildingObjectPropertyValueSerializer(traverser);
        opSerializer.configure(configuration());
        return new JsonLdTreeBuilder(new ObjectGraphValueSerializers(serializers, opSerializer), jsonLdContextFactory);
    }

    private JsonNode buildObjectWithContextAndGraph(ObjectGraphTraverser traverser, JsonLdContext rootContext,
                                                    JsonLdContextFactory jsonLdContextFactory,
                                                    Collection<?> items) {
        final CollectionNode<?> graph = JsonNodeFactory.createCollectionNodeFromArray(JsonLd.GRAPH);
        items.stream().map(item -> {
            final JsonLdTreeBuilder treeBuilder = initTreeBuilder(traverser, jsonLdContextFactory);
            traverser.setVisitor(treeBuilder);
            traverser.traverse(item);
            return treeBuilder.getTreeRoot();
        }).forEach(graph::addItem);
        final ObjectNode result = JsonNodeFactory.createObjectNode();
        result.addItem(rootContext.getContextNode());
        result.addItem(graph);
        return result;
    }
}
