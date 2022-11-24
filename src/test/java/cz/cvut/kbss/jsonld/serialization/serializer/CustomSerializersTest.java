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
package cz.cvut.kbss.jsonld.serialization.serializer;

import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Study;
import cz.cvut.kbss.jsonld.serialization.JsonLdTreeBuilder;
import cz.cvut.kbss.jsonld.serialization.JsonLdTreeBuilderTest;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.context.DummyJsonLdContext;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.DefaultValueSerializer;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.MultilingualStringSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomSerializersTest {

    private ValueSerializers serializers;

    private JsonLdTreeBuilder treeBuilder;

    @BeforeEach
    void setUp() {
        this.serializers = new LiteralValueSerializers(new DefaultValueSerializer(new MultilingualStringSerializer()));
        this.treeBuilder = new JsonLdTreeBuilder(serializers);
    }

    @Test
    void visitAttributeUsesConfiguredCustomSerializerWhenTypeMatches() throws Exception {
        final ValueSerializer<Integer> intSerializer = mock(ValueSerializer.class);
        serializers.registerSerializer(Integer.class, intSerializer);
        final Study study = new Study();
        study.setUri(Generator.generateUri());
        study.setNoOfPeopleInvolved(10);
        final JsonNode serialized = JsonNodeFactory.createLiteralNode(Vocabulary.NUMBER_OF_PEOPLE_INVOLVED,
                                                                      (long) study.getNoOfPeopleInvolved());
        when(intSerializer.serialize(any(), any())).thenReturn(serialized);
        treeBuilder.openObject(new SerializationContext<>(study, DummyJsonLdContext.INSTANCE));
        final SerializationContext<Integer> ctx = new SerializationContext<>(Vocabulary.NUMBER_OF_PEOPLE_INVOLVED,
                                                                             Study.class.getDeclaredField(
                                                                                     "noOfPeopleInvolved"),
                                                                             study.getNoOfPeopleInvolved(),
                                                                             DummyJsonLdContext.INSTANCE);
        treeBuilder.visitAttribute(ctx);
        verify(intSerializer).serialize(study.getNoOfPeopleInvolved(), ctx);
        final JsonNode node =
                JsonLdTreeBuilderTest.getNode(treeBuilder.getTreeRoot(), Vocabulary.NUMBER_OF_PEOPLE_INVOLVED);
        assertNotNull(node);
        assertSame(serialized, node);
    }
}
