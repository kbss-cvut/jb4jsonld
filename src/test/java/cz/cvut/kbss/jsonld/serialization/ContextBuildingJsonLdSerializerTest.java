package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.IdentifierUtil;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.TestUtil;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.*;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Models;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
class ContextBuildingJsonLdSerializerTest extends JsonLdSerializerTestBase {

    @BeforeEach
    void setUp() {
        this.sut = new ContextBuildingJsonLdSerializer(jsonWriter);
    }

    @Test
    void jsonLdContextContainsCorrectAttributeToPropertyMapping() throws Exception {
        final User user = Generator.generateUser();

        final Map<String, ?> jsonMap = serializeAndRead(user);
        assertThat(jsonMap, hasKey(JsonLd.CONTEXT));
        assertInstanceOf(Map.class, jsonMap.get(JsonLd.CONTEXT));
        final Map<String, String> context = (Map<String, String>) jsonMap.get(JsonLd.CONTEXT);
        assertEquals(Vocabulary.FIRST_NAME, context.get(User.getFirstNameField().getName()));
        assertEquals(Vocabulary.LAST_NAME, context.get(User.getLastNameField().getName()));
        assertEquals(Vocabulary.USERNAME, context.get(User.getUsernameField().getName()));
        assertEquals(Vocabulary.IS_ADMIN, context.get(User.class.getDeclaredField("admin").getName()));
    }

    @Test
    void serializeWithContextUsesFieldNamesAsJsonLdAttributeNames() throws Exception {
        final User user = Generator.generateUser();

        final Map<String, ?> jsonMap = serializeAndRead(user);
        assertEquals(user.getFirstName(), jsonMap.get(User.getFirstNameField().getName()));
        assertEquals(user.getLastName(), jsonMap.get(User.getLastNameField().getName()));
        assertEquals(user.getUsername(), jsonMap.get(User.getUsernameField().getName()));
        assertEquals(user.getAdmin(), jsonMap.get(User.class.getDeclaredField("admin").getName()));
    }

    @Test
    void serializeWithContextSupportsTypesAndIdMapping() throws Exception {
        final User user = Generator.generateUser();
        user.setTypes(Collections.singleton(Generator.generateUri().toString()));

        final Map<String, ?> jsonMap = serializeAndRead(user);
        assertThat(jsonMap, hasKey(JsonLd.CONTEXT));
        assertInstanceOf(Map.class, jsonMap.get(JsonLd.CONTEXT));
        final Map<String, String> context = (Map<String, String>) jsonMap.get(JsonLd.CONTEXT);
        assertEquals(JsonLd.ID, context.get(Person.class.getDeclaredField("uri").getName()));
        assertEquals(JsonLd.TYPE, context.get(User.class.getDeclaredField("types").getName()));
    }

    @Test
    void serializeWithContextBuildsContextForObjectProperty() throws Exception {
        final Employee employee = Generator.generateEmployee();

        final Map<String, ?> jsonMap = serializeAndRead(employee);
        assertThat(jsonMap, hasKey(JsonLd.CONTEXT));
        assertInstanceOf(Map.class, jsonMap.get(JsonLd.CONTEXT));
        final Map<String, String> context = (Map<String, String>) jsonMap.get(JsonLd.CONTEXT);
        assertEquals(Vocabulary.IS_MEMBER_OF, context.get(Employee.getEmployerField().getName()));
        assertEquals(Vocabulary.DATE_CREATED,
                     context.get(Organization.class.getDeclaredField("dateCreated").getName()));
        assertEquals(RDFS.LABEL, context.get(Organization.class.getDeclaredField("name").getName()));
        assertEquals(Vocabulary.BRAND, context.get(Organization.class.getDeclaredField("brands").getName()));
        assertEquals(Vocabulary.DATE_CREATED,
                     context.get(Organization.class.getDeclaredField("dateCreated").getName()));
    }

