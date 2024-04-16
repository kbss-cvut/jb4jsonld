/*
 * JB4JSON-LD
 * Copyright (C) 2024 Czech Technical University in Prague
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
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jopa.vocabulary.SKOS;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.IdentifierUtil;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.TestUtil;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.ObjectWithMultilingualString;
import cz.cvut.kbss.jsonld.environment.model.ObjectWithPluralMultilingualString;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.OwlPropertyType;
import cz.cvut.kbss.jsonld.environment.model.PersonWithTypedProperties;
import cz.cvut.kbss.jsonld.environment.model.StudyWithNamespaces;
import cz.cvut.kbss.jsonld.environment.model.User;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompactedJsonLdSerializerTest extends JsonLdSerializerTestBase {

    @BeforeEach
    void setUp() {
        this.sut = new CompactedJsonLdSerializer(jsonWriter);
    }

    @Test
    void testSerializeCollectionOfObjects() {
        final Set<User> users = Generator.generateUsers();
        final JsonValue jsonObject = serializeAndRead(users);
        assertEquals(JsonValue.ValueType.ARRAY, jsonObject.getValueType());
    }

    @Test
    void serializationOfCollectionOfInstancesReferencingSameInstanceUsesReferenceNode() {
        final Organization org = Generator.generateOrganization();
        generateEmployees(org, true);
        final Set<Employee> employees = org.getEmployees();
        org.setEmployees(null);
        final JsonValue jsonObject = serializeAndRead(employees);
        assertEquals(JsonValue.ValueType.ARRAY, jsonObject.getValueType());
        final JsonArray jsonList = jsonObject.asJsonArray();
        for (int i = 1; i < jsonList.size(); i++) { // Start from 1, the first one contains the full object
            final JsonObject item = jsonList.getJsonObject(i);
            assertEquals(JsonValue.ValueType.OBJECT, item.get(Vocabulary.IS_MEMBER_OF).getValueType());
            final JsonObject map = item.getJsonObject(Vocabulary.IS_MEMBER_OF);
            assertEquals(1, map.size());
            assertEquals(org.getUri().toString(), map.getString(JsonLd.ID));
        }
    }

    @Test
    void serializationSkipsNullDataPropertyValues() {
        final User user = Generator.generateUser();
        user.setAdmin(null);
        final JsonValue json = serializeAndRead(user);
        assertEquals(JsonValue.ValueType.OBJECT, json.getValueType());
        assertThat(json.asJsonObject(), not(hasKey(Vocabulary.IS_ADMIN)));
    }

    @Test
    void serializationSkipsNullObjectPropertyValues() {
        final Employee employee = Generator.generateEmployee();
        employee.setEmployer(null);
        final JsonValue json = serializeAndRead(employee);
        assertEquals(JsonValue.ValueType.OBJECT, json.getValueType());
        assertThat(json.asJsonObject(), not(hasKey(Vocabulary.IS_MEMBER_OF)));
    }

    @Test
    void serializationGeneratesBlankNodeIfInstancesDoesNotHaveIdentifierValue() {
        final Organization company = Generator.generateOrganization();
        company.setUri(null);
        final JsonValue json = serializeAndRead(company);
        assertEquals(JsonValue.ValueType.OBJECT, json.getValueType());
        assertThat(json.asJsonObject(), hasKey(JsonLd.ID));
        assertThat(json.asJsonObject().getString(JsonLd.ID), startsWith(IdentifierUtil.B_NODE_PREFIX));
    }

    @Test
    void serializationSerializesMultilingualStringWithValues() {
        final ObjectWithMultilingualString instance = new ObjectWithMultilingualString(Generator.generateUri());
        final MultilingualString name = new MultilingualString();
        name.set("en", "Leveraging Semantic Web Technologies in Domain-specific Information Systems");
        name.set("cs", "Využití technologií sémantického webu v doménových informačních systémech");
        instance.setLabel(name);

        final JsonValue json = serializeAndRead(instance);
        assertEquals(JsonValue.ValueType.OBJECT, json.getValueType());
        assertTrue(json.asJsonObject().containsKey(RDFS.LABEL));
        final JsonArray label = json.asJsonObject().getJsonArray(RDFS.LABEL);
        assertEquals(name.getValue().size(), label.size());
        for (JsonValue item : label) {
            assertEquals(JsonValue.ValueType.OBJECT, item.getValueType());
            final JsonObject m = item.asJsonObject();
            assertTrue(m.containsKey(JsonLd.LANGUAGE));
            assertTrue(m.containsKey(JsonLd.VALUE));
            assertTrue(name.contains(m.getString(JsonLd.LANGUAGE)));
            assertEquals(name.get(m.getString(JsonLd.LANGUAGE)), m.getString(JsonLd.VALUE));
        }
    }

    @Test
    void serializationSerializesPluralMultilingualString() throws Exception {
        final ObjectWithPluralMultilingualString instance = new ObjectWithPluralMultilingualString(
                URI.create("http://onto.fel.cvut.cz/ontologies/jb4json-ld/concept#instance-117711"));
        final MultilingualString one = new MultilingualString();
        one.set("en", "Building");
        one.set("cs", "Budova");
        final MultilingualString two = new MultilingualString();
        two.set("en", "Construction");
        two.set("cs", "Stavba");
        instance.setAltLabel(new HashSet<>(Arrays.asList(one, two)));

        sut.serialize(instance);
        final JsonArray resultExpanded = TestUtil.parseAndExpand(jsonWriter.getResult());
        final JsonArray strCol = resultExpanded.getJsonObject(0).getJsonArray(SKOS.ALT_LABEL);
        instance.getAltLabel().forEach(ms -> ms.getValue()
                                               .forEach((lang, lex) -> assertTrue(strCol.stream()
                                                                                        .anyMatch(val -> lex.equals(
                                                                                                val.asJsonObject()
                                                                                                   .getString(
                                                                                                           JsonLd.VALUE)) && lang.equals(
                                                                                                val.asJsonObject()
                                                                                                   .getString(
                                                                                                           JsonLd.LANGUAGE))))));
    }

    @Test
    void serializationSerializesMultilingualStringInTypedUnmappedProperties() {
        final PersonWithTypedProperties instance = new PersonWithTypedProperties();
        instance.setUri(Generator.generateUri());
        final MultilingualString ms = MultilingualString.create("en", "Falcon");
        ms.set("cs", "Sokol");
        final URI property = URI.create("http://xmlns.com/foaf/0.1/nick");
        instance.setProperties(Collections.singletonMap(property, Collections.singleton(ms)));

        final JsonValue json = serializeAndRead(instance);
        assertEquals(JsonValue.ValueType.OBJECT, json.getValueType());
        assertTrue(json.asJsonObject().containsKey(property.toString()));
        final JsonArray nick = json.asJsonObject().getJsonArray(property.toString());
        assertEquals(ms.getValue().size(), nick.size());
        for (JsonValue item : nick) {
            assertEquals(JsonValue.ValueType.OBJECT, item.getValueType());
            final JsonObject m = item.asJsonObject();
            assertTrue(m.containsKey(JsonLd.LANGUAGE));
            assertTrue(m.containsKey(JsonLd.VALUE));
            assertTrue(ms.contains(m.getString(JsonLd.LANGUAGE)));
            assertEquals(ms.get(m.getString(JsonLd.LANGUAGE)), m.getString(JsonLd.VALUE));
        }
    }

    @Test
    void serializationSupportsCompactedIrisBasedOnJOPANamespaces() {
        final StudyWithNamespaces study = new StudyWithNamespaces();
        study.setUri(Generator.generateUri());
        study.setName("Test study");
        study.setParticipants(Collections.singleton(Generator.generateEmployee()));
        study.setMembers(Collections.singleton(Generator.generateEmployee()));

        final JsonValue json = serializeAndRead(study);
        assertEquals(JsonValue.ValueType.OBJECT, json.getValueType());
        assertThat(json.asJsonObject(), hasKey(RDFS.LABEL));
        assertThat(json.asJsonObject(), hasKey(Vocabulary.HAS_PARTICIPANT));
        assertThat(json.asJsonObject(), hasKey(Vocabulary.HAS_MEMBER));
    }

    /**
     * Bug #36
     */
    @Test
    void serializationSerializesMultilingualStringWithLanguageLessValue() {
        final ObjectWithMultilingualString instance = new ObjectWithMultilingualString(Generator.generateUri());
        final MultilingualString name = new MultilingualString();
        name.set("en", "Value in English");
        name.set("cs", "Hodnota v češtině");
        name.set("Default value");
        instance.setLabel(name);

        final JsonValue json = serializeAndRead(instance);
        assertEquals(JsonValue.ValueType.OBJECT, json.getValueType());
        assertTrue(json.asJsonObject().containsKey(RDFS.LABEL));
        final JsonArray label = json.asJsonObject().getJsonArray(RDFS.LABEL);
        assertEquals(name.getValue().size(), label.size());
        final Optional<JsonValue> result = label.stream().filter(item -> {
            assertEquals(JsonValue.ValueType.OBJECT, item.getValueType());
            final JsonObject m = item.asJsonObject();
            return Objects.equals(m.getString(JsonLd.LANGUAGE), JsonLd.NONE);
        }).findAny();
        assertTrue(result.isPresent());
        assertEquals(name.get(), result.get().asJsonObject().getString(JsonLd.VALUE));
    }

    @Test
    void serializationSerializesRootCollectionOfEnumConstantsMappedToIndividualsAsArrayOfIndividuals() {
        final List<OwlPropertyType> value = Arrays.asList(OwlPropertyType.values());

        final JsonValue jsonObject = serializeAndRead(new LinkedHashSet<>(value));
        assertEquals(JsonValue.ValueType.ARRAY, jsonObject.getValueType());
        final JsonArray lst = jsonObject.asJsonArray();
        assertEquals(value.size(), lst.size());
        for (int i = 0; i < value.size(); i++) {
            final JsonObject element = lst.getJsonObject(i);
            assertThat(element, hasKey(JsonLd.ID));
            assertEquals(OwlPropertyType.getMappedIndividual(value.get(i)), element.getString(JsonLd.ID));
        }
    }
}
