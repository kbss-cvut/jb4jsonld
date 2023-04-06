package cz.cvut.kbss.jsonld.serialization;

import com.github.jsonldjava.utils.JsonUtils;
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
import cz.cvut.kbss.jsonld.environment.model.*;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Models;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
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
        final Map<String, ?> context = (Map<String, JsonNode>) jsonMap.get(JsonLd.CONTEXT);
        assertEquals(Vocabulary.IS_MEMBER_OF, context.get(Employee.getEmployerField().getName()));
        assertEquals(RDFS.LABEL, context.get(Organization.class.getDeclaredField("name").getName()));
        assertEquals(Vocabulary.BRAND, context.get(Organization.class.getDeclaredField("brands").getName()));
        final Object dateDefinition = context.get(Organization.class.getDeclaredField("dateCreated").getName());
        assertInstanceOf(Map.class, dateDefinition);
        final Map<String, String> expected = new HashMap<>();
        expected.put(JsonLd.TYPE, XSD.DATETIME);
        expected.put(JsonLd.ID, Vocabulary.DATE_CREATED);
        assertEquals(expected, dateDefinition);
    }

    @Test
    void serializeWithContextUsesReferencedEntityFieldNamesAsAttributeNames() throws Exception {
        final Employee employee = Generator.generateEmployee();

        final Map<String, ?> jsonMap = serializeAndRead(employee);
        assertInstanceOf(Map.class, jsonMap.get(Employee.getEmployerField().getName()));
        final Map<String, Object> orgMap = (Map<String, Object>) jsonMap.get(Employee.getEmployerField().getName());
        assertEquals(employee.getEmployer().getName(),
                     orgMap.get(Organization.class.getDeclaredField("name").getName()));
        assertEquals(
                DateTimeFormatter.ISO_DATE_TIME.format(employee.getEmployer().getDateCreated().toInstant().atOffset(
                        ZoneOffset.UTC)), orgMap.get(Organization.class.getDeclaredField("dateCreated").getName()));
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
        assertEquals(
                Collections.singletonMap(TestUtil.ID_FIELD_NAME, employees.get(1).getEmployer().getUri().toString()),
                orgTwo);

        final Model result = readJson(jsonWriter.getResult());
        final Model orgModel = toRdf(employees.get(0).getEmployer());
        assertTrue(Models.isSubset(orgModel, result));
    }

    @Test
    void serializationBuildsCorrectlyContextBasedOnAnnotationPropertyAttribute() throws Exception {
        final ObjectWithMultilingualString instance = new ObjectWithMultilingualString(Generator.generateUri());
        instance.setScopeNote(MultilingualString.create("Test scope note", "en"));
        final Map<String, ?> json = serializeAndRead(instance);
        assertThat(json, hasKey(JsonLd.CONTEXT));
        assertInstanceOf(Map.class, json.get(JsonLd.CONTEXT));
        final Map<String, ?> context = (Map<String, JsonNode>) json.get(JsonLd.CONTEXT);
        assertThat(context, hasKey(ObjectWithMultilingualString.getScopeNoteField().getName()));
    }

    @Test
    void serializationUsesMappedTermForTypesWhenItIsRegisteredInReferencedObject() throws Exception {
        final Study instance = new Study();
        instance.setUri(Generator.generateUri());
        final Employee emp = Generator.generateEmployee();
        instance.setMembers(Collections.singleton(emp));

        final Map<String, ?> json = serializeAndRead(instance);
        assertThat(json, hasKey("types"));
        assertEquals(Collections.singletonList(Vocabulary.STUDY), json.get("types"));
    }

    @Test
    void serializationCreatesEmbeddedContextToOverrideIncompatibleTermMapping() throws Exception {
        final StudyWithTitle instance = new StudyWithTitle();
        instance.uri = Generator.generateUri();
        instance.name = "Test study";
        instance.organization = Generator.generateOrganization();

        final Map<String, ?> json = serializeAndRead(instance);
        verifyEmbeddedContext(json);
    }

    private void verifyEmbeddedContext(Map<String, ?> json) {
        assertThat(json, hasKey(JsonLd.CONTEXT));
        assertInstanceOf(Map.class, json.get(JsonLd.CONTEXT));
        final Map<String, ?> context = (Map<String, JsonNode>) json.get(JsonLd.CONTEXT);
        assertEquals(DC.Terms.TITLE, context.get("name"));
        assertThat(json, hasKey("organization"));
        assertInstanceOf(Map.class, json.get("organization"));
        final Map<String, ?> organization = (Map<String, ?>) json.get("organization");
        assertThat(organization, hasKey(JsonLd.CONTEXT));
        assertInstanceOf(Map.class, organization.get(JsonLd.CONTEXT));
        final Map<String, ?> embeddedCtx = (Map<String, ?>) organization.get(JsonLd.CONTEXT);
        assertEquals(RDFS.LABEL, embeddedCtx.get("name"));
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
    void serializationUsesRegisteredIdentifierTermWhenSerializingPlainIdentifierObjectPropertyValue() throws Exception {
        final Organization instance = Generator.generateOrganization();
        instance.setCountry(URI.create("http://dbpedia.org/resource/Czech_Republic"));

        final Map<String, ?> json = serializeAndRead(instance);
        assertThat(json, hasKey("country"));
        assertInstanceOf(Map.class, json.get("country"));
        final Map<String, ?> country = (Map<String, ?>) json.get("country");
        assertThat(country, hasKey("uri"));
        assertEquals(instance.getCountry().toString(), country.get("uri"));
    }

    @Test
    void serializationSerializesRootCollectionOfEnumConstantsMappedToIndividualsAsArrayOfIndividuals() throws Exception {
        final List<OwlPropertyType> value = Arrays.asList(OwlPropertyType.values());

        sut.serialize(new LinkedHashSet<>(value));
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertInstanceOf(Map.class, jsonObject);
        final Map<?, ?> map = (Map<?, ?>) jsonObject;
        assertThat(map, hasKey(JsonLd.GRAPH));
        assertInstanceOf(List.class, map.get(JsonLd.GRAPH));
        final List<?> lst = (List<?>) map.get(JsonLd.GRAPH);
        assertEquals(value.size(), lst.size());
        for (int i = 0; i < value.size(); i++) {
            assertInstanceOf(Map.class, lst.get(i));
            final Map<?, ?> element = (Map<?, ?>) lst.get(i);
            assertThat(element, hasKey(JsonLd.ID));
            assertEquals(OwlPropertyType.getMappedIndividual(value.get(i)), element.get(JsonLd.ID));
        }
    }

    /**
     * Bug #51
     */
    @Test
    void serializationCreatesEmbeddedContextOnCorrectLevel() throws Exception {
        final StudyWithTitle instance = new StudyWithTitle();
        instance.uri = Generator.generateUri();
        instance.name = "Test study";
        instance.organization = Generator.generateOrganization();
        instance.organization.addEmployee(Generator.generateEmployee());
        instance.organization.addEmployee(Generator.generateEmployee());
        instance.organization.getEmployees().forEach(e -> e.setEmployer(instance.organization));

        final Map<String, ?> json = serializeAndRead(instance);
        verifyEmbeddedContext(json);
    }

    @Test
    void serializationSerializesIndividualsAsStringWithExpandedTermDefinitionInContextWhenConfiguredTo() throws Exception {
        sut.configuration().set(ConfigParam.SERIALIZE_INDIVIDUALS_USING_EXPANDED_DEFINITION, Boolean.TRUE.toString());
        final Attribute instance = new Attribute();
        instance.setUri(Generator.generateUri());
        instance.setPropertyType(OwlPropertyType.DATATYPE_PROPERTY);
        instance.setPluralPropertyType(
                new HashSet<>(Arrays.asList(OwlPropertyType.ANNOTATION_PROPERTY, OwlPropertyType.OBJECT_PROPERTY)));

        final Map<String, ?> json = serializeAndRead(instance);
        assertThat(json, hasKey(JsonLd.CONTEXT));
        assertInstanceOf(Map.class, json.get(JsonLd.CONTEXT));
        final Map<String, ?> context = (Map<String, JsonNode>) json.get(JsonLd.CONTEXT);
        assertThat(context, hasKey("propertyType"));
        assertInstanceOf(Map.class, context.get("propertyType"));
        final Map<String, ?> termDef = (Map<String, ?>) context.get("propertyType");
        assertThat(termDef, hasKey(JsonLd.ID));
        assertEquals(termDef.get(JsonLd.ID), Vocabulary.HAS_PROPERTY_TYPE);
        assertThat(termDef, hasKey(JsonLd.TYPE));
        assertEquals(termDef.get(JsonLd.TYPE), JsonLd.ID);
        assertThat(context, hasKey("pluralPropertyType"));
        assertInstanceOf(Map.class, context.get("propertyType"));
        final Map<String, ?> pluralTermDef = (Map<String, ?>) context.get("pluralPropertyType");
        assertThat(pluralTermDef, hasKey(JsonLd.ID));
        assertEquals(pluralTermDef.get(JsonLd.ID), Vocabulary.HAS_PLURAL_PROPERTY_TYPE);
        assertThat(pluralTermDef, hasKey(JsonLd.TYPE));
        assertEquals(pluralTermDef.get(JsonLd.TYPE), JsonLd.ID);
        assertThat(json, hasKey("propertyType"));
        assertEquals(OWL.DATATYPE_PROPERTY, json.get("propertyType"));
        assertThat(json, hasKey("pluralPropertyType"));
        assertInstanceOf(List.class, json.get("pluralPropertyType"));
        assertThat((List<String>) json.get("pluralPropertyType"),
                   hasItems(OWL.ANNOTATION_PROPERTY, OWL.OBJECT_PROPERTY));
    }

    @Test
    void serializationSerializesPlainIdentifierAsStringWithExpandedTermDefinitionInContextWhenConfiguredTo() throws Exception {
        sut.configuration().set(ConfigParam.SERIALIZE_INDIVIDUALS_USING_EXPANDED_DEFINITION, Boolean.TRUE.toString());
        final Organization instance = Generator.generateOrganization();
        instance.setCountry(URI.create("http://dbpedia.org/resource/Czech_Republic"));

        final Map<String, ?> json = serializeAndRead(instance);
        assertThat(json, hasKey(JsonLd.CONTEXT));
        assertInstanceOf(Map.class, json.get(JsonLd.CONTEXT));
        final Map<String, ?> context = (Map<String, JsonNode>) json.get(JsonLd.CONTEXT);
        assertThat(context, hasKey("country"));
        assertInstanceOf(Map.class, context.get("country"));
        final Map<String, ?> termDef = (Map<String, ?>) context.get("country");
        assertEquals(Vocabulary.ORIGIN, termDef.get(JsonLd.ID));
        assertEquals(JsonLd.ID, termDef.get(JsonLd.TYPE));
        assertEquals(instance.getCountry().toString(), json.get("country"));
    }
}