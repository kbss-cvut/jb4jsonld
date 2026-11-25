/*
 * JB4JSON-LD
 * Copyright (C) 2026 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.serialization.serializer;

import cz.cvut.kbss.jopa.vocabulary.DC;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.Statement;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.context.DummyJsonLdContext;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.DefaultValueSerializer;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.MultilingualStringSerializer;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.ObjectPropertyValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.ObjectGraphTraverser;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ObjectGraphValueSerializersTest {

    @Mock
    private ObjectGraphTraverser traverser;

    private ObjectGraphValueSerializers sut;

    @BeforeEach
    void setUp() {
        this.sut = new ObjectGraphValueSerializers(
                new LiteralValueSerializers(new DefaultValueSerializer(new MultilingualStringSerializer())),
                new ObjectPropertyValueSerializer(traverser));
    }

    @Test
    void getSerializerReturnsObjectPropertySerializerWhenProvidedFieldIsObjectProperty() throws Exception {
        final SerializationContext<Organization> ctx =
                new SerializationContext<>(Vocabulary.IS_MEMBER_OF, Employee.getEmployerField(),
                                           Generator.generateOrganization(), DummyJsonLdContext.INSTANCE);
        final Optional<ValueSerializer<Organization>> result = sut.getSerializer(ctx);
        assertTrue(result.isPresent());
        assertThat(result.get(), instanceOf(ObjectPropertyValueSerializer.class));
    }

    @Test
    void getSerializerReturnsEmptyOptionalWhenFieldIsNotObjectPropertyAndNoCustomSerializerIsRegisteredForIt() throws Exception {
        final SerializationContext<String> ctx =
                new SerializationContext<>(RDFS.LABEL, Organization.class.getDeclaredField("name"), "Test",
                                           DummyJsonLdContext.INSTANCE);
        final Optional<ValueSerializer<String>> result = sut.getSerializer(ctx);
        assertFalse(result.isPresent());
    }

    @Test
    void getSerializerReturnsCustomSerializerWhenItIsRegisteredInCommon() throws Exception {
        final ValueSerializer<Organization> serializer =
                ((_, ctx) -> JsonNodeFactory.createObjectIdNode(ctx.getTerm(), Generator.generateUri()));
        sut.registerSerializer(Organization.class, serializer);
        final SerializationContext<Organization> ctx =
                new SerializationContext<>(Vocabulary.IS_MEMBER_OF, Employee.getEmployerField(),
                                           Generator.generateOrganization(), DummyJsonLdContext.INSTANCE);
        final Optional<ValueSerializer<Organization>> result = sut.getSerializer(ctx);
        assertTrue(result.isPresent());
        assertEquals(serializer, result.get());
    }

    @Test
    void getOrDefaultReturnsObjectPropertySerializerWhenProvidedFieldIsObjectProperty() throws Exception {
        final SerializationContext<Organization> ctx =
                new SerializationContext<>(Vocabulary.IS_MEMBER_OF, Employee.getEmployerField(),
                                           Generator.generateOrganization(), DummyJsonLdContext.INSTANCE);
        final ValueSerializer<Organization> result = sut.getOrDefault(ctx);
        assertThat(result, instanceOf(ObjectPropertyValueSerializer.class));
    }

    @Test
    void getOrDefaultReturnsCustomSerializerWhenItIsRegistered() throws Exception {
        final ValueSerializer<Organization> serializer =
                ((_, ctx) -> JsonNodeFactory.createObjectIdNode(ctx.getTerm(), Generator.generateUri()));
        sut.registerSerializer(Organization.class, serializer);
        final SerializationContext<Organization> ctx =
                new SerializationContext<>(Vocabulary.IS_MEMBER_OF, Employee.getEmployerField(),
                                           Generator.generateOrganization(), DummyJsonLdContext.INSTANCE);
        final ValueSerializer<Organization> result = sut.getOrDefault(ctx);
        assertEquals(serializer, result);
    }

    @Test
    void getOrDefaultReturnsDefaultSerializerWhenFieldIsNotObjectPropertyAndNoCustomSerializerIsRegisteredForIt() throws Exception {
        final SerializationContext<String> ctx =
                new SerializationContext<>(RDFS.LABEL, Organization.class.getDeclaredField("name"), "Test",
                                           DummyJsonLdContext.INSTANCE);
        final ValueSerializer<String> result = sut.getOrDefault(ctx);
        assertNotNull(result);
    }

    @Test
    void getOrDefaultReturnsObjectPropertySerializerWhenAttributeIsAnnotationPropertyWithEntityValue() throws Exception {
        final cz.cvut.kbss.jsonld.environment.model.Statement instance =
                new cz.cvut.kbss.jsonld.environment.model.Statement(
                        new cz.cvut.kbss.jsonld.environment.model.Statement.Iri(Generator.generateUri()),
                        new cz.cvut.kbss.jsonld.environment.model.Statement.Iri(URI.create(RDF.TYPE.stringValue())),
                        new cz.cvut.kbss.jsonld.environment.model.Statement.Iri(URI.create(Vocabulary.AGENT)),
                        new cz.cvut.kbss.jsonld.environment.model.Statement.Iri(Generator.generateUri()));
        final SerializationContext<Statement.Iri> ctx = new SerializationContext<>(DC.Terms.SOURCE,
                                                                                   Statement.class.getDeclaredField(
                                                                                           "context"),
                                                                                   instance.getContext(),
                                                                                   DummyJsonLdContext.INSTANCE);

        final ValueSerializer<Statement.Iri> result = sut.getOrDefault(ctx);
        assertInstanceOf(ObjectPropertyValueSerializer.class, result);
    }

    @Test
    void getOrDefaultReturnsObjectPropertySerializerWhenAttributeIsPluralAnnotationPropertyWithEntityValue() throws Exception {
        final cz.cvut.kbss.jsonld.environment.model.Statement instance =
                new cz.cvut.kbss.jsonld.environment.model.Statement(
                        new cz.cvut.kbss.jsonld.environment.model.Statement.Iri(Generator.generateUri()),
                        new cz.cvut.kbss.jsonld.environment.model.Statement.Iri(URI.create(RDF.TYPE.stringValue())),
                        new cz.cvut.kbss.jsonld.environment.model.Statement.Iri(URI.create(Vocabulary.AGENT)));
        instance.setIsReferencedBy(Set.of(
                new cz.cvut.kbss.jsonld.environment.model.Statement.Iri(Generator.generateUri()),
                new cz.cvut.kbss.jsonld.environment.model.Statement.Iri(Generator.generateUri()),
                new cz.cvut.kbss.jsonld.environment.model.Statement.Iri(Generator.generateUri())
        ));
        final SerializationContext<Set<Statement.Iri>> ctx = new SerializationContext<>(DC.Terms.IS_REFERENCED_BY,
                                                                                   Statement.class.getDeclaredField(
                                                                                           "isReferencedBy"),
                                                                                   instance.getIsReferencedBy(),
                                                                                   DummyJsonLdContext.INSTANCE);

        final ValueSerializer<Set<Statement.Iri>> result = sut.getOrDefault(ctx);
        assertInstanceOf(ObjectPropertyValueSerializer.class, result);
    }
}