    @Test
    void serializeWithContextUsesReferencedEntityFieldNamesAsAttributeNames() throws Exception {
        final Employee employee = Generator.generateEmployee();

        final Map<String, ?> jsonMap = serializeAndRead(employee);
        assertInstanceOf(Map.class, jsonMap.get(Employee.getEmployerField().getName()));
        final Map<String, Object> orgMap = (Map<String, Object>) jsonMap.get(Employee.getEmployerField().getName());
        assertEquals(employee.getEmployer().getName(),
                     orgMap.get(Organization.class.getDeclaredField("name").getName()));
        assertInstanceOf(Map.class, orgMap.get(Organization.class.getDeclaredField("dateCreated").getName()));
        final Map<String, String> dateMap = new HashMap<>();
        dateMap.put(JsonLd.VALUE,
                    DateTimeFormatter.ISO_DATE_TIME.format(employee.getEmployer().getDateCreated().toInstant().atOffset(
                            ZoneOffset.UTC)));
        dateMap.put(JsonLd.TYPE, XSD.DATETIME);
        assertEquals(dateMap, orgMap.get(Organization.class.getDeclaredField("dateCreated").getName()));
        assertInstanceOf(Collection.class, orgMap.get(Organization.class.getDeclaredField("brands").getName()));
        final Collection<String> jsonBrands =
                (Collection<String>) orgMap.get(Organization.class.getDeclaredField("brands").getName());
        assertEquals(employee.getEmployer().getBrands().size(), jsonBrands.size());
        assertThat(jsonBrands, hasItems(employee.getEmployer().getBrands().toArray(new String[]{})));
    }

    @Test
    void serializationSkipsNullDataPropertyValues() throws Exception {
        final User user = Generator.generateUser();
        user.setAdmin(null);
        final Map<String, ?> json = serializeAndRead(user);
        assertThat(json, not(hasKey(User.class.getDeclaredField("admin").getName())));
    }

    @Test
    void serializationSkipsNullObjectPropertyValues() throws Exception {
        final Employee employee = Generator.generateEmployee();
        employee.setEmployer(null);
        final Map<String, ?> json = serializeAndRead(employee);
        assertThat(json, not(hasKey(Employee.getEmployerField().getName())));
    }

    @Test
    void serializeWithContextSupportsCompactedIrisBasedOnJOPANamespacesInContext() throws Exception {
        final StudyWithNamespaces study = new StudyWithNamespaces();
        study.setUri(Generator.generateUri());
        study.setName("Test study");
        study.setParticipants(Collections.singleton(Generator.generateEmployee()));
        study.setMembers(Collections.singleton(Generator.generateEmployee()));

        final Map<String, ?> json = serializeAndRead(study);
        final Map<String, String> context = (Map<String, String>) json.get(JsonLd.CONTEXT);
        assertEquals(RDFS.LABEL, context.get(StudyWithNamespaces.class.getDeclaredField("name").getName()));
        assertEquals(Vocabulary.HAS_PARTICIPANT,
                     context.get(StudyWithNamespaces.class.getDeclaredField("participants").getName()));
        assertEquals(Vocabulary.HAS_MEMBER,
                     context.get(StudyWithNamespaces.class.getDeclaredField("members").getName()));
    }

    @Test
    void serializationGeneratesBlankNodeIfInstancesDoesNotHaveIdentifierValue() throws Exception {
        final Organization company = Generator.generateOrganization();
        company.setUri(null);
        final Map<String, ?> json = serializeAndRead(company);
        assertThat(json, hasKey(TestUtil.ID_FIELD_NAME));
        assertThat(json.get(TestUtil.ID_FIELD_NAME).toString(), startsWith(IdentifierUtil.B_NODE_PREFIX));
    }

    @Test
    void serializationOfCollectionReturnsJsonObjectWithContextAndGraphWithSerializedCollection() throws Exception {
        final List<User> users =
                IntStream.range(0, 5).mapToObj(i -> Generator.generateUser()).collect(Collectors.toList());

        final Map<String, ?> json = serializeAndRead(users);
        assertThat(json, hasKey(JsonLd.CONTEXT));
        assertThat(json, hasKey(JsonLd.GRAPH));
        assertInstanceOf(List.class, json.get(JsonLd.GRAPH));
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

        final Map<String, ?> json = serializeAndRead(employees);
        final List<?> items = (List<?>) json.get(JsonLd.GRAPH);
        final Map<String, ?> eOne = (Map<String, ?>) items.get(0);
        final Map<String, ?> orgOne = (Map<String, ?>) eOne.get(Employee.getEmployerField().getName());
        assertThat(orgOne.size(), greaterThan(1));
        assertEquals(employees.get(0).getEmployer().getUri().toString(), orgOne.get(TestUtil.ID_FIELD_NAME));
        final Map<String, ?> eTwo = (Map<String, ?>) items.get(1);
        final Map<String, ?> orgTwo = (Map<String, ?>) eTwo.get(Employee.getEmployerField().getName());
        assertEquals(Collections.singletonMap(TestUtil.ID_FIELD_NAME, employees.get(1).getEmployer().getUri().toString()), orgTwo);

        final Model result = readJson(jsonWriter.getResult());
        final Model orgModel = toRdf(employees.get(0).getEmployer());
        assertTrue(Models.isSubset(orgModel, result));
    }
}