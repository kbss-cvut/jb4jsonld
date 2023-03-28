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
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.IdentifierUtil;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.TestUtil;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class CompactedJsonLdSerializerTest extends JsonLdSerializerTestBase {

    @BeforeEach
    void setUp() {
        this.sut = new CompactedJsonLdSerializer(jsonWriter);
    }

    @Test
    void testSerializeCollectionOfObjects() throws Exception {
        final Set<User> users = Generator.generateUsers();
        sut.serialize(users);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertNotNull(jsonObject);
        assertInstanceOf(List.class, jsonObject);
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
    void serializationSkipsNullDataPropertyValues() throws Exception {
        final User user = Generator.generateUser();
        user.setAdmin(null);
        final Map<String, ?> json = serializeAndRead(user);
        assertThat(json, not(hasKey(Vocabulary.IS_ADMIN)));
    }

    @Test
    void serializationSkipsNullObjectPropertyValues() throws Exception {
        final Employee employee = Generator.generateEmployee();
        employee.setEmployer(null);
        final Map<String, ?> json = serializeAndRead(employee);
        assertThat(json, not(hasKey(Vocabulary.IS_MEMBER_OF)));
    }

    @Test
    void serializationGeneratesBlankNodeIfInstancesDoesNotHaveIdentifierValue() throws Exception {
        final Organization company = Generator.generateOrganization();
        company.setUri(null);
        final Map<String, ?> json = serializeAndRead(company);
        assertThat(json, hasKey(JsonLd.ID));
        assertThat(json.get(JsonLd.ID).toString(), startsWith(IdentifierUtil.B_NODE_PREFIX));
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
        final StudyWithNamespaces study = new StudyWithNamespaces();
        study.setUri(Generator.generateUri());
        study.setName("Test study");
        study.setParticipants(Collections.singleton(Generator.generateEmployee()));
        study.setMembers(Collections.singleton(Generator.generateEmployee()));

        final Map<String, ?> json = serializeAndRead(study);
        assertThat(json, hasKey(RDFS.LABEL));
        assertThat(json, hasKey(Vocabulary.HAS_PARTICIPANT));
        assertThat(json, hasKey(Vocabulary.HAS_MEMBER));
    }

    /**
     * Bug #36
     */
    @SuppressWarnings("unchecked")
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

    @Test
    void serializationSerializesRootCollectionOfEnumConstantsMappedToIndividualsAsArrayOfIndividuals() throws Exception {
        final List<OwlPropertyType> value = Arrays.asList(OwlPropertyType.values());

        sut.serialize(new LinkedHashSet<>(value));
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertInstanceOf(List.class, jsonObject);
        final List<?> lst = (List<?>) jsonObject;
        assertEquals(value.size(), lst.size());
        for (int i = 0; i < value.size(); i++) {
            assertInstanceOf(Map.class, lst.get(i));
            final Map<?, ?> element = (Map<?, ?>) lst.get(i);
            assertThat(element, hasKey(JsonLd.ID));
            assertEquals(OwlPropertyType.getMappedIndividual(value.get(i)), element.get(JsonLd.ID));
        }
    }
}
