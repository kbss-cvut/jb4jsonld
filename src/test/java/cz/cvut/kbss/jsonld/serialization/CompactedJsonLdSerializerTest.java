/**
 * Copyright (C) 2022 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.IdentifierUtil;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.TestUtil;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.*;
import cz.cvut.kbss.jsonld.exception.MissingIdentifierException;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.util.BufferedJsonGenerator;
import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
class CompactedJsonLdSerializerTest {

    private BufferedJsonGenerator jsonWriter;

    private JsonLdSerializer sut;

    @BeforeEach
    void setUp() {
        this.jsonWriter = new BufferedJsonGenerator();
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
    void testSerializeObjectWithSingularReference() throws Exception {
        final Employee employee = Generator.generateEmployee();
        sut.serialize(employee);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertNotNull(jsonObject);
        final Map<String, ?> map = (Map<String, ?>) jsonObject;
        verifyEmployee(employee, map);
    }

    @Test
    void testSerializeObjectWithPluralReference() throws Exception {
        final Organization org = Generator.generateOrganization();
        generateEmployees(org, false);  // No backward references for this test
        sut.configuration().set(ConfigParam.SERIALIZE_DATETIME_AS_MILLIS, Boolean.toString(true));
        sut.serialize(org);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertNotNull(jsonObject);
        final Map<String, ?> map = (Map<String, ?>) jsonObject;
        verifyOrganization(org, map);
    }

    private void generateEmployees(Organization org, boolean withBackwardReference) {
        for (int i = 0; i < Generator.randomCount(5, 10); i++) {
            final Employee emp = Generator.generateEmployee();
            emp.setEmployer(withBackwardReference ? org : null);
            org.addEmployee(emp);
        }
    }

    @Test
    void testSerializeObjectWithBackwardReferences() throws Exception {
        final Organization org = Generator.generateOrganization();
        generateEmployees(org, true);
        sut.configuration().set(ConfigParam.SERIALIZE_DATETIME_AS_MILLIS, Boolean.toString(true));
        sut.serialize(org);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertNotNull(jsonObject);
        final Map<String, ?> map = (Map<String, ?>) jsonObject;
        verifyOrganization(org, map);
    }

    @Test
    void testSerializeObjectWithPluralReferences() throws Exception {
        final Organization org = Generator.generateOrganization();
        generateEmployees(org, true);
        org.getEmployees().stream().filter(emp -> Generator.randomBoolean()).forEach(org::addAdmin);
        if (org.getAdmins() == null || org.getAdmins().isEmpty()) {
            org.setAdmins(new HashSet<>(Collections.singletonList(org.getEmployees().iterator().next())));
        }
        sut.configuration().set(ConfigParam.SERIALIZE_DATETIME_AS_MILLIS, Boolean.toString(true));
        sut.serialize(org);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertNotNull(jsonObject);
        final Map<String, ?> map = (Map<String, ?>) jsonObject;
        verifyOrganization(org, map);
    }

    private void verifyOrganization(Organization org, Map<String, ?> json) {
        assertEquals(org.getUri().toString(), json.get(JsonLd.ID));
        assertEquals(org.getDateCreated().getTime(), json.get(Vocabulary.DATE_CREATED));
        assertEquals(org.getBrands().size(), ((Collection<?>) json.get(Vocabulary.BRAND)).size());
        assertTrue(org.getBrands().containsAll((Collection<?>) json.get(Vocabulary.BRAND)));
        if (org.getAdmins() != null && !org.getAdmins().isEmpty()) {
            assertTrue(json.get(Vocabulary.HAS_ADMIN) instanceof List);
            final List<?> admins = (List<?>) json.get(Vocabulary.HAS_ADMIN);
            verifyOrganizationMembers(admins, org.getAdmins());
        }
        if (org.getEmployees() != null && !org.getEmployees().isEmpty()) {
            assertTrue(json.get(Vocabulary.HAS_MEMBER) instanceof List);
            final List<?> members = (List<?>) json.get(Vocabulary.HAS_MEMBER);
            verifyOrganizationMembers(members, org.getEmployees());
        }
        final List<?> types = (List<?>) json.get(JsonLd.TYPE);
        assertTrue(types.contains(Vocabulary.ORGANIZATION));
    }

    private void verifyOrganizationMembers(List<?> json, Set<Employee> members) {
        final Set<URI> memberUris = members.stream().map(Person::getUri).collect(Collectors.toSet());
        for (Object member : json) {
            assertTrue(member instanceof Map);
            final Map<?, ?> memberMap = (Map<?, ?>) member;
            final URI memberUri = URI.create(memberMap.get(JsonLd.ID).toString());
            assertTrue(memberUris.contains(memberUri));
            final Optional<Employee> e = members.stream().filter(emp -> emp.getUri().equals(memberUri))
                    .findFirst();
            assert e.isPresent();
            verifyEmployee(e.get(), memberMap);
        }
    }

    private void verifyEmployee(Employee employee, Map<?, ?> json) {
        assertEquals(employee.getUri().toString(), json.get(JsonLd.ID));
        if (json.size() > 1) {
            final List<?> types = (List<?>) json.get(JsonLd.TYPE);
            assertTrue(types.contains(Vocabulary.EMPLOYEE));
            assertTrue(types.contains(Vocabulary.USER));
            assertTrue(types.contains(Vocabulary.PERSON));
            assertEquals(employee.getFirstName(), json.get(Vocabulary.FIRST_NAME));
            assertEquals(employee.getLastName(), json.get(Vocabulary.LAST_NAME));
            assertEquals(employee.getUsername(), json.get(Vocabulary.USERNAME));
        }
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
    void testSerializationOfObjectWithStringBasedUnmappedProperties() throws Exception {
        final Person person = Generator.generatePerson();
        final Map<String, ?> json = serializeAndRead(person);
        assertEquals(person.getUri().toString(), json.get(JsonLd.ID));
        final List<?> types = (List<?>) json.get(JsonLd.TYPE);
        assertEquals(1, types.size());
        assertEquals(Vocabulary.PERSON, types.get(0));
        assertEquals(person.getFirstName(), json.get(Vocabulary.FIRST_NAME));
        assertEquals(person.getLastName(), json.get(Vocabulary.LAST_NAME));
        for (Map.Entry<String, Set<String>> entry : person.getProperties().entrySet()) {
            assertTrue(json.containsKey(entry.getKey()));
            final List<?> values = (List<?>) json.get(entry.getKey());
            assertNotNull(values);
            assertEquals(entry.getValue().size(), values.size());
            values.forEach(v -> assertTrue(entry.getValue().contains(v)));
        }
    }

    private Map<String, ?> serializeAndRead(Object value) throws IOException {
        sut.serialize(value);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertThat(jsonObject, instanceOf(Map.class));
        return (Map<String, ?>) jsonObject;
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
        assertThat(json.get(JsonLd.ID).toString(), StringStartsWith.startsWith("_:"));
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

    @SuppressWarnings("unused")
    @OWLClass(iri = Vocabulary.PERSON)
    private static class PersonWithoutIdentifier {

        @OWLDataProperty(iri = Vocabulary.FIRST_NAME)
        private String firstName;

        @OWLDataProperty(iri = Vocabulary.LAST_NAME)
        private String lastName;
    }

    @Test
    void serializationThrowsMissingIdentifierExceptionWhenNoIdentifierFieldIsFoundAndRequiredIdIsConfigured() {
        sut.configuration().set(ConfigParam.REQUIRE_ID, Boolean.TRUE.toString());
        final PersonWithoutIdentifier person = new PersonWithoutIdentifier();
        person.firstName = "Thomas";
        person.lastName = "Lasky";
        assertThrows(MissingIdentifierException.class, () -> sut.serialize(person));
    }

    @Test
    void serializationSkipsPropertiesWithWriteOnlyAccess() throws Exception {
        final User user = Generator.generateUser();
        user.setPassword("test-117");
        final Map<String, ?> json = serializeAndRead(user);
        assertFalse(json.containsKey(Vocabulary.PASSWORD));
    }

    @Test
    void serializationSerializesPropertyWithReadOnlyAccess() throws Exception {
        final Study study = new Study();
        study.setUri(Generator.generateUri());
        study.setName("Test study");
        study.setParticipants(Collections.singleton(Generator.generateEmployee()));
        study.setMembers(Collections.singleton(Generator.generateEmployee()));
        final Map<String, ?> json = serializeAndRead(study);
        assertTrue(json.containsKey(Vocabulary.NUMBER_OF_PEOPLE_INVOLVED));
        assertEquals(study.getNoOfPeopleInvolved(), json.get(Vocabulary.NUMBER_OF_PEOPLE_INVOLVED));
    }

    @Test
    void serializationSerializesAnnotationPropertyStringValueAsString() throws Exception {
        final ObjectWithAnnotationProperties toSerialize = new ObjectWithAnnotationProperties(Generator.generateUri());
        toSerialize.setChangedValue(Generator.generateUri().toString());

        final Map<String, ?> json = serializeAndRead(toSerialize);
        assertTrue(json.containsKey(Vocabulary.CHANGED_VALUE));
        assertEquals(toSerialize.getChangedValue(), json.get(Vocabulary.CHANGED_VALUE));
    }

    @Test
    void serializationSerializesUrisOfAnnotationPropertyAttributeAsObjectsWithId() throws Exception {
        final ObjectWithAnnotationProperties toSerialize = new ObjectWithAnnotationProperties(Generator.generateUri());
        toSerialize
                .setOrigins(IntStream.range(0, 5).mapToObj(i -> Generator.generateUri()).collect(Collectors.toSet()));

        final Map<String, ?> json = serializeAndRead(toSerialize);
        assertTrue(json.containsKey(Vocabulary.ORIGIN));
        final Object origins = json.get(Vocabulary.ORIGIN);
        assertThat(origins, instanceOf(List.class));
        final List<?> originList = (List<?>) origins;
        assertEquals(toSerialize.getOrigins().size(), originList.size());
        final Set<?> result = originList.stream().map(item -> {
            assertThat(item, instanceOf(Map.class));
            assertTrue(((Map<?, ?>) item).containsKey(JsonLd.ID));
            return ((Map<?, ?>) item).get(JsonLd.ID);
        }).collect(Collectors.toSet());
        assertEquals(toSerialize.getOrigins().stream().map(Object::toString).collect(Collectors.toSet()), result);
    }

    @Test
    void serializationSerializesEnumDataPropertyAsStringValueOfEnumConstant() throws Exception {
        final User user = Generator.generateUser();
        user.setRole(Role.ADMIN);
        final Map<String, ?> json = serializeAndRead(user);
        assertTrue(json.containsKey(Vocabulary.ROLE));
        assertEquals(Role.ADMIN.toString(), json.get(Vocabulary.ROLE));
    }

    @Test
    void serializationSerializesConcreteValueOfFieldOfTypeObject() throws Exception {
        final GenericMember instance = new GenericMember();
        instance.setUri(Generator.generateUri());
        instance.setMemberOf(Generator.generateOrganization());

        final Map<String, ?> json = serializeAndRead(instance);
        assertTrue(json.containsKey(Vocabulary.IS_MEMBER_OF));
        final Map<?, ?> org = (Map<?, ?>) json.get(Vocabulary.IS_MEMBER_OF);
        assertFalse(org.isEmpty());
        final List<String> types = (List<String>) org.get(JsonLd.TYPE);
        assertThat(types, hasItem(Vocabulary.ORGANIZATION));
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

        final Map<String, ?> json = serializeAndRead(instance);
        assertEquals(simpleValue, json.get(someProperty.toString()));
        final Object friendObject = json.get(Vocabulary.KNOWS);
        assertThat(friendObject, instanceOf(Map.class));
        final Map<String, ?> friendJson = (Map<String, ?>) friendObject;
        assertEquals(friend.getUri().toString(), friendJson.get(JsonLd.ID));
        assertEquals(friend.getFirstName(), friendJson.get(Vocabulary.FIRST_NAME));
        assertEquals(friend.getLastName(), friendJson.get(Vocabulary.LAST_NAME));
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

        final Map<String, ?> json = serializeAndRead(instance);
        assertEquals(simpleValue, json.get(someProperty.toString()));
        final Object friendObject = json.get(Vocabulary.KNOWS);
        assertThat(friendObject, instanceOf(Map.class));
        final Map<String, ?> friendJson = (Map<String, ?>) friendObject;
        assertEquals(friendId.toString(), friendJson.get(JsonLd.ID));
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
        sut.registerSerializer(LocalDate.class, ((value, ctx) -> JsonNodeFactory.createLiteralNode(ctx.getAttributeId(), value.toString())));
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
        final ValueSerializer<Organization> serializer = (value, ctx) -> JsonNodeFactory.createObjectIdNode(ctx.getAttributeId(), value.getUri());
        sut.registerSerializer(Organization.class, serializer);
        final Employee employee = Generator.generateEmployee();

        final Map<String, ?> json = serializeAndRead(employee);
        assertThat(json, hasKey(Vocabulary.IS_MEMBER_OF));
        assertEquals(employee.getEmployer().getUri().toString(), json.get(Vocabulary.IS_MEMBER_OF));
    }

    @Test
    void serializationSupportsUsageOfCustomObjectPropertyValueSerializersOnPluralAttributes() throws Exception {
        final ValueSerializer<Employee> serializer = (value, ctx) -> {
            final ObjectNode node = ctx.getAttributeId() != null ? JsonNodeFactory.createObjectNode(ctx.getAttributeId()) : JsonNodeFactory.createObjectNode();
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
