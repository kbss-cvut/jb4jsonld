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
package cz.cvut.kbss.jsonld.serialization;

import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.core.JsonLdUtils;
import com.github.jsonldjava.utils.JsonUtils;
import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.IdentifierUtil;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.TestUtil;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.*;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import org.eclipse.rdf4j.model.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;

import static cz.cvut.kbss.jsonld.environment.IsIsomorphic.isIsomorphic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
class CompactedJsonLdSerializerTest extends JsonLdSerializerTestBase {

    @BeforeEach
    void setUp() {
        this.sut = new CompactedJsonLdSerializer(jsonWriter);
    }

    // The following tests only verify validity of the output JSON-LD, no structure checks are performed

    @Test
    void testSerializeObjectWithDataProperties() throws Exception {
        final User user = Generator.generateUser();
        sut.serialize(user);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertNotNull(jsonObject);
    }

    @Test
    void testSerializeCollectionOfObjects() throws Exception {
        final Set<User> users = Generator.generateUsers();
        sut.serialize(users);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertNotNull(jsonObject);
    }

    @Test
    void serializationOfCollectionOfInstancesReferencingSameInstanceUsesReferenceNode() throws Exception {
        final Organization org = Generator.generateOrganization();
        generateEmployees(org, true);
        final Set<Employee> employees = org.getEmployees();
        org.setEmployees(null);
        sut.serialize(employees);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertNotNull(jsonObject);
        final List<?> jsonList = (List<?>) jsonObject;
        for (int i = 1; i < jsonList.size(); i++) { // Start from 1, the first one contains the full object
            final Map<?, ?> item = (Map<?, ?>) jsonList.get(i);
            assertTrue(item.get(Vocabulary.IS_MEMBER_OF) instanceof Map);
            final Map<?, ?> map = (Map<?, ?>) item.get(Vocabulary.IS_MEMBER_OF);
            assertEquals(1, map.size());
            assertEquals(org.getUri().toString(), map.get(JsonLd.ID));
        }
    }

    @Test
    void serializationPutsOwlClassAndTypesContentIntoOneTypeProperty() throws Exception {
        final User user = Generator.generateUser();
        final String type = Generator.URI_BASE + "TypeOne";
        user.setTypes(Collections.singleton(type));
        final Map<String, ?> json = serializeAndRead(user);
        final List<?> types = (List<?>) json.get(JsonLd.TYPE);
        assertTrue(types.contains(Vocabulary.USER));
        assertTrue(types.contains(type));
    }

    private Map<String, ?> serializeAndRead(Object value) throws IOException {
        sut.serialize(value);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertThat(jsonObject, instanceOf(Map.class));
        return (Map<String, ?>) jsonObject;
    }

    @Test
    void serializationSkipsNullDataPropertyValues() throws Exception {
        final User user = Generator.generateUser();
        user.setAdmin(null);
        final Map<String, ?> json = serializeAndRead(user);
        assertFalse(json.containsKey(Vocabulary.IS_ADMIN));
    }

    @Test
    void serializationSkipsNullObjectPropertyValues() throws Exception {
        final Employee employee = Generator.generateEmployee();
        employee.setEmployer(null);
        final Map<String, ?> json = serializeAndRead(employee);
        assertFalse(json.containsKey(Vocabulary.IS_MEMBER_OF));
    }

    @Test
    void serializationSerializesPlainIdentifierObjectPropertyValue() throws Exception {
        final Organization company = Generator.generateOrganization();
        company.setCountry(URI.create("http://dbpedia.org/resource/Czech_Republic"));
        final Map<String, ?> json = serializeAndRead(company);
        final Object value = json.get(Vocabulary.ORIGIN);
        assertTrue(value instanceof Map);
        final Map<String, ?> country = (Map<String, ?>) value;
        assertEquals(1, country.size());
        assertEquals(company.getCountry().toString(), country.get(JsonLd.ID));
    }

