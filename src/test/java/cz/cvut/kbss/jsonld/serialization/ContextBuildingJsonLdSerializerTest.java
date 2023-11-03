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

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.vocabulary.DC;
import cz.cvut.kbss.jopa.vocabulary.OWL;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.IdentifierUtil;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.TestUtil;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Attribute;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.ObjectWithMultilingualString;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.OwlPropertyType;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.environment.model.Study;
import cz.cvut.kbss.jsonld.environment.model.StudyWithNamespaces;
import cz.cvut.kbss.jsonld.environment.model.User;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Models;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextBuildingJsonLdSerializerTest extends JsonLdSerializerTestBase {

    @BeforeEach
    void setUp() {
        this.sut = new ContextBuildingJsonLdSerializer(jsonWriter);
    }

    @Test
    void jsonLdContextContainsCorrectAttributeToPropertyMapping() throws Exception {
        final User user = Generator.generateUser();

        final JsonObject jsonMap = serializeAndRead(user).asJsonObject();
        assertThat(jsonMap, hasKey(JsonLd.CONTEXT));
        assertEquals(JsonValue.ValueType.OBJECT, jsonMap.get(JsonLd.CONTEXT).getValueType());
        final JsonObject context = jsonMap.getJsonObject(JsonLd.CONTEXT);
        assertEquals(Vocabulary.FIRST_NAME, context.getString(User.getFirstNameField().getName()));
        assertEquals(Vocabulary.LAST_NAME, context.getString(User.getLastNameField().getName()));
        assertEquals(Vocabulary.USERNAME, context.getString(User.getUsernameField().getName()));
        assertEquals(Vocabulary.IS_ADMIN, context.getString(User.class.getDeclaredField("admin").getName()));
    }

    @Test
    void serializeWithContextUsesFieldNamesAsJsonLdAttributeNames() throws Exception {
        final User user = Generator.generateUser();

        final JsonObject jsonMap = serializeAndRead(user).asJsonObject();
        assertEquals(user.getFirstName(), jsonMap.getString(User.getFirstNameField().getName()));
        assertEquals(user.getLastName(), jsonMap.getString(User.getLastNameField().getName()));
        assertEquals(user.getUsername(), jsonMap.getString(User.getUsernameField().getName()));
        assertEquals(user.getAdmin(), jsonMap.getBoolean(User.class.getDeclaredField("admin").getName()));
    }

    @Test
    void serializeWithContextSupportsTypesAndIdMapping() throws Exception {
        final User user = Generator.generateUser();
        user.setTypes(Collections.singleton(Generator.generateUri().toString()));

        final JsonObject jsonMap = serializeAndRead(user).asJsonObject();
        assertEquals(JsonValue.ValueType.OBJECT, jsonMap.get(JsonLd.CONTEXT).getValueType());
        final JsonObject context = jsonMap.getJsonObject(JsonLd.CONTEXT);
        assertEquals(JsonLd.ID, context.getString(Person.class.getDeclaredField("uri").getName()));
        assertEquals(JsonLd.TYPE, context.getString(User.class.getDeclaredField("types").getName()));
    }

    @Test
    void serializeWithContextBuildsContextForObjectProperty() throws Exception {
        final Employee employee = Generator.generateEmployee();

        final JsonObject jsonMap = serializeAndRead(employee).asJsonObject();
        assertThat(jsonMap, hasKey(JsonLd.CONTEXT));
        assertEquals(JsonValue.ValueType.OBJECT, jsonMap.get(JsonLd.CONTEXT).getValueType());
        final JsonObject context = jsonMap.getJsonObject(JsonLd.CONTEXT);
        assertEquals(Vocabulary.IS_MEMBER_OF, context.getString(Employee.getEmployerField().getName()));
        assertEquals(RDFS.LABEL, context.getString(Organization.class.getDeclaredField("name").getName()));
        assertEquals(Vocabulary.BRAND, context.getString(Organization.class.getDeclaredField("brands").getName()));
        final JsonValue dateDefinition = context.get(Organization.class.getDeclaredField("dateCreated").getName());
        assertEquals(JsonValue.ValueType.OBJECT, dateDefinition.getValueType());
        assertEquals(XSD.DATETIME, dateDefinition.asJsonObject().getString(JsonLd.TYPE));
        assertEquals(Vocabulary.DATE_CREATED, dateDefinition.asJsonObject().getString(JsonLd.ID));
    }

    @Test
    void serializeWithContextUsesReferencedEntityFieldNamesAsAttributeNames() throws Exception {
        final Employee employee = Generator.generateEmployee();

        final JsonObject jsonMap = serializeAndRead(employee).asJsonObject();
        assertEquals(JsonValue.ValueType.OBJECT, jsonMap.get(Employee.getEmployerField().getName()).getValueType());
        final JsonObject orgMap = jsonMap.getJsonObject(Employee.getEmployerField().getName());
        assertEquals(employee.getEmployer().getName(),
                     orgMap.getString(Organization.class.getDeclaredField("name").getName()));
        assertEquals(
                DateTimeFormatter.ISO_DATE_TIME.format(employee.getEmployer().getDateCreated().toInstant().atOffset(
                        ZoneOffset.UTC)),
                orgMap.getString(Organization.class.getDeclaredField("dateCreated").getName()));
        assertEquals(JsonValue.ValueType.ARRAY,
                     orgMap.get(Organization.class.getDeclaredField("brands").getName()).getValueType());
        final JsonArray jsonBrands = orgMap.getJsonArray(Organization.class.getDeclaredField("brands").getName());
        assertEquals(employee.getEmployer().getBrands().size(), jsonBrands.size());
        assertThat(jsonBrands.stream().map(item -> ((JsonString) item).getString()).collect(Collectors.toList()),
                   hasItems(employee.getEmployer().getBrands().toArray(new String[]{})));
    }

    @Test
    void serializationSkipsNullDataPropertyValues() throws Exception {
        final User user = Generator.generateUser();
        user.setAdmin(null);
        final JsonObject json = serializeAndRead(user).asJsonObject();
        assertThat(json, not(hasKey(User.class.getDeclaredField("admin").getName())));
    }

    @Test
    void serializationSkipsNullObjectPropertyValues() throws Exception {
        final Employee employee = Generator.generateEmployee();
        employee.setEmployer(null);
        final JsonObject json = serializeAndRead(employee).asJsonObject();
        assertThat(json, not(hasKey(Employee.getEmployerField().getName())));
    }

    @Test
    void serializeWithContextSupportsCompactedIrisBasedOnJOPANamespacesInContext() throws Exception {
        final StudyWithNamespaces study = new StudyWithNamespaces();
        study.setUri(Generator.generateUri());
        study.setName("Test study");
        study.setParticipants(Collections.singleton(Generator.generateEmployee()));
        study.setMembers(Collections.singleton(Generator.generateEmployee()));

        final JsonObject json = serializeAndRead(study).asJsonObject();
        final JsonObject context = json.getJsonObject(JsonLd.CONTEXT);
        assertEquals(RDFS.LABEL, context.getString(StudyWithNamespaces.class.getDeclaredField("name").getName()));
        assertEquals(Vocabulary.HAS_PARTICIPANT,
                     context.getString(StudyWithNamespaces.class.getDeclaredField("participants").getName()));
        assertEquals(Vocabulary.HAS_MEMBER,
                     context.getString(StudyWithNamespaces.class.getDeclaredField("members").getName()));
    }

    @Test
    void serializationGeneratesBlankNodeIfInstancesDoesNotHaveIdentifierValue() {
        final Organization company = Generator.generateOrganization();
        company.setUri(null);
        final JsonObject json = serializeAndRead(company).asJsonObject();
        assertThat(json, hasKey(TestUtil.ID_FIELD_NAME));
        assertThat(json.getString(TestUtil.ID_FIELD_NAME), startsWith(IdentifierUtil.B_NODE_PREFIX));
    }

    @Test
    void serializationOfCollectionReturnsJsonObjectWithContextAndGraphWithSerializedCollection() throws Exception {
        final List<User> users =
                IntStream.range(0, 5).mapToObj(i -> Generator.generateUser()).collect(Collectors.toList());

        final JsonObject json = serializeAndRead(users).asJsonObject();
        assertThat(json, hasKey(JsonLd.CONTEXT));
        assertThat(json, hasKey(JsonLd.GRAPH));
        assertEquals(JsonValue.ValueType.ARRAY, json.get(JsonLd.GRAPH).getValueType());
        final Model result = readJson(jsonWriter.getResult());
        users.forEach(u -> {
            final Model pModel = new LinkedHashModel();
            u.toRdf(new LinkedHashModel(), vf(), new HashSet<>());
            assertTrue(Models.isSubset(pModel, result));
        });
    }

    @Test
    void serializationOfCollectionReusesReferences() throws Exception {
        final List<Employee> employees = Arrays.asList(Generator.generateEmployee(), Generator.generateEmployee());
        employees.get(1).setEmployer(employees.get(0).getEmployer());

        final JsonObject json = serializeAndRead(employees).asJsonObject();
        final JsonArray items = json.getJsonArray(JsonLd.GRAPH);
        final JsonObject eOne = items.getJsonObject(0);
        final JsonObject orgOne = eOne.getJsonObject(Employee.getEmployerField().getName());
        assertThat(orgOne.size(), greaterThan(1));
        assertEquals(employees.get(0).getEmployer().getUri().toString(), orgOne.getString(TestUtil.ID_FIELD_NAME));
        final JsonObject eTwo = items.getJsonObject(1);
        final JsonObject orgTwo = eTwo.getJsonObject(Employee.getEmployerField().getName());
        assertEquals(1, orgTwo.size());
        assertEquals(employees.get(1).getEmployer().getUri().toString(), orgTwo.getString(TestUtil.ID_FIELD_NAME));

        final Model result = readJson(jsonWriter.getResult());
        final Model orgModel = toRdf(employees.get(0).getEmployer());
        assertTrue(Models.isSubset(orgModel, result));
    }

    @Test
    void serializationBuildsCorrectlyContextBasedOnAnnotationPropertyAttribute() throws Exception {
        final ObjectWithMultilingualString instance = new ObjectWithMultilingualString(Generator.generateUri());
        instance.setScopeNote(MultilingualString.create("Test scope note", "en"));
        final JsonObject json = serializeAndRead(instance).asJsonObject();
        assertThat(json, hasKey(JsonLd.CONTEXT));
        assertEquals(JsonValue.ValueType.OBJECT, json.get(JsonLd.CONTEXT).getValueType());
        final JsonObject context = json.get(JsonLd.CONTEXT).asJsonObject();
        assertThat(context, hasKey(ObjectWithMultilingualString.getScopeNoteField().getName()));
    }

    @Test
    void serializationUsesMappedTermForTypesWhenItIsRegisteredInReferencedObject() {
        final Study instance = new Study();
        instance.setUri(Generator.generateUri());
        final Employee emp = Generator.generateEmployee();
        instance.setMembers(Collections.singleton(emp));

        final JsonObject json = serializeAndRead(instance).asJsonObject();
        assertThat(json, hasKey("types"));
        assertEquals(Collections.singletonList(Vocabulary.STUDY),
                     json.getJsonArray("types").stream().map(v -> ((JsonString) v).getString()).collect(
                             Collectors.toList()));
    }

    @Test
    void serializationCreatesEmbeddedContextToOverrideIncompatibleTermMapping() {
        final StudyWithTitle instance = new StudyWithTitle();
        instance.uri = Generator.generateUri();
        instance.name = "Test study";
        instance.organization = Generator.generateOrganization();

        final JsonObject json = serializeAndRead(instance).asJsonObject();
        verifyEmbeddedContext(json);
    }

    private void verifyEmbeddedContext(JsonObject json) {
        assertThat(json, hasKey(JsonLd.CONTEXT));
        assertEquals(JsonValue.ValueType.OBJECT, json.get(JsonLd.CONTEXT).getValueType());
        final JsonObject context = json.getJsonObject(JsonLd.CONTEXT);
        assertEquals(DC.Terms.TITLE, context.getString("name"));
        assertThat(json, hasKey("organization"));
        assertEquals(JsonValue.ValueType.OBJECT, json.get("organization").getValueType());
        final JsonObject organization = json.getJsonObject("organization");
        assertThat(organization, hasKey(JsonLd.CONTEXT));
        assertEquals(JsonValue.ValueType.OBJECT, organization.get(JsonLd.CONTEXT).getValueType());
        final JsonObject embeddedCtx = organization.getJsonObject(JsonLd.CONTEXT);
        assertEquals(RDFS.LABEL, embeddedCtx.getString("name"));
    }

    @OWLClass(iri = Vocabulary.STUDY)
    private static class StudyWithTitle {

        @Id
        private URI uri;

        // Organization name uses rdfs:label
        @OWLDataProperty(iri = DC.Terms.TITLE)
        private String name;

        @OWLObjectProperty(iri = Vocabulary.HAS_PARTICIPANT)
        private Organization organization;
    }

    @Test
    void serializationUsesRegisteredIdentifierTermWhenSerializingPlainIdentifierObjectPropertyValue() {
        final Organization instance = Generator.generateOrganization();
        instance.setCountry(URI.create("http://dbpedia.org/resource/Czech_Republic"));

        final JsonObject json = serializeAndRead(instance).asJsonObject();
        assertThat(json, hasKey("country"));
        assertEquals(JsonValue.ValueType.OBJECT, json.get("country").getValueType());
        final JsonObject country = json.getJsonObject("country");
        assertThat(country, hasKey("uri"));
        assertEquals(instance.getCountry().toString(), country.getString("uri"));
    }

    @Test
    void serializationSerializesRootCollectionOfEnumConstantsMappedToIndividualsAsArrayOfIndividuals() {
        final List<OwlPropertyType> value = Arrays.asList(OwlPropertyType.values());

        final JsonObject jsonObject = serializeAndRead(new LinkedHashSet<>(value)).asJsonObject();
        assertThat(jsonObject, hasKey(JsonLd.GRAPH));
        assertEquals(JsonValue.ValueType.ARRAY, jsonObject.get(JsonLd.GRAPH).getValueType());
        final JsonArray lst = jsonObject.getJsonArray(JsonLd.GRAPH);
        assertEquals(value.size(), lst.size());
        for (int i = 0; i < value.size(); i++) {
            assertEquals(JsonValue.ValueType.OBJECT, lst.get(i).getValueType());
            final JsonObject element = lst.getJsonObject(i);
            assertThat(element, hasKey(JsonLd.ID));
            assertEquals(OwlPropertyType.getMappedIndividual(value.get(i)), element.getString(JsonLd.ID));
        }
    }

    /**
     * Bug #51
     */
    @Test
    void serializationCreatesEmbeddedContextOnCorrectLevel() {
        final StudyWithTitle instance = new StudyWithTitle();
        instance.uri = Generator.generateUri();
        instance.name = "Test study";
        instance.organization = Generator.generateOrganization();
        instance.organization.addEmployee(Generator.generateEmployee());
        instance.organization.addEmployee(Generator.generateEmployee());
        instance.organization.getEmployees().forEach(e -> e.setEmployer(instance.organization));

        final JsonObject json = serializeAndRead(instance).asJsonObject();
        verifyEmbeddedContext(json);
    }

    @Test
    void serializationSerializesIndividualsAsStringWithExpandedTermDefinitionInContextWhenConfiguredTo() {
        sut.configuration().set(ConfigParam.SERIALIZE_INDIVIDUALS_USING_EXPANDED_DEFINITION, Boolean.TRUE.toString());
        final Attribute instance = new Attribute();
        instance.setUri(Generator.generateUri());
        instance.setPropertyType(OwlPropertyType.DATATYPE_PROPERTY);
        instance.setPluralPropertyType(
                new HashSet<>(Arrays.asList(OwlPropertyType.ANNOTATION_PROPERTY, OwlPropertyType.OBJECT_PROPERTY)));

        final JsonObject json = serializeAndRead(instance).asJsonObject();
        assertThat(json, hasKey(JsonLd.CONTEXT));
        assertEquals(JsonValue.ValueType.OBJECT, json.get(JsonLd.CONTEXT).getValueType());
        final JsonObject context = json.getJsonObject(JsonLd.CONTEXT);
        assertThat(context, hasKey("propertyType"));
        assertEquals(JsonValue.ValueType.OBJECT, context.get("propertyType").getValueType());
        final JsonObject termDef = context.getJsonObject("propertyType");
        assertThat(termDef, hasKey(JsonLd.ID));
        assertEquals(Vocabulary.HAS_PROPERTY_TYPE, termDef.getString(JsonLd.ID));
        assertThat(termDef, hasKey(JsonLd.TYPE));
        assertEquals(JsonLd.ID, termDef.getString(JsonLd.TYPE));
        assertThat(context, hasKey("pluralPropertyType"));
        assertEquals(JsonValue.ValueType.OBJECT, context.get("pluralPropertyType").getValueType());
        final JsonObject pluralTermDef = context.getJsonObject("pluralPropertyType");
        assertThat(pluralTermDef, hasKey(JsonLd.ID));
        assertEquals(Vocabulary.HAS_PLURAL_PROPERTY_TYPE, pluralTermDef.getString(JsonLd.ID));
        assertThat(pluralTermDef, hasKey(JsonLd.TYPE));
        assertEquals(JsonLd.ID, pluralTermDef.getString(JsonLd.TYPE));
        assertThat(json, hasKey("propertyType"));
        assertEquals(OWL.DATATYPE_PROPERTY, json.getString("propertyType"));
        assertThat(json, hasKey("pluralPropertyType"));
        assertEquals(JsonValue.ValueType.ARRAY, json.get("pluralPropertyType").getValueType());
        assertThat(json.getJsonArray("pluralPropertyType").stream().map(v -> ((JsonString) v).getString()).collect(
                Collectors.toList()), hasItems(OWL.ANNOTATION_PROPERTY, OWL.OBJECT_PROPERTY));
    }

    @Test
    void serializationSerializesPlainIdentifierAsStringWithExpandedTermDefinitionInContextWhenConfiguredTo() {
        sut.configuration().set(ConfigParam.SERIALIZE_INDIVIDUALS_USING_EXPANDED_DEFINITION, Boolean.TRUE.toString());
        final Organization instance = Generator.generateOrganization();
        instance.setCountry(URI.create("http://dbpedia.org/resource/Czech_Republic"));

        final JsonObject json = serializeAndRead(instance).asJsonObject();
        assertThat(json, hasKey(JsonLd.CONTEXT));
        assertEquals(JsonValue.ValueType.OBJECT, json.get(JsonLd.CONTEXT).getValueType());
        final JsonObject context = json.getJsonObject(JsonLd.CONTEXT);
        assertThat(context, hasKey("country"));
        assertEquals(JsonValue.ValueType.OBJECT, context.get("country").getValueType());
        final JsonObject termDef = context.getJsonObject("country");
        assertEquals(Vocabulary.ORIGIN, termDef.getString(JsonLd.ID));
        assertEquals(JsonLd.ID, termDef.getString(JsonLd.TYPE));
        assertEquals(instance.getCountry().toString(), json.getString("country"));
    }
}