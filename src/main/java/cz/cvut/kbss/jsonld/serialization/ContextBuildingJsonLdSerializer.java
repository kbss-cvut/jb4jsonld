package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.serialization.context.MappingJsonLdContext;
import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
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
        final MappingJsonLdContext context = new MappingJsonLdContext();
        final ObjectGraphTraverser traverser =
                new ObjectGraphTraverser(new SerializationContextFactory(context));
        traverser.setRequireId(configuration().is(ConfigParam.REQUIRE_ID));
        if (root instanceof Collection) {
            return buildObjectWithContextAndGraph(traverser, context, (Collection<?>) root);
        }
        final JsonLdTreeBuilder treeBuilder = initTreeBuilder(traverser);
        traverser.setVisitor(treeBuilder);
        traverser.traverse(root);
        treeBuilder.getTreeRoot().prependItem(context.getContextNode());
        return treeBuilder.getTreeRoot();
    }

    private JsonLdTreeBuilder initTreeBuilder(ObjectGraphTraverser traverser) {
        return new JsonLdTreeBuilder(new ObjectGraphValueSerializers(serializers,
                                                                     new ContextBuildingObjectPropertyValueSerializer(
                                                                             traverser)));
    }

    private JsonNode buildObjectWithContextAndGraph(ObjectGraphTraverser traverser, MappingJsonLdContext context,
                                                    Collection<?> items) {
        final CollectionNode<?> graph = JsonNodeFactory.createCollectionNodeFromArray(JsonLd.GRAPH);
        items.stream().map(item -> {
            final JsonLdTreeBuilder treeBuilder = initTreeBuilder(traverser);
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
