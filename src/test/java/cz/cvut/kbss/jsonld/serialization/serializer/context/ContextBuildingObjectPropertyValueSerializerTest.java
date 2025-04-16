/*
 * JB4JSON-LD
 * Copyright (C) 2025 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.serialization.serializer.context;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.serialization.context.JsonLdContext;
import cz.cvut.kbss.jsonld.serialization.serializer.SerializerUtils;
import cz.cvut.kbss.jsonld.serialization.traversal.ObjectGraphTraverser;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ContextBuildingObjectPropertyValueSerializerTest {

    @Mock
    private ObjectGraphTraverser objectGraphTraverser;

    @InjectMocks
    private ContextBuildingObjectPropertyValueSerializer sut;

    @Test
    void serializeRegistersTermIriMappingInJsonLdContext() throws Exception {
        final JsonLdContext ctx = mock(JsonLdContext.class);
        final Organization value = Generator.generateOrganization();
        final SerializationContext<Organization> serializationCtx =
                new SerializationContext<>(Vocabulary.IS_MEMBER_OF, Employee.getEmployerField(), value, ctx);

        sut.serialize(value, serializationCtx);
        verify(ctx).registerTermMapping(Employee.getEmployerField().getName(), Vocabulary.IS_MEMBER_OF);
    }

    @Test
    void serializeRegistersExtendedTermDefinitionWithIdAndTypeInJsonLdContextWhenConfiguredToUseExtendedDefinition() throws Exception {
        final Configuration config = new Configuration();
        config.set(ConfigParam.SERIALIZE_INDIVIDUALS_USING_EXPANDED_DEFINITION, Boolean.TRUE.toString());
        sut.configure(config);
        final JsonLdContext ctx = mock(JsonLdContext.class);
        final URI value = Generator.generateUri();
        final SerializationContext<URI> serializationCtx =
                new SerializationContext<>(Vocabulary.ORIGIN, Organization.class.getDeclaredField("country"), value,
                                           ctx);

        sut.serialize(value, serializationCtx);
        verify(ctx).registerTermMapping("country",
                                        SerializerUtils.createTypedTermDefinition("country", Vocabulary.ORIGIN,
                                                                                  JsonLd.ID));
    }

    @Test
    void serializeRegistersTermIriMappingInJsonLdContextWhenConfiguredToUseExtendedDefinitionWhenValueIsComplex() throws Exception {
        final Configuration config = new Configuration();
        config.set(ConfigParam.SERIALIZE_INDIVIDUALS_USING_EXPANDED_DEFINITION, Boolean.TRUE.toString());
        sut.configure(config);
        final JsonLdContext ctx = mock(JsonLdContext.class);
        final Organization value = Generator.generateOrganization();
        final SerializationContext<Organization> serializationCtx =
                new SerializationContext<>(Vocabulary.IS_MEMBER_OF, Employee.getEmployerField(), value, ctx);

        sut.serialize(value, serializationCtx);
        verify(ctx).registerTermMapping(Employee.getEmployerField().getName(), Vocabulary.IS_MEMBER_OF);
    }

    @Test
    void serializeRegistersTermIriMappingInJsonLdContextWhenConfiguredToUseExtendedDefinitionWhenValueIsCollectionOfComplexObjects() throws Exception {
        final Configuration config = new Configuration();
        config.set(ConfigParam.SERIALIZE_INDIVIDUALS_USING_EXPANDED_DEFINITION, Boolean.TRUE.toString());
        sut.configure(config);
        final JsonLdContext ctx = mock(JsonLdContext.class);
        final Set<URI> value = new HashSet<>(Arrays.asList(Generator.generateUri(), Generator.generateUri()));
        final SerializationContext<Set<URI>> serializationCtx =
                new SerializationContext<>(Vocabulary.HAS_MEMBER, Organization.getEmployeesField(), value, ctx);

        sut.serialize(value, serializationCtx);
        verify(ctx).registerTermMapping(Organization.getEmployeesField().getName(),
                                        SerializerUtils.createTypedTermDefinition(
                                                Organization.getEmployeesField().getName(), Vocabulary.HAS_MEMBER,
                                                JsonLd.ID));
    }
}