    @Test
    void serializationGeneratesBlankNodeIfInstancesDoesNotHaveIdentifierValue() throws Exception {
        final Organization company = Generator.generateOrganization();
        company.setUri(null);
        final Map<String, ?> json = serializeAndRead(company);
        assertTrue(json.containsKey(JsonLd.ID));
        assertThat(json.get(JsonLd.ID).toString(), startsWith(IdentifierUtil.B_NODE_PREFIX));
    }

    @Test
    void serializationUsesGeneratedBlankNodeForObjectReference() throws Exception {
        final Organization company = Generator.generateOrganization();
        company.setUri(null);
        final Employee employee = Generator.generateEmployee();
        employee.setEmployer(company);
        company.addEmployee(employee);
        final Map<String, ?> json = serializeAndRead(company);
        final String id = (String) json.get(JsonLd.ID);
        final List<?> employees = (List<?>) json.get(Vocabulary.HAS_MEMBER);
        for (Object e : employees) {
            final Map<?, ?> eMap = (Map<?, ?>) e;
            final Map<?, ?> employer = (Map<?, ?>) eMap.get(Vocabulary.IS_MEMBER_OF);
            assertEquals(id, employer.get(JsonLd.ID));
        }
    }

    @Test
    void serializationGeneratesBlankNodeIdentifierForInstanceOfClassWithoutIdentifierField() throws Exception {
        final PersonWithoutIdentifier person = new PersonWithoutIdentifier();
        person.firstName = "Thomas";
        person.lastName = "Lasky";
        final Map<String, ?> json = serializeAndRead(person);
        final String id = (String) json.get(JsonLd.ID);
        assertNotNull(id);
        assertThat(id, startsWith(IdentifierUtil.B_NODE_PREFIX));
    }

