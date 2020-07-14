/**
 * Copyright (C) 2017 Czech Technical University in Prague
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

import com.github.jsonldjava.utils.JsonUtils;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.IdentifierUtil;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.*;
import cz.cvut.kbss.jsonld.exception.MissingIdentifierException;
import cz.cvut.kbss.jsonld.serialization.util.BufferedJsonGenerator;
import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
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
        sut.serialize(person);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        final Map<String, ?> json = (Map<String, ?>) jsonObject;
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

    @Test
    void serializationPutsOwlClassAndTypesContentIntoOneTypeProperty() throws Exception {
        final User user = Generator.generateUser();
        final String type = Generator.URI_BASE + "TypeOne";
        user.setTypes(Collections.singleton(type));
        sut.serialize(user);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        final Map<String, ?> json = (Map<String, ?>) jsonObject;
        final List<?> types = (List<?>) json.get(JsonLd.TYPE);
        assertTrue(types.contains(Vocabulary.USER));
        assertTrue(types.contains(type));
    }

    @Test
    void serializationSkipsNullDataPropertyValues() throws Exception {
        final User user = Generator.generateUser();
        user.setAdmin(null);
        sut.serialize(user);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        final Map<String, ?> json = (Map<String, ?>) jsonObject;
        assertFalse(json.containsKey(Vocabulary.IS_ADMIN));
    }

    @Test
    void serializationSkipsNullObjectPropertyValues() throws Exception {
        final Employee employee = Generator.generateEmployee();
        employee.setEmployer(null);
        sut.serialize(employee);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        final Map<String, ?> json = (Map<String, ?>) jsonObject;
        assertFalse(json.containsKey(Vocabulary.IS_MEMBER_OF));
    }

    @Test
    void serializationSerializesPlainIdentifierObjectPropertyValue() throws Exception {
        final Organization company = Generator.generateOrganization();
        company.setCountry(URI.create("http://dbpedia.org/resource/Czech_Republic"));
        sut.serialize(company);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        final Map<String, ?> json = (Map<String, ?>) jsonObject;
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
        sut.serialize(company);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        final Map<String, ?> json = (Map<String, ?>) jsonObject;
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
        sut.serialize(company);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        final Map<String, ?> json = (Map<String, ?>) jsonObject;
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
        sut.serialize(person);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        final Map<String, ?> json = (Map<String, ?>) jsonObject;
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
        sut.serialize(user);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        final Map<String, ?> json = (Map<String, ?>) jsonObject;
        assertFalse(json.containsKey(Vocabulary.PASSWORD));
    }

    @Test
    void serializationSerializesPropertyWithReadOnlyAccess() throws Exception {
        final Study study = new Study();
        study.setUri(Generator.generateUri());
        study.setName("Test study");
        study.setParticipants(Collections.singleton(Generator.generateEmployee()));
        study.setMembers(Collections.singleton(Generator.generateEmployee()));
        sut.serialize(study);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        final Map<String, ?> json = (Map<String, ?>) jsonObject;
        assertTrue(json.containsKey(Vocabulary.NUMBER_OF_PEOPLE_INVOLVED));
        assertEquals(study.getNoOfPeopleInvolved(), json.get(Vocabulary.NUMBER_OF_PEOPLE_INVOLVED));
    }

    @Test
    void serializationSerializesAnnotationPropertyStringValueAsString() throws Exception {
        final ObjectWithAnnotationProperties toSerialize = new ObjectWithAnnotationProperties(Generator.generateUri());
        toSerialize.setChangedValue(Generator.generateUri().toString());

        sut.serialize(toSerialize);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        final Map<String, ?> json = (Map<String, ?>) jsonObject;
        assertTrue(json.containsKey(Vocabulary.CHANGED_VALUE));
        assertEquals(toSerialize.getChangedValue(), json.get(Vocabulary.CHANGED_VALUE));
    }

    @Test
    void serializationSerializesUrisOfAnnotationPropertyAttributeAsObjectsWithId() throws Exception {
        final ObjectWithAnnotationProperties toSerialize = new ObjectWithAnnotationProperties(Generator.generateUri());
        toSerialize
                .setOrigins(IntStream.range(0, 5).mapToObj(i -> Generator.generateUri()).collect(Collectors.toSet()));

        sut.serialize(toSerialize);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        final Map<String, ?> json = (Map<String, ?>) jsonObject;
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
        sut.serialize(user);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        final Map<String, ?> json = (Map<String, ?>) jsonObject;
        assertTrue(json.containsKey(Vocabulary.ROLE));
        assertEquals(Role.ADMIN.toString(), json.get(Vocabulary.ROLE));
    }

    @Test
    void serializationSerializesConcreteValueOfFieldOfTypeObject() throws Exception {
        final GenericMember instance = new GenericMember();
        instance.setUri(Generator.generateUri());
        instance.setMemberOf(Generator.generateOrganization());

        sut.serialize(instance);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        final Map<String, ?> json = (Map<String, ?>) jsonObject;
        assertTrue(json.containsKey(Vocabulary.IS_MEMBER_OF));
        final Map<?, ?> org = (Map<?, ?>) json.get(Vocabulary.IS_MEMBER_OF);
        assertFalse(org.isEmpty());
        final List<String> types = (List<String>) org.get(JsonLd.TYPE);
        assertThat(types, hasItem(Vocabulary.ORGANIZATION));
    }
}
