package cz.cvut.kbss.jsonld.serialization;

import com.github.jsonldjava.utils.JsonUtils;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.environment.model.User;
import cz.cvut.kbss.jsonld.serialization.util.BufferedJsonGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ContextBuildingJsonLdSerializerTest {

    private BufferedJsonGenerator jsonWriter;

    private JsonLdSerializer sut;

    @BeforeEach
    void setUp() {
        this.jsonWriter = new BufferedJsonGenerator();
        this.sut = new ContextBuildingJsonLdSerializer(jsonWriter);
    }

    @Test
    void jsonLdContextContainsCorrectAttributeToPropertyMapping() throws Exception {
        final User user = Generator.generateUser();
        sut.serialize(user);
        final Object result = JsonUtils.fromString(jsonWriter.getResult());
        assertInstanceOf(Map.class, result);
        final Map<String, Object> jsonMap = (Map<String, Object>) result;
        assertThat(jsonMap, hasKey(JsonLd.CONTEXT));
        assertInstanceOf(Map.class, jsonMap.get(JsonLd.CONTEXT));
        final Map<String, String> context = (Map<String, String>) jsonMap.get(JsonLd.CONTEXT);
        assertEquals(Vocabulary.FIRST_NAME, context.get(User.getFirstNameField().getName()));
        assertEquals(user.getFirstName(), jsonMap.get(User.getFirstNameField().getName()));
        assertEquals(Vocabulary.LAST_NAME, context.get(User.getLastNameField().getName()));
        assertEquals(user.getLastName(), jsonMap.get(User.getLastNameField().getName()));
        assertEquals(Vocabulary.USERNAME, context.get(User.getUsernameField().getName()));
        assertEquals(user.getUsername(), jsonMap.get(User.getUsernameField().getName()));
        assertEquals(Vocabulary.IS_ADMIN, context.get(User.class.getDeclaredField("admin").getName()));
        assertEquals(user.getAdmin(), jsonMap.get(User.class.getDeclaredField("admin").getName()));
    }

    @Test
    void serializeWithContextUsesFieldNamesAsJsonLdAttributeNames() throws Exception {
        final User user = Generator.generateUser();
        sut.serialize(user);
        final Object result = JsonUtils.fromString(jsonWriter.getResult());
        assertInstanceOf(Map.class, result);
        final Map<String, Object> jsonMap = (Map<String, Object>) result;
        assertEquals(user.getFirstName(), jsonMap.get(User.getFirstNameField().getName()));
        assertEquals(user.getLastName(), jsonMap.get(User.getLastNameField().getName()));
        assertEquals(user.getUsername(), jsonMap.get(User.getUsernameField().getName()));
        assertEquals(user.getAdmin(), jsonMap.get(User.class.getDeclaredField("admin").getName()));
    }

    @Test
    void serializeWithContextSupportsTypesAndIdMapping() throws Exception {
        final User user = Generator.generateUser();
        user.setTypes(Collections.singleton(Generator.generateUri().toString()));
        sut.serialize(user);
        final Object result = JsonUtils.fromString(jsonWriter.getResult());
        assertInstanceOf(Map.class, result);
        final Map<String, Object> jsonMap = (Map<String, Object>) result;
        assertThat(jsonMap, hasKey(JsonLd.CONTEXT));
        assertInstanceOf(Map.class, jsonMap.get(JsonLd.CONTEXT));
        final Map<String, String> context = (Map<String, String>) jsonMap.get(JsonLd.CONTEXT);
        assertEquals(JsonLd.ID, context.get(Person.class.getDeclaredField("uri").getName()));
        assertEquals(user.getUri().toString(), jsonMap.get(Person.class.getDeclaredField("uri").getName()));
        assertEquals(JsonLd.TYPE, context.get(User.class.getDeclaredField("types").getName()));
        assertThat((List<String>) jsonMap.get(User.class.getDeclaredField("types").getName()),
                   hasItems(user.getTypes().toArray(new String[]{})));
        assertThat((List<String>) jsonMap.get(User.class.getDeclaredField("types").getName()),
                   hasItems(Vocabulary.PERSON, Vocabulary.USER));
    }

    @Test
    void serializeWithContextBuildsContextForObjectProperty() throws Exception {
        final Employee employee = Generator.generateEmployee();
        sut.serialize(employee);
        final Object result = JsonUtils.fromString(jsonWriter.getResult());
        assertInstanceOf(Map.class, result);
        final Map<String, Object> jsonMap = (Map<String, Object>) result;
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
        sut.serialize(employee);
        final Object result = JsonUtils.fromString(jsonWriter.getResult());
        assertInstanceOf(Map.class, result);
        final Map<String, Object> jsonMap = (Map<String, Object>) result;
        assertInstanceOf(Map.class, jsonMap.get(Employee.getEmployerField().getName()));
        final Map<String, Object> orgMap = (Map<String, Object>) jsonMap.get(Employee.getEmployerField().getName());
        assertEquals(employee.getEmployer().getName(),
                     orgMap.get(Organization.class.getDeclaredField("name").getName()));
        assertEquals(employee.getEmployer().getDateCreated().toInstant().toString(),
                     orgMap.get(Organization.class.getDeclaredField("dateCreated").getName()));
        assertInstanceOf(Collection.class, orgMap.get(Organization.class.getDeclaredField("brands").getName()));
        final Collection<String> jsonBrands =
                (Collection<String>) orgMap.get(Organization.class.getDeclaredField("brands").getName());
        assertEquals(employee.getEmployer().getBrands().size(), jsonBrands.size());
        assertThat(jsonBrands, hasItems(employee.getEmployer().getBrands().toArray(new String[]{})));
    }
}