    @Test
    void serializationSerializesMultilingualStringWithValues() throws Exception {
        final ObjectWithMultilingualString instance = new ObjectWithMultilingualString(Generator.generateUri());
        final MultilingualString name = new MultilingualString();
        name.set("en", "Leveraging Semantic Web Technologies in Domain-specific Information Systems");
        name.set("cs", "Využití technologií sémantického webu v doménových informačních systémech");
        instance.setLabel(name);

        final Map<String, ?> json = serializeAndRead(instance);
        assertTrue(json.containsKey(RDFS.LABEL));
        final List<?> label = (List<?>) json.get(RDFS.LABEL);
        assertEquals(name.getValue().size(), label.size());
        for (Object item : label) {
            assertThat(item, instanceOf(Map.class));
            final Map<?, ?> m = (Map<?, ?>) item;
            assertTrue(m.containsKey(JsonLd.LANGUAGE));
            assertTrue(m.containsKey(JsonLd.VALUE));
            assertTrue(name.contains(m.get(JsonLd.LANGUAGE).toString()));
            assertEquals(name.get(m.get(JsonLd.LANGUAGE).toString()), m.get(JsonLd.VALUE));
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
        final Object resultExpanded = JsonLdProcessor.expand(JsonUtils.fromString(jsonWriter.getResult()));
        final Object expectedExpanded = TestUtil.readAndExpand("objectWithPluralMultilingualString.json");
        assertTrue(JsonLdUtils.deepCompare(expectedExpanded, resultExpanded));
    }

    @Test
    void serializationSerializesIndividualsInTypedUnmappedPropertiesAsObjects() throws Exception {
        final PersonWithTypedProperties instance = new PersonWithTypedProperties();
        instance.setUri(Generator.generateUri());
        instance.setFirstName("Sarah");
        instance.setLastName("Palmer");
        instance.setProperties(new HashMap<>());
        final URI someProperty = Generator.generateUri();
        final String simpleValue = "Simple string value";
        instance.getProperties().put(someProperty, Collections.singleton(simpleValue));
        final Person friend = Generator.generatePerson();
        instance.getProperties().put(URI.create(Vocabulary.KNOWS), Collections.singleton(friend));

        sut.serialize(instance);
        final Model expected = toRdf(instance);
        final Model actual = readJson(jsonWriter.getResult());
        assertThat(actual, isIsomorphic(expected));
    }

    @Test
    void serializationSerializesIdentifierInTypedUnmappedPropertiesAsObjectsWithId() throws Exception {
        final PersonWithTypedProperties instance = new PersonWithTypedProperties();
        instance.setUri(Generator.generateUri());
        instance.setFirstName("Sarah");
        instance.setLastName("Palmer");
        instance.setProperties(new HashMap<>());
        final URI someProperty = Generator.generateUri();
        final Integer simpleValue = 4;
        instance.getProperties().put(someProperty, Collections.singleton(simpleValue));
        final URI friendId = Generator.generateUri();
        instance.getProperties().put(URI.create(Vocabulary.KNOWS), Collections.singleton(friendId));

        sut.serialize(instance);
        final Model expected = toRdf(instance);
        final Model actual = readJson(jsonWriter.getResult());
        assertThat(actual, isIsomorphic(expected));
    }

    @Test
    void serializationSerializesMultilingualStringInTypedUnmappedProperties() throws Exception {
        final PersonWithTypedProperties instance = new PersonWithTypedProperties();
        instance.setUri(Generator.generateUri());
        final MultilingualString ms = MultilingualString.create("en", "Falcon");
        ms.set("cs", "Sokol");
        final URI property = URI.create("http://xmlns.com/foaf/0.1/nick");
        instance.setProperties(Collections.singletonMap(property, Collections.singleton(ms)));

        final Map<String, ?> json = serializeAndRead(instance);
        assertTrue(json.containsKey(property.toString()));
        final List<?> nick = (List<?>) json.get(property.toString());
        assertEquals(ms.getValue().size(), nick.size());
        for (Object item : nick) {
            assertThat(item, instanceOf(Map.class));
            final Map<?, ?> m = (Map<?, ?>) item;
            assertTrue(m.containsKey(JsonLd.LANGUAGE));
            assertTrue(m.containsKey(JsonLd.VALUE));
            assertTrue(ms.contains(m.get(JsonLd.LANGUAGE).toString()));
            assertEquals(ms.get(m.get(JsonLd.LANGUAGE).toString()), m.get(JsonLd.VALUE));
        }
    }

    @Test
    void serializationSupportsCompactedIrisBasedOnJOPANamespaces() throws Exception {
        final Study study = new Study();
        study.setUri(Generator.generateUri());
        study.setName("Test study");
        study.setParticipants(Collections.singleton(Generator.generateEmployee()));
        study.setMembers(Collections.singleton(Generator.generateEmployee()));

        final Map<String, ?> json = serializeAndRead(study);
        assertThat(json, hasKey(RDFS.LABEL));
        assertThat(json, hasKey(Vocabulary.HAS_PARTICIPANT));
        assertThat(json, hasKey(Vocabulary.HAS_MEMBER));
    }

    @Test
    void serializationSupportsRegistrationAndUsageOfCustomSerializers() throws Exception {
        sut.registerSerializer(LocalDate.class, ((value, ctx) -> JsonNodeFactory.createLiteralNode(ctx.getAttributeId(),
                                                                                                   value.toString())));
        final OrganizationWithLocalDate organization = new OrganizationWithLocalDate();
        organization.uri = Generator.generateUri();
        organization.created = LocalDate.now();

        final Map<String, ?> json = serializeAndRead(organization);
        assertThat(json, hasKey(Vocabulary.DATE_CREATED));
        assertEquals(organization.created.toString(), json.get(Vocabulary.DATE_CREATED));
    }

    @SuppressWarnings("unused")
    @OWLClass(iri = Vocabulary.ORGANIZATION)
    public static class OrganizationWithLocalDate {
        @Id
        private URI uri;

        @OWLDataProperty(iri = Vocabulary.DATE_CREATED)
        private LocalDate created;
    }

    @Test
    void serializationSupportsRegistrationAndUsageOfCustomObjectPropertyValueSerializers() throws Exception {
        final ValueSerializer<Organization> serializer =
                (value, ctx) -> JsonNodeFactory.createObjectIdNode(ctx.getAttributeId(), value.getUri());
        sut.registerSerializer(Organization.class, serializer);
        final Employee employee = Generator.generateEmployee();

        final Map<String, ?> json = serializeAndRead(employee);
        assertThat(json, hasKey(Vocabulary.IS_MEMBER_OF));
        assertEquals(employee.getEmployer().getUri().toString(), json.get(Vocabulary.IS_MEMBER_OF));
    }

    @Test
    void serializationSupportsUsageOfCustomObjectPropertyValueSerializersOnPluralAttributes() throws Exception {
        final ValueSerializer<Employee> serializer = (value, ctx) -> {
            final ObjectNode node =
                    ctx.getAttributeId() != null ? JsonNodeFactory.createObjectNode(ctx.getAttributeId()) :
                    JsonNodeFactory.createObjectNode();
            node.addItem(JsonNodeFactory.createObjectIdNode(JsonLd.ID, value.getUri().toString()));
            node.addItem(JsonNodeFactory.createLiteralNode(Vocabulary.USERNAME, value.getUsername()));
            return node;
        };
        sut.registerSerializer(Employee.class, serializer);
        final Organization organization = Generator.generateOrganization();
        final Employee eOne = Generator.generateEmployee();
        eOne.setEmployer(organization);
        final Employee eTwo = Generator.generateEmployee();
        eTwo.setEmployer(organization);
        organization.setEmployees(new LinkedHashSet<>(Arrays.asList(eOne, eTwo)));

        final Map<String, ?> json = serializeAndRead(organization);
        assertThat(json, hasKey(Vocabulary.HAS_MEMBER));
        assertThat(json.get(Vocabulary.HAS_MEMBER), instanceOf(List.class));
        final List<?> employees = (List<?>) json.get(Vocabulary.HAS_MEMBER);
        assertEquals(organization.getEmployees().size(), employees.size());
        final Iterator<Employee> itExp = organization.getEmployees().iterator();
        final Iterator<?> itRes = employees.iterator();
        while (itExp.hasNext() && itRes.hasNext()) {
            final Employee exp = itExp.next();
            final Map<String, ?> res = (Map<String, ?>) itRes.next();
            assertEquals(res.size(), 2);
            assertEquals(exp.getUri().toString(), res.get(JsonLd.ID));
            assertEquals(exp.getUsername(), res.get(Vocabulary.USERNAME));
        }
    }

    /**
     * Bug #36
     */
    @Test
    void serializationSerializesMultilingualStringWithLanguageLessValue() throws Exception {
        final ObjectWithMultilingualString instance = new ObjectWithMultilingualString(Generator.generateUri());
        final MultilingualString name = new MultilingualString();
        name.set("en", "Value in English");
        name.set("cs", "Hodnota v češtině");
        name.set("Default value");
        instance.setLabel(name);

        final Map<String, ?> json = serializeAndRead(instance);
        assertTrue(json.containsKey(RDFS.LABEL));
        final List<?> label = (List<?>) json.get(RDFS.LABEL);
        assertEquals(name.getValue().size(), label.size());
        final Optional<Map<?, ?>> result = (Optional<Map<?, ?>>) label.stream().filter(item -> {
            assertThat(item, instanceOf(Map.class));
            final Map<?, ?> m = (Map<?, ?>) item;
            return Objects.equals(m.get(JsonLd.LANGUAGE), JsonLd.NONE);
        }).findAny();
        assertTrue(result.isPresent());
        assertEquals(name.get(), result.get().get(JsonLd.VALUE));
    }
}
