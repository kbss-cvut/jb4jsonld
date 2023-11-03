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
package cz.cvut.kbss.jsonld.deserialization.expanded;

import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.deserialization.CommonValueDeserializers;
import cz.cvut.kbss.jsonld.deserialization.DefaultInstanceBuilder;
import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.InstanceBuilder;
import cz.cvut.kbss.jsonld.deserialization.ValueDeserializer;
import cz.cvut.kbss.jsonld.deserialization.reference.PendingReferenceRegistry;
import cz.cvut.kbss.jsonld.deserialization.util.LangString;
import cz.cvut.kbss.jsonld.deserialization.util.TargetClassResolver;
import cz.cvut.kbss.jsonld.deserialization.util.TypeMap;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.TestUtil;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.ObjectWithMultilingualString;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.exception.MissingIdentifierException;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CollectionDeserializerTest {

    private InstanceBuilder instanceBuilder;
    private DeserializerConfig deserializerConfig;

    @BeforeEach
    void setUp() {
        final TargetClassResolver typeResolver = new TargetClassResolver(new TypeMap());
        this.instanceBuilder = new DefaultInstanceBuilder(typeResolver, new PendingReferenceRegistry());
        this.deserializerConfig =
                new DeserializerConfig(new Configuration(), typeResolver, new CommonValueDeserializers());
    }

    @Test
    void processValueAddsObjectIdentifiersIntoPropertiesMap() throws Exception {
        final JsonValue jsonLd = TestUtil.readAndExpand("objectWithPluralReference.json").get(0);
        final JsonArray collection = jsonLd.asJsonObject().getJsonArray(Vocabulary.HAS_MEMBER);
        final CollectionDeserializer sut = new CollectionDeserializer(instanceBuilder, deserializerConfig,
                                                                      Vocabulary.HAS_MEMBER);
        instanceBuilder.openObject(Generator.generateUri().toString(), Person.class);
        sut.processValue(collection);
        final Person person = (Person) instanceBuilder.getCurrentRoot();
        final Set<?> values = person.getProperties().get(Vocabulary.HAS_MEMBER);
        assertTrue(values.contains(TestUtil.HALSEY_URI.toString()));
        assertTrue(values.contains(TestUtil.LASKY_URI.toString()));
        assertTrue(values.contains(TestUtil.PALMER_URI.toString()));
    }

    @Test
    void processValueThrowsMissingIdentifierExceptionWhenInstanceToBeAddedIntoPropertiesHasNoIdentifier()
            throws Exception {
        final JsonObject jsonLd = TestUtil.readAndExpand("objectWithPluralReference.json").getJsonObject(0);
        final JsonObject item = jsonLd.getJsonArray(Vocabulary.HAS_MEMBER).getJsonObject(0);
        final Map<String, Object> itemMap = new HashMap<>(item);
        itemMap.remove(JsonLd.ID);
        final JsonObject itemUpdated = Json.createObjectBuilder(itemMap).build();
        final JsonArray collectionToProcess = Json.createArrayBuilder().add(itemUpdated).build();
        final CollectionDeserializer sut = new CollectionDeserializer(instanceBuilder, deserializerConfig,
                                                                      Vocabulary.HAS_MEMBER);
        instanceBuilder.openObject(Generator.generateUri().toString(), Person.class);
        final MissingIdentifierException result = assertThrows(MissingIdentifierException.class,
                                                               () -> sut.processValue(collectionToProcess));
        assertThat(result.getMessage(),
                   containsString("Cannot put an object without an identifier into @Properties. Object: "));
    }

    @Test
    void processValueAddsLangStringWhenItemHasLanguageString() throws Exception {
        final JsonObject jsonLd = TestUtil.readAndExpand("objectWithMultilingualString.json").getJsonObject(0);
        final JsonArray labels = jsonLd.getJsonArray(RDFS.LABEL);
        final InstanceBuilder builderSpy = spy(instanceBuilder);
        final CollectionDeserializer sut = new CollectionDeserializer(builderSpy, deserializerConfig, RDFS.LABEL);
        builderSpy.openObject(Generator.generateUri().toString(), ObjectWithMultilingualString.class);
        sut.processValue(labels);
        verify(builderSpy, times(2)).addValue(ArgumentMatchers.any(LangString.class));
    }

    @Test
    void processValueAddsLangStringWhenAttributeValueIsSingleObjectWithLanguageTag() throws Exception {
        final JsonObject jsonLd = TestUtil.readAndExpand("objectWithSingleLangStringValue.json").getJsonObject(0);
        final JsonArray labels = jsonLd.getJsonArray(RDFS.LABEL);
        final InstanceBuilder builderSpy = spy(instanceBuilder);
        final CollectionDeserializer sut = new CollectionDeserializer(builderSpy, deserializerConfig, RDFS.LABEL);
        builderSpy.openObject(Generator.generateUri().toString(), ObjectWithMultilingualString.class);
        sut.processValue(labels);
        verify(builderSpy).addValue(eq(RDFS.LABEL), ArgumentMatchers.any(LangString.class));
    }

    @Test
    void processValueUsesCustomDeserializerWhenItMatchesSingularPropertyTargetType() throws Exception {
        final JsonObject jsonLd = TestUtil.readAndExpand("objectWithSingularReference.json").getJsonObject(0);
        final OrganizationDeserializer customDeserializer = spy(new OrganizationDeserializer());
        deserializerConfig.getDeserializers().registerDeserializer(Organization.class, customDeserializer);
        final CollectionDeserializer sut =
                new CollectionDeserializer(instanceBuilder, deserializerConfig, Vocabulary.IS_MEMBER_OF);
        instanceBuilder.openObject(Generator.generateUri().toString(), Employee.class);
        sut.processValue(jsonLd.getJsonArray(Vocabulary.IS_MEMBER_OF));
        verify(customDeserializer).deserialize(any(JsonObject.class), any(DeserializationContext.class));
        final Employee result = (Employee) instanceBuilder.getCurrentRoot();
        assertNotNull(result.getEmployer());
        assertEquals(TestUtil.UNSC_URI, result.getEmployer().getUri());
        assertEquals("UNSC", result.getEmployer().getName());
    }

    private static class OrganizationDeserializer implements ValueDeserializer<Organization> {
        @Override
        public Organization deserialize(JsonValue jsonNode, DeserializationContext<Organization> ctx) {
            assert jsonNode.getValueType() == JsonValue.ValueType.OBJECT;
            final Organization result = new Organization();
            final JsonValue label = jsonNode.asJsonObject().get(RDFS.LABEL).asJsonArray().get(0);
            result.setName(label.asJsonObject().getString(JsonLd.VALUE));
            result.setUri(URI.create(jsonNode.asJsonObject().getString(JsonLd.ID)));
            return result;
        }
    }

    @Test
    void processValueUsesCustomDeserializerWhenItMatchesPluralPropertyElementType() throws Exception {
        final JsonObject jsonLd = TestUtil.readAndExpand("objectWithPluralReference.json").getJsonObject(0);
        final EmployeeDeserializer customDeserializer = spy(new EmployeeDeserializer());
        deserializerConfig.getDeserializers().registerDeserializer(Employee.class, customDeserializer);
        final CollectionDeserializer sut =
                new CollectionDeserializer(instanceBuilder, deserializerConfig, Vocabulary.HAS_MEMBER);
        instanceBuilder.openObject(Generator.generateUri().toString(), Organization.class);
        sut.processValue(jsonLd.getJsonArray(Vocabulary.HAS_MEMBER));
        verify(customDeserializer, times(3)).deserialize(any(JsonObject.class), any(DeserializationContext.class));
        final Organization result = (Organization) instanceBuilder.getCurrentRoot();
        assertNotNull(result.getEmployees());
        assertEquals(3, result.getEmployees().size());
        assertTrue(result.getEmployees().stream().anyMatch(e -> TestUtil.HALSEY_URI.equals(e.getUri())));
        assertTrue(result.getEmployees().stream().anyMatch(e -> TestUtil.LASKY_URI.equals(e.getUri())));
        assertTrue(result.getEmployees().stream().anyMatch(e -> TestUtil.PALMER_URI.equals(e.getUri())));
    }

    private static class EmployeeDeserializer implements ValueDeserializer<Employee> {
        @Override
        public Employee deserialize(JsonValue jsonNode, DeserializationContext<Employee> ctx) {
            final Employee result = new Employee();
            result.setUri(URI.create(jsonNode.asJsonObject().getString(JsonLd.ID)));
            final JsonArray firstName = jsonNode.asJsonObject().get(Vocabulary.FIRST_NAME).asJsonArray();
            result.setFirstName(firstName.get(0).asJsonObject().getString(JsonLd.VALUE));
            final JsonArray lastName = jsonNode.asJsonObject().get(Vocabulary.LAST_NAME).asJsonArray();
            result.setLastName(lastName.get(0).asJsonObject().getString(JsonLd.VALUE));
            return result;
        }
    }
}
