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
package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.serialization.context.DummyJsonLdContext;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.LiteralValueSerializers;
import cz.cvut.kbss.jsonld.serialization.serializer.ObjectGraphValueSerializers;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializers;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.*;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime.TemporalAmountSerializer;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.datetime.TemporalSerializer;
import cz.cvut.kbss.jsonld.serialization.serializer.datetime.DateSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.ObjectGraphTraverser;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContextFactory;

import java.time.*;
import java.util.Date;

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
    protected ValueSerializers initSerializers() {
        final LiteralValueSerializers valueSerializers =
                new LiteralValueSerializers(new DefaultValueSerializer(new MultilingualStringSerializer()));
        valueSerializers.registerIdentifierSerializer(new IdentifierSerializer());
        valueSerializers.registerTypesSerializer(new TypesSerializer());
        valueSerializers.registerIndividualSerializer(new IndividualSerializer());
        final TemporalSerializer ts = new TemporalSerializer();
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
        final TemporalAmountSerializer tas = new TemporalAmountSerializer();
        valueSerializers.registerSerializer(Duration.class, tas);
        valueSerializers.registerSerializer(Period.class, tas);
        return valueSerializers;
    }

    @Override
    protected JsonNode buildJsonTree(Object root) {
        final ObjectGraphTraverser traverser = new ObjectGraphTraverser(new SerializationContextFactory(
                DummyJsonLdContext.INSTANCE));
        traverser.setRequireId(configuration().is(ConfigParam.REQUIRE_ID));
        final JsonLdTreeBuilder treeBuilder = initTreeBuilder(traverser);
        traverser.setVisitor(treeBuilder);
        traverser.traverse(root);
        return treeBuilder.getTreeRoot();
    }

    private JsonLdTreeBuilder initTreeBuilder(ObjectGraphTraverser traverser) {
        final ObjectPropertyValueSerializer opSerializer = new ObjectPropertyValueSerializer(traverser);
        opSerializer.configure(configuration());
        return new JsonLdTreeBuilder(new ObjectGraphValueSerializers(serializers, opSerializer),
                                     DummyJsonLdContext.INSTANCE);
    }
}
