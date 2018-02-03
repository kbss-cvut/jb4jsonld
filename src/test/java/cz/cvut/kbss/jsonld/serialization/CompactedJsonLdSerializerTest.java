/**
 * Copyright (C) 2017 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.serialization;

import com.github.jsonldjava.utils.JsonUtils;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.environment.model.User;
import cz.cvut.kbss.jsonld.serialization.util.BufferedJsonGenerator;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class CompactedJsonLdSerializerTest {

    private BufferedJsonGenerator jsonWriter;

    private JsonLdSerializer serializer;

    @Before
    public void setUp() {
        this.jsonWriter = new BufferedJsonGenerator();
        this.serializer = new CompactedJsonLdSerializer(jsonWriter);
    }

    // The following tests only verify validity of the output JSON-LD, no structure checks are performed

    @Test
    public void testSerializeObjectWithDataProperties() throws Exception {
        final User user = Generator.generateUser();
        serializer.serialize(user);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertNotNull(jsonObject);
    }

    @Test
    public void testSerializeCollectionOfObjects() throws Exception {
        final Set<User> users = Generator.generateUsers();
        serializer.serialize(users);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertNotNull(jsonObject);
    }

    @Test
    public void testSerializeObjectWithSingularReference() throws Exception {
        final Employee employee = Generator.generateEmployee();
        serializer.serialize(employee);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertNotNull(jsonObject);
        final Map<String, ?> map = (Map<String, ?>) jsonObject;
        verifyEmployee(employee, map);
    }

    @Test
    public void testSerializeObjectWithPluralReference() throws Exception {
        final Organization org = Generator.generateOrganization();
        generateEmployees(org, false);  // No backward references for this test
        serializer.serialize(org);
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
    public void testSerializeObjectWithBackwardReferences() throws Exception {
        final Organization org = Generator.generateOrganization();
        generateEmployees(org, true);
        serializer.serialize(org);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertNotNull(jsonObject);
        final Map<String, ?> map = (Map<String, ?>) jsonObject;
        verifyOrganization(org, map);
    }

    @Test
    public void testSerializeObjectWithPluralReferences() throws Exception {
        final Organization org = Generator.generateOrganization();
        generateEmployees(org, true);
        org.getEmployees().stream().filter(emp -> Generator.randomBoolean()).forEach(org::addAdmin);
        if (org.getAdmins() == null || org.getAdmins().isEmpty()) {
            org.setAdmins(new HashSet<>(Collections.singletonList(org.getEmployees().iterator().next())));
        }
        serializer.serialize(org);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertNotNull(jsonObject);
        final Map<String, ?> map = (Map<String, ?>) jsonObject;
        verifyOrganization(org, map);
    }

    private void verifyOrganization(Organization org, Map<String, ?> json) {
        assertEquals(org.getUri().toString(), json.get(JsonLd.ID));
        assertEquals(org.getDateCreated().toString(), json.get(Vocabulary.DATE_CREATED));
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
            if (member instanceof String) {
                assertTrue(memberUris.contains(URI.create(member.toString())));
            } else {
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
    }

    private void verifyEmployee(Employee employee, Map<?, ?> json) {
        assertEquals(employee.getUri().toString(), json.get(JsonLd.ID));
        final List<?> types = (List<?>) json.get(JsonLd.TYPE);
        assertTrue(types.contains(Vocabulary.EMPLOYEE));
        assertTrue(types.contains(Vocabulary.USER));
        assertTrue(types.contains(Vocabulary.PERSON));
        assertEquals(employee.getFirstName(), json.get(Vocabulary.FIRST_NAME));
        assertEquals(employee.getLastName(), json.get(Vocabulary.LAST_NAME));
        assertEquals(employee.getUsername(), json.get(Vocabulary.USERNAME));
    }

    @Test
    public void serializationOfCollectionOfInstancesReferencingSameInstanceUsesReferenceUri() throws Exception {
        final Organization org = Generator.generateOrganization();
        generateEmployees(org, true);
        final Set<Employee> employees = org.getEmployees();
        org.setEmployees(null);
        serializer.serialize(employees);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertNotNull(jsonObject);
        final List<?> jsonList = (List<?>) jsonObject;
        for (int i = 0; i < jsonList.size(); i++) {
            final Map<?, ?> item = (Map<?, ?>) jsonList.get(i);
            if (i == 0) {
                assertTrue(item.get(Vocabulary.IS_MEMBER_OF) instanceof Map);
            } else {
                assertTrue(item.get(Vocabulary.IS_MEMBER_OF) instanceof String);
                assertEquals(org.getUri(), URI.create(item.get(Vocabulary.IS_MEMBER_OF).toString()));
            }
        }
    }

    @Test
    public void testSerializationOfObjectWithStringBasedUnmappedProperties() throws Exception {
        final Person person = Generator.generatePerson();
        serializer.serialize(person);
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
    public void serializationPutsOwlClassAndTypesContentIntoOneTypeProperty() throws Exception {
        final User user = Generator.generateUser();
        final String type = Generator.URI_BASE + "TypeOne";
        user.setTypes(Collections.singleton(type));
        serializer.serialize(user);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        final Map<String, ?> json = (Map<String, ?>) jsonObject;
        final List<?> types = (List<?>) json.get(JsonLd.TYPE);
        assertTrue(types.contains(Vocabulary.USER));
        assertTrue(types.contains(type));
    }

    @Test
    public void serializationSkipsNullDataPropertyValues() throws Exception {
        final User user = Generator.generateUser();
        user.setAdmin(null);
        serializer.serialize(user);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        final Map<String, ?> json = (Map<String, ?>) jsonObject;
        assertFalse(json.containsKey(Vocabulary.IS_ADMIN));
    }

    @Test
    public void serializationSkipsNullObjectPropertyValues() throws Exception {
        final Employee employee = Generator.generateEmployee();
        employee.setEmployer(null);
        serializer.serialize(employee);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        final Map<String, ?> json = (Map<String, ?>) jsonObject;
        assertFalse(json.containsKey(Vocabulary.IS_MEMBER_OF));
    }

    @Test
    public void serializationSerializesPlainIdentifierObjectPropertyValue() throws Exception {
        final Organization company = Generator.generateOrganization();
        company.setCountry(URI.create("http://dbpedia.org/resource/Czech_Republic"));
        serializer.serialize(company);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        final Map<String, ?> json = (Map<String, ?>) jsonObject;
        final Object value = json.get(Vocabulary.ORIGIN);
        assertTrue(value instanceof Map);
        final Map<String, ?> country = (Map<String, ?>) value;
        assertEquals(1, country.size());
        assertEquals(company.getCountry().toString(), country.get(JsonLd.ID));
    }
}
