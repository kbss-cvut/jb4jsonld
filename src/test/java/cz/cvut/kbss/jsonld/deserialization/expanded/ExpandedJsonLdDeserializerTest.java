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
package cz.cvut.kbss.jsonld.deserialization.expanded;

import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import cz.cvut.kbss.jopa.model.annotations.Properties;
import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.deserialization.DeserializationContext;
import cz.cvut.kbss.jsonld.deserialization.JsonLdDeserializer;
import cz.cvut.kbss.jsonld.deserialization.ValueDeserializer;
import cz.cvut.kbss.jsonld.environment.TestUtil;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.*;
import cz.cvut.kbss.jsonld.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.*;

import static cz.cvut.kbss.jsonld.environment.TestUtil.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@SuppressWarnings("unused")
class ExpandedJsonLdDeserializerTest {

    private static final Map<URI, User> USERS = initUsers();

    private static final String ORG_NAME = "UNSC";
    private static final String[] ORG_BRANDS = {"Spartan-II", "Mjolnir IV"};

    private JsonLdDeserializer sut;

    private static Map<URI, User> initUsers() {
        final Map<URI, User> map = new HashMap<>();
        map.put(HALSEY_URI, new User(HALSEY_URI, "Catherine", "Halsey", "halsey@unsc.org", true));
        map.put(LASKY_URI, new User(LASKY_URI, "Thomas", "Lasky", "lasky@unsc.org", false));
        map.put(PALMER_URI, new User(PALMER_URI, "Sarah", "Palmer", "palmer@unsc.org", false));
        return map;
    }

    @BeforeEach
    void setUp() {
        final Configuration config = new Configuration();
        config.set(ConfigParam.SCAN_PACKAGE, "cz.cvut.kbss.jsonld");
        this.sut = JsonLdDeserializer.createExpandedDeserializer(config);
    }

    @Test
    void testDeserializeInstanceWithDataProperties() throws Exception {
        final Object input = readAndExpand("objectWithDataProperties.json");
        final User result = sut.deserialize(input, User.class);
        verifyUserAttributes(USERS.get(HALSEY_URI), result);
    }

    private void verifyUserAttributes(User expected, User actual) {
        assertEquals(expected.getUri(), actual.getUri());
        assertEquals(expected.getAdmin(), actual.getAdmin());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getUsername(), actual.getUsername());
    }

    @Test
    void testDeserializeInstanceWithSingularObjectProperty() throws Exception {
        final Object input = readAndExpand("objectWithSingularReference.json");
        final Employee result = sut.deserialize(input, Employee.class);
        verifyUserAttributes(USERS.get(HALSEY_URI), result);
        assertNotNull(result.getEmployer());
        verifyOrganizationAttributes(result.getEmployer());
    }

    private void verifyOrganizationAttributes(Organization result) {
        assertEquals(TestUtil.UNSC_URI, result.getUri());
        assertEquals(ORG_NAME, result.getName());
        assertNotNull(result.getDateCreated());
        for (String brand : ORG_BRANDS) {
            assertTrue(result.getBrands().contains(brand));
        }
    }

    @Test
    void testDeserializeInstanceWithPluralObjectProperty() throws Exception {
        final Object input = readAndExpand("objectWithPluralReference.json");
        final Organization result = sut.deserialize(input, Organization.class);
        verifyOrganizationAttributes(result);
        assertEquals(3, result.getEmployees().size());
        for (Employee e : result.getEmployees()) {
            assertTrue(USERS.containsKey(e.getUri()));
            verifyUserAttributes(USERS.get(e.getUri()), e);
        }
    }

    @Test
    void testDeserializeInstanceWithPluralObjectPropertyWithBackwardReferencesToOriginalInstance()
            throws Exception {
        final Object input = readAndExpand("objectWithPluralObjectPropertyWithBackwardReferences.json");
        final Organization result = sut.deserialize(input, Organization.class);
        verifyOrganizationAttributes(result);
        assertEquals(3, result.getEmployees().size());
        for (Employee e : result.getEmployees()) {
            assertTrue(USERS.containsKey(e.getUri()));
            verifyUserAttributes(USERS.get(e.getUri()), e);
            assertNotNull(e.getEmployer());
            assertSame(result, e.getEmployer());
        }
    }

    @Test
    void testDeserializeInstanceWithSingularObjectPropertyWithBackwardReference() throws Exception {
        final Object input = readAndExpand("objectWithSingularObjectPropertyWithBackwardReference.json");
        final Employee result = sut.deserialize(input, Employee.class);
        verifyUserAttributes(USERS.get(HALSEY_URI), result);
        final Organization org = result.getEmployer();
        assertNotNull(org);
        verifyOrganizationAttributes(org);
        assertEquals(1, org.getEmployees().size());
        assertSame(result, org.getEmployees().iterator().next());
    }

    @Test
    void deserializationOfArrayWithOneInstanceReturnsProperCollection() throws Exception {
        final Object input = readAndExpand("objectWithPluralReferenceContainingOneValue.json");
        final Organization result = sut.deserialize(input, Organization.class);
        verifyOrganizationAttributes(result);
        assertEquals(1, result.getEmployees().size());
        final Employee e = result.getEmployees().iterator().next();
        verifyUserAttributes(USERS.get(e.getUri()), e);
    }

    @Test
    void throwsUnknownPropertyExceptionWhenIgnoreIsNotConfiguredAndUnmappedPropertyIsEncountered()
            throws Exception {
        final Object input = readAndExpand("objectWithUnknownProperty.json");
        final String property = "http://purl.org/dc/terms/description";

        final UnknownPropertyException result = assertThrows(UnknownPropertyException.class,
                                                             () -> sut.deserialize(input, Organization.class));
        assertEquals("No field matching property " + property + " was found in " + Organization.class +
                             " or its ancestors.", result.getMessage());
    }

    @Test
    void skipsUnknownPropertyWhenIgnoreIsConfiguredAndUnmappedPropertyIsEncountered() throws Exception {
        final Object input = readAndExpand("objectWithUnknownProperty.json");
        sut.configuration().set(ConfigParam.IGNORE_UNKNOWN_PROPERTIES, Boolean.TRUE.toString());
        final Organization result = sut.deserialize(input, Organization.class);
        verifyOrganizationAttributes(result);
    }

    @Test
    void deserializationResolvesReferenceInPluralPropertyWrappedInAnotherObject() throws Exception {
        final Object input = readAndExpand("objectWithPluralReferenceSharingObject.json");
        final Study result = sut.deserialize(input, Study.class);

        assertNotNull(result.getName());
        Organization org = null;
        assertFalse(result.getParticipants().isEmpty());
        for (Employee e : result.getParticipants()) {
            if (org == null) {
                org = e.getEmployer();
            } else {
                assertSame(org, e.getEmployer());
            }
        }
        assertFalse(result.getMembers().isEmpty());
        for (Employee e : result.getMembers()) {
            assertSame(org, e.getEmployer());
        }
    }

    @Test
    void deserializationSetsValueOfTypesSpecification() throws Exception {
        final Object input = readAndExpand("objectWithDataProperties.json");
        final User result = sut.deserialize(input, User.class);
        assertTrue(result.getTypes().contains(Vocabulary.AGENT));
        assertFalse(result.getTypes().contains(Vocabulary.USER));   // Type of the class should not be in @Types
    }

    @Test
    void deserializationThrowsExceptionWhenTypesAttributeDoesNotContainTargetClassType() throws Exception {
        final Object input = readAndExpand("objectWithDataProperties.json");
        final TargetTypeException result = assertThrows(TargetTypeException.class,
                                                        () -> sut.deserialize(input, Employee.class));
        assertThat(result.getMessage(), containsString(
                "Neither " + Employee.class + " nor any of its subclasses matches the types "));
    }

    @Test
    void deserializationPopulatesPropertiesFieldWithUnmappedPropertiesFoundInInput() throws Exception {
        final Object input = readAndExpand("objectWithUnmappedProperties.json");
        final Person result = sut.deserialize(input, Person.class);
        final User model = USERS.get(HALSEY_URI);
        assertEquals(model.getUri(), result.getUri());
        assertEquals(model.getFirstName(), result.getFirstName());
        assertEquals(model.getLastName(), result.getLastName());
        assertFalse(result.getProperties().isEmpty());
        assertTrue(result.getProperties().containsKey(Vocabulary.USERNAME));
        assertEquals(1, result.getProperties().get(Vocabulary.USERNAME).size());
        assertEquals(model.getUsername(), result.getProperties().get(Vocabulary.USERNAME).iterator().next());
        assertTrue(result.getProperties().containsKey(Vocabulary.IS_ADMIN));
        assertEquals(1, result.getProperties().get(Vocabulary.IS_ADMIN).size());
        assertEquals(Boolean.TRUE.toString(), result.getProperties().get(Vocabulary.IS_ADMIN).iterator().next());
    }

    @Test
    void deserializationPopulatesTypedProperties() throws Exception {
        final Object input = readAndExpand("objectWithUnmappedProperties.json");
        final ClassWithProperties result = sut.deserialize(input, ClassWithProperties.class);
        assertNotNull(result);
        final User model = USERS.get(HALSEY_URI);
        assertEquals(model.getUri(), result.uri);
        assertTrue(result.properties.containsKey(URI.create(Vocabulary.FIRST_NAME)));
        assertEquals(model.getFirstName(), result.properties.get(URI.create(Vocabulary.FIRST_NAME)).iterator().next());
        assertTrue((Boolean) result.properties.get(URI.create(Vocabulary.IS_ADMIN)).iterator().next());
    }

    @OWLClass(iri = Vocabulary.PERSON)
    public static class ClassWithProperties {
        @Id
        private URI uri;

        @Properties
        private Map<URI, Set<?>> properties;

        public ClassWithProperties() {
        }
    }

    @Test
    void deserializationThrowsExceptionWhenMultipleValuesForSingularFieldAreEncountered() throws Exception {
        final Object input = readAndExpand("objectWithAttributeCardinalityViolation.json");
        final JsonLdDeserializationException result = assertThrows(JsonLdDeserializationException.class,
                                                                   () -> sut.deserialize(input, Person.class));
        assertThat(result.getMessage(),
                   containsString("Encountered multiple values of property " + Vocabulary.FIRST_NAME));
    }

    @Test
    void deserializationThrowsExceptionWhenMultipleValuesOfUnmappedPropertyForPropertiesWithSingularValuesAreEncountered()
            throws Exception {
        final Object input = readAndExpand("objectWithAttributeCardinalityViolation.json");
        final JsonLdDeserializationException result = assertThrows(JsonLdDeserializationException.class,
                                                                   () -> sut.deserialize(input,
                                                                                         ClassWithSingularProperties.class));
        assertThat(result.getMessage(),
                   containsString("Encountered multiple values of property " + Vocabulary.FIRST_NAME));
    }

    @OWLClass(iri = Vocabulary.PERSON)
    public static class ClassWithSingularProperties {
        @Id
        private URI uri;

        @Properties
        private Map<String, String> properties;

        public ClassWithSingularProperties() {
        }
    }

    @Test
    void deserializationSupportsPlainIdentifierObjectPropertyValues() throws Exception {
        final Object input = readAndExpand("objectWithPlainIdentifierObjectPropertyValue.json");
        final Organization result = sut.deserialize(input, Organization.class);
        assertNotNull(result);
        assertEquals(URI.create("http://dbpedia.org/resource/Czech_Republic"), result.getCountry());
    }

    @Test
    void deserializationSupportsObjectsWithBlankNodeIds() throws Exception {
        final Object input = readAndExpand("objectWithBlankNodeIdentifier.json");
        final User result = sut.deserialize(input, User.class);
        assertNotNull(result);
        assertNull(result.getUri());
    }

    @Test
    void deserializationReturnsSubclassInstanceWhenTypesMatch() throws Exception {
        final Configuration config = new Configuration();
        config.set(ConfigParam.SCAN_PACKAGE, "cz.cvut.kbss.jsonld.environment.model");
        this.sut = JsonLdDeserializer.createExpandedDeserializer(config);
        final Object input = readAndExpand("objectWithDataProperties.json");
        final Person result = sut.deserialize(input, Person.class);
        assertTrue(result instanceof User);
    }

    @Test
    void deserializationSupportsPolymorphismForCollections() throws Exception {
        final Configuration config = new Configuration();
        config.set(ConfigParam.SCAN_PACKAGE, "cz.cvut.kbss.jsonld.environment.model");
        config.set(ConfigParam.IGNORE_UNKNOWN_PROPERTIES, Boolean.toString(true));
        this.sut = JsonLdDeserializer.createExpandedDeserializer(config);
        final Object input = readAndExpand("objectWithPluralReference.json");
        final PolymorphicOrganization result = sut.deserialize(input, PolymorphicOrganization.class);
        assertNotNull(result.employees);
        assertEquals(3, result.employees.size());
        result.employees.forEach(e -> assertTrue(e instanceof Employee));
    }

    @OWLClass(iri = Vocabulary.ORGANIZATION)
    public static class PolymorphicOrganization {

        @Id
        private URI id;

        @OWLObjectProperty(iri = Vocabulary.HAS_MEMBER)
        private Set<Person> employees;

        public PolymorphicOrganization() {
        }
    }

    @Test
    void deserializationSupportsPolymorphismForAttributes() throws Exception {
        final Configuration config = new Configuration();
        config.set(ConfigParam.SCAN_PACKAGE, "cz.cvut.kbss.jsonld.environment.model");
        config.set(ConfigParam.IGNORE_UNKNOWN_PROPERTIES, Boolean.toString(true));
        this.sut = JsonLdDeserializer.createExpandedDeserializer(config);
        final Object input = readAndExpand("objectWithSingularPolymorphicReference.json");
        final PolymorphicPerson result = sut.deserialize(input, PolymorphicPerson.class);
        assertTrue(result.friend instanceof Employee);
    }

    @OWLClass(iri = Vocabulary.PERSON)
    public static class PolymorphicPerson {
        @Id
        private URI id;

        @OWLObjectProperty(iri = Vocabulary.KNOWS)
        private Person friend;

        public PolymorphicPerson() {
        }
    }

    @Test
    void deserializationSupportsDeserializingObjectAsPlainIdentifier() throws Exception {
        final Configuration config = new Configuration();
        config.set(ConfigParam.IGNORE_UNKNOWN_PROPERTIES, Boolean.toString(true));
        config.set(ConfigParam.SCAN_PACKAGE, "cz.cvut.kbss.jsonld");
        this.sut = JsonLdDeserializer.createExpandedDeserializer(config);
        final Object input = readAndExpand("objectWithSingularPolymorphicReference.json");
        final PersonWithPlainIdentifierAttribute result =
                sut.deserialize(input, PersonWithPlainIdentifierAttribute.class);
        assertEquals(URI.create("http://krizik.felk.cvut.cz/ontologies/jb4jsonld#Sarah+Palmer"), result.friend);
    }

    @OWLClass(iri = Vocabulary.PERSON)
    public static class PersonWithPlainIdentifierAttribute {
        @Id
        private URI id;

        @OWLObjectProperty(iri = Vocabulary.KNOWS)
        private URI friend;

        public PersonWithPlainIdentifierAttribute() {
        }
    }

    @Test
    void deserializationSupportsDeserializingObjectsAsPluralPlainIdentifiers() throws Exception {
        final Configuration config = new Configuration();
        config.set(ConfigParam.IGNORE_UNKNOWN_PROPERTIES, Boolean.toString(true));
        config.set(ConfigParam.SCAN_PACKAGE, "cz.cvut.kbss.jsonld");
        this.sut = JsonLdDeserializer.createExpandedDeserializer(config);
        final Object input = readAndExpand("objectWithPluralReference.json");
        final OrganizationWithPlainIdentifiers result =
                sut.deserialize(input, OrganizationWithPlainIdentifiers.class);
        assertNotNull(result.members);
        assertTrue(result.members
                           .contains(URI.create("http://krizik.felk.cvut.cz/ontologies/jb4jsonld#Catherine+Halsey")));
        assertTrue(result.members.contains(URI.create("http://krizik.felk.cvut.cz/ontologies/jb4jsonld#Thomas+Lasky")));
        assertTrue(result.members.contains(URI.create("http://krizik.felk.cvut.cz/ontologies/jb4jsonld#Sarah+Palmer")));
    }

    @OWLClass(iri = Vocabulary.ORGANIZATION)
    public static class OrganizationWithPlainIdentifiers {
        @Id
        private URI id;

        @OWLObjectProperty(iri = Vocabulary.HAS_MEMBER)
        private Set<URI> members;

        public OrganizationWithPlainIdentifiers() {
        }
    }

    @Test
    void deserializationHandlesJsonLdListOfValues() throws Exception {
        final Configuration config = new Configuration();
        config.set(ConfigParam.IGNORE_UNKNOWN_PROPERTIES, Boolean.toString(true));
        config.set(ConfigParam.SCAN_PACKAGE, "cz.cvut.kbss.jsonld");
        this.sut = JsonLdDeserializer.createExpandedDeserializer(config);
        final Object input = readAndExpand("objectWithList.json");
        final OrganizationWithListOfMembers result =
                sut.deserialize(input, OrganizationWithListOfMembers.class);
        assertEquals(3, result.members.size());
        assertEquals(HALSEY_URI, result.members.get(0).getUri());
        assertEquals(LASKY_URI, result.members.get(1).getUri());
        assertEquals(PALMER_URI, result.members.get(2).getUri());
    }

    @OWLClass(iri = Vocabulary.ORGANIZATION)
    public static class OrganizationWithListOfMembers {
        @Id
        private URI id;

        @OWLObjectProperty(iri = Vocabulary.HAS_MEMBER)
        private List<Employee> members;

        public OrganizationWithListOfMembers() {
        }
    }

    @Test
    void deserializationReconstructsObjectFromMultiplePlaces() throws Exception {
        final Object input = readAndExpand("objectWithDefinitionSpreadOverMultipleReferences.json");
        final Employee result = sut.deserialize(input, Employee.class);
        verifyUserAttributes(USERS.get(HALSEY_URI), result);
    }

    @Test
    void deserializationPutsUnmappedObjectReferencesIntoProperties() throws Exception {
        final Object input = readAndExpand("objectWithSingularReference.json");
        final PersonWithoutSubclass result = sut.deserialize(input, PersonWithoutSubclass.class);
        assertTrue(result.properties.containsKey(Vocabulary.IS_MEMBER_OF));
        assertTrue(result.properties.get(Vocabulary.IS_MEMBER_OF).contains(TestUtil.UNSC_URI.toString()));
        assertTrue(result.properties.containsKey(Vocabulary.USERNAME));
        assertTrue(result.properties.get(Vocabulary.USERNAME).contains("halsey@unsc.org"));
    }

    @OWLClass(iri = Vocabulary.PERSON)
    public static class PersonWithoutSubclass {

        @Id
        public URI uri;

        @OWLDataProperty(iri = Vocabulary.FIRST_NAME)
        private String firstName;

        @OWLDataProperty(iri = Vocabulary.LAST_NAME)
        private String lastName;

        @Properties
        private Map<String, Set<String>> properties;
    }

    @Test
    void deserializationPutsUnmappedObjectReferencesIntoTypedProperties() throws Exception {
        final Object input = readAndExpand("objectWithSingularReference.json");
        final ClassWithProperties result = sut.deserialize(input, ClassWithProperties.class);
        assertTrue(result.properties.containsKey(URI.create(Vocabulary.IS_MEMBER_OF)));
        assertTrue(result.properties.get(URI.create(Vocabulary.IS_MEMBER_OF)).contains(TestUtil.UNSC_URI));
    }

    @Test
    void deserializationParsesNumericTimestampForDateField() throws Exception {
        final Object input = readAndExpand("objectWithPluralReference.json");
        final Organization result = sut.deserialize(input, Organization.class);
        assertNotNull(result.getDateCreated());
        assertTrue(result.getDateCreated().before(new Date()));
    }

    @Test
    void deserializationIgnoresPropertyWithReadOnlyAccess() throws Exception {
        final Object input = readAndExpand("objectWithReadOnlyPropertyValue.json");
        final Study result = sut.deserialize(input, Study.class);
        assertNull(result.getNoOfPeopleInvolved());
    }

    @Test
    void deserializationHandlesAnnotationPropertyValuesWhichMixLiteralAndReferenceValues() throws Exception {
        final Object input = readAndExpand("objectWithAnnotationPropertyReferenceValues.json");
        final ObjectWithAnnotationProperties result = sut.deserialize(input, ObjectWithAnnotationProperties.class);
        assertNotNull(result);
        assertThat(result.getOrigins(), hasItems(URI.create("http://dbpedia.org/resource/Czech_Republic"), "TermIt"));
    }

    @Test
    void deserializationHandlesSingularAnnotationPropertyWithReferenceValue() throws Exception {
        final Object jsonLd = JsonUtils.fromString("{" +
                                                           "  \"@id\": \"http://krizik.felk.cvut.cz/ontologies/jb4jsonld#ChangeRecord01\"," +
                                                           "  \"@type\": \"http://krizik.felk.cvut.cz/ontologies/jb4jsonld/ObjectWithAnnotations\"," +
                                                           "  \"http://krizik.felk.cvut.cz/ontologies/jb4jsonld/origin\": {" +
                                                           "      \"@id\": \"http://dbpedia.org/resource/Czech_Republic\"" +
                                                           "    }}");
        final Object expanded = JsonLdProcessor.expand(jsonLd);

        final ObjectWithAnnotationProperty result = sut.deserialize(expanded, ObjectWithAnnotationProperty.class);
        assertNotNull(result);
        assertEquals(URI.create("http://dbpedia.org/resource/Czech_Republic"), result.origin);
    }

    @OWLClass(iri = Vocabulary.OBJECT_WITH_ANNOTATIONS)
    public static class ObjectWithAnnotationProperty {
        @Id
        private URI uri;

        @OWLAnnotationProperty(iri = Vocabulary.ORIGIN)
        private Object origin;
    }

    @Test
    void deserializationUnmarshallsDataPropertyValueIntoEnumConstant() throws Exception {
        final Object input = readAndExpand("objectWithEnumDataPropertyValue.json");
        final User result = sut.deserialize(input, User.class);
        assertNotNull(result);
        assertEquals(Role.USER, result.getRole());
    }

    @Test
    void deserializationThrowsJsonLdDeserializationExceptionWhenInputIsNotExpandedJsonLd() {
        final Object input = Collections.emptyList();
        assertThrows(JsonLdDeserializationException.class, () -> sut.deserialize(input, User.class));
    }

    @Test
    void deserializationHandlesTypedDataPropertyValues() throws Exception {
        final Object input = readAndExpand("objectWithTypedDataProperties.json");
        final User result = sut.deserialize(input, User.class);
        assertTrue(result.getAdmin());
    }

    @Test
    void deserializationHandlesObjectPropertyFieldOfTypeObject() throws Exception {
        final Object input = readAndExpand("objectWithSingularReference.json");
        final Configuration config = new Configuration();
        config.set(ConfigParam.SCAN_PACKAGE, "cz.cvut.kbss.jsonld");
        config.set(ConfigParam.IGNORE_UNKNOWN_PROPERTIES, Boolean.TRUE.toString());
        // This will prevent problems with multiple classes matching the same type (Organization)
        config.set(ConfigParam.SCAN_PACKAGE, "cz.cvut.kbss.jsonld.environment.model");
        this.sut = ExpandedJsonLdDeserializer.createExpandedDeserializer(config);
        final GenericMember result = sut.deserialize(input, GenericMember.class);
        assertNotNull(result.getMemberOf());
        assertThat(result.getMemberOf(), instanceOf(Organization.class));
        final Organization org = (Organization) result.getMemberOf();
        assertNotNull(org.getUri());
        assertNotNull(org.getBrands());
        assertNotNull(org.getDateCreated());
        assertEquals("UNSC", org.getName());
    }

    @Test
    void deserializationUsesDeferredReferenceResolutionToHandleReferencePrecedingObjectDeclaration() throws Exception {
        final Object input = readAndExpand("objectWithReferencePrecedingFullObject.json");
        final Study result = sut.deserialize(input, Study.class);
        assertEquals(1, result.getMembers().size());
        final Organization memberOrg = result.getMembers().iterator().next().getEmployer();
        assertNotNull(memberOrg);
        assertEquals(1, result.getParticipants().size());
        final Organization participantOrg = result.getParticipants().iterator().next().getEmployer();
        assertNotNull(participantOrg);
        assertEquals(memberOrg.getUri(), participantOrg.getUri());
        assertEquals(memberOrg.getName(), participantOrg.getName());
    }

    @Test
    void deserializationUsesDeferredReferenceResolutionToHandleReferencePrecedingObjectDeclarationInCollection()
            throws Exception {
        final Object input = readAndExpand("objectWithReferencePrecedingFullObjectInCollection.json");
        final Study result = sut.deserialize(input, Study.class);
        assertEquals(1, result.getMembers().size());
        assertEquals(1, result.getParticipants().size());
        final Employee member = result.getMembers().iterator().next();
        final Employee participant = result.getParticipants().iterator().next();
        assertEquals(member.getUri(), participant.getUri());
        assertSame(member, participant);
    }

    @Test
    void deserializationThrowsUnresolvedReferenceExceptionWhenUnresolvedReferenceIsFound() throws Exception {
        final Object input = readAndExpand("objectWithUnresolvedReference.json");
        assertThrows(UnresolvedReferenceException.class, () -> sut.deserialize(input, Study.class));
    }

    @Test
    void deserializationSupportsPlainObjectReturnType() throws Exception {
        final Configuration config = new Configuration();
        config.set(ConfigParam.IGNORE_UNKNOWN_PROPERTIES, Boolean.TRUE.toString());
        // This will prevent problems with multiple classes matching the same type (Organization)
        config.set(ConfigParam.SCAN_PACKAGE, "cz.cvut.kbss.jsonld.environment.model");
        this.sut = ExpandedJsonLdDeserializer.createExpandedDeserializer(config);
        final Object input = readAndExpand("objectWithDataProperties.json");
        final Object result = sut.deserialize(input, Object.class);
        assertThat(result, instanceOf(User.class));
        verifyUserAttributes(USERS.get(HALSEY_URI), (User) result);
    }

    @Test
    void deserializationUsesProvidedTargetTypeWhenNoTypeIsSpecifiedAndTypeAssumingIsEnabled() throws Exception {
        final Configuration config = new Configuration();
        config.set(ConfigParam.ASSUME_TARGET_TYPE, Boolean.TRUE.toString());
        config.set(ConfigParam.SCAN_PACKAGE, "cz.cvut.kbss.jsonld");
        this.sut = ExpandedJsonLdDeserializer.createExpandedDeserializer(config);
        final Object input = readAndExpand("objectWithoutTypes.json");
        final User result = sut.deserialize(input, User.class);
        assertNotNull(result);
        verifyUserAttributes(USERS.get(HALSEY_URI), result);
    }

    @Test
    void deserializationSupportsMultilingualStringAttributes() throws Exception {
        final Object input = readAndExpand("objectWithMultilingualString.json");
        final ObjectWithMultilingualString result = sut.deserialize(input, ObjectWithMultilingualString.class);
        assertNotNull(result);
        assertNotNull(result.getLabel());
        assertEquals("Leveraging Semantic Web Technologies in Domain-specific Information Systems",
                     result.getLabel().get("en"));
        assertEquals("Využití technologií sémantického webu v doménových informačních systémech",
                     result.getLabel().get("cs"));
    }

    @Test
    void deserializationSupportsDeserializingSingleValueIntoMultilingualStringAttribute() throws Exception {
        final Object input = readAndExpand("objectWithSingleLangStringValue.json");
        final ObjectWithMultilingualString result = sut.deserialize(input, ObjectWithMultilingualString.class);
        assertNotNull(result);
        assertNotNull(result.getLabel());
        assertEquals("Leveraging Semantic Web Technologies in Domain-specific Information Systems",
                     result.getLabel().get("en"));
    }

    @Test
    void deserializationSupportsDeserializingSingleLanguageTaggedValueIntoPlainStringAttribute() throws Exception {
        final Object input = readAndExpand("objectWithSingleLangStringValue.json");
        final Study result = sut.deserialize(input, Study.class);
        assertNotNull(result);
        assertNotNull(result.getName());
        assertEquals("Leveraging Semantic Web Technologies in Domain-specific Information Systems", result.getName());
    }

    @Test
    void deserializationSupportsDeserializingPlainStringToMultilingualStringAttribute() throws Exception {
        sut.configuration().set(ConfigParam.IGNORE_UNKNOWN_PROPERTIES, Boolean.TRUE.toString());
        final Object input = readAndExpand("objectWithPluralReferenceSharingObject.json");
        final ObjectWithMultilingualString result = sut.deserialize(input, ObjectWithMultilingualString.class);
        assertNotNull(result);
        assertNotNull(result.getLabel());
        assertTrue(result.getLabel().containsSimple());
        assertEquals("LupusStudy", result.getLabel().get());
    }

    @Test
    void deserializationSupportsPluralMultilingualAttributes() throws Exception {
        final Object input = readAndExpand("objectWithPluralMultilingualString.json");
        final ObjectWithPluralMultilingualString result = sut
                .deserialize(input, ObjectWithPluralMultilingualString.class);
        assertNotNull(result);
        assertNotNull(result.getAltLabel());
        assertFalse(result.getAltLabel().isEmpty());
        assertTrue(
                result.getAltLabel().stream().anyMatch(ms -> ms.contains("en") && ms.get("en").equals("Construction")));
        assertTrue(result.getAltLabel().stream().anyMatch(ms -> ms.contains("en") && ms.get("en").equals("Building")));
        assertTrue(result.getAltLabel().stream().anyMatch(ms -> ms.contains("cs") && ms.get("cs").equals("Stavba")));
        assertTrue(result.getAltLabel().stream().anyMatch(ms -> ms.contains("cs") && ms.get("cs").equals("Budova")));
    }

    @Test
    void deserializationSupportsOptimisticTargetTypeResolution() throws Exception {
        final Configuration config = sut.configuration();
        config.set(ConfigParam.ENABLE_OPTIMISTIC_TARGET_TYPE_RESOLUTION, Boolean.toString(true));
        config.set(ConfigParam.SCAN_PACKAGE, "cz.cvut.kbss.jsonld.environment.model");
        this.sut = ExpandedJsonLdDeserializer.createExpandedDeserializer(config);
        final Object input = readAndExpand("objectWithPluralOptimisticallyTypedReference.json");
        final StudyOnPersons result = sut.deserialize(input, StudyOnPersons.class);
        assertFalse(result.getParticipants().isEmpty());
        result.getParticipants().forEach(p -> assertThat(p, anyOf(instanceOf(User.class), instanceOf(Employee.class))));
    }

    @Test
    void deserializationSupportsOptimisticTargetTypeResolutionWithSuperclassPreference() throws Exception {
        final Configuration config = sut.configuration();
        config.set(ConfigParam.ENABLE_OPTIMISTIC_TARGET_TYPE_RESOLUTION, Boolean.toString(true));
        config.set(ConfigParam.PREFER_SUPERCLASS, Boolean.toString(true));
        config.set(ConfigParam.IGNORE_UNKNOWN_PROPERTIES, Boolean.toString(true));
        config.set(ConfigParam.SCAN_PACKAGE, "cz.cvut.kbss.jsonld.environment.model");
        this.sut = ExpandedJsonLdDeserializer.createExpandedDeserializer(config);
        final Object input = readAndExpand("objectWithPluralOptimisticallyTypedReference.json");
        final StudyOnPersons result = sut.deserialize(input, StudyOnPersons.class);
        assertFalse(result.getParticipants().isEmpty());
        result.getParticipants().forEach(p -> assertThat(p, instanceOf(Person.class)));
    }

    @Test
    void deserializationThrowsAmbiguousTargetTypeExceptionForAmbiguousTargetTypeWithDisabledOptimisticTargetTypeResolution()
            throws Exception {
        final Object input = readAndExpand("objectWithPluralOptimisticallyTypedReference.json");
        assertThrows(AmbiguousTargetTypeException.class, () -> sut.deserialize(input, StudyOnPersons.class));
    }

    @Test
    void deserializationEnsuresEqualityAndHashCodeBasedCollectionsArePopulatedCorrectly() throws Exception {
        final Object input = readAndExpand("objectWithPluralReference.json");
        final Organization result = sut.deserialize(input, Organization.class);
        assertFalse(result.getEmployees().isEmpty());
        result.getEmployees().forEach(e -> assertFalse(result.getEmployees().add(e)));
    }

    @Test
    void deserializationSupportsCompactedIrisBasedOnJOPANamespaces() throws Exception {
        sut.configuration().set(ConfigParam.IGNORE_UNKNOWN_PROPERTIES, Boolean.toString(true));
        final Object input = readAndExpand("objectWithReadOnlyPropertyValue.json");
        final StudyWithNamespaces result = sut.deserialize(input, StudyWithNamespaces.class);
        assertEquals("LupusStudy", result.getName());
        assertFalse(result.getMembers().isEmpty());
        assertFalse(result.getParticipants().isEmpty());
    }

    /**
     * Bug #36
     */
    @Test
    void deserializationSupportsMultilingualStringWithNoneLanguage() throws Exception {
        final Object input = readAndExpand("objectWithLanguageLessMultilingualString.json");
        final ObjectWithMultilingualString result = sut.deserialize(input, ObjectWithMultilingualString.class);
        assertNotNull(result);
        assertNotNull(result.getLabel());
        assertEquals("Leveraging Semantic Web Technologies in Domain-specific Information Systems",
                     result.getLabel().get());
        assertEquals("Využití technologií sémantického webu v doménových informačních systémech",
                     result.getLabel().get("cs"));
    }

    @Test
    void deserializeUsesCustomDeserializerForRootObjectIfTargetClassMatches() throws Exception {
        final Object input = readAndExpand("objectWithBlankNodeIdentifier.json");
        final PersonDeserializer customDeserializer = spy(new PersonDeserializer());
        sut.registerDeserializer(Person.class, customDeserializer);
        final Person result = sut.deserialize(input, Person.class);
        assertNotNull(result);
        assertEquals(USERS.get(HALSEY_URI).getFirstName(), result.getFirstName());
        assertEquals(USERS.get(HALSEY_URI).getLastName(), result.getLastName());
        verify(customDeserializer).deserialize(anyMap(), any(DeserializationContext.class));
    }

    private static class PersonDeserializer implements ValueDeserializer<Person> {
        @Override
        public Person deserialize(Map<?, ?> jsonNode, DeserializationContext<Person> ctx) {
            final Person result = new Person();
            final Map<?, ?> firstNameMap = (Map<?, ?>) ((List<?>) jsonNode.get(Vocabulary.FIRST_NAME)).get(0);
            result.setFirstName(firstNameMap.get(JsonLd.VALUE).toString());
            final Map<?, ?> lastNameMap = (Map<?, ?>) ((List<?>) jsonNode.get(Vocabulary.LAST_NAME)).get(0);
            result.setLastName(lastNameMap.get(JsonLd.VALUE).toString());
            return result;
        }
    }

    @Test
    void deserializeHandlesCustomDeserializerReturningNull() throws Exception {
        final Object input = readAndExpand("objectWithSingularReference.json");
        final NullDeserializer customDeserializer = spy(new NullDeserializer());
        sut.registerDeserializer(Organization.class, customDeserializer);
        final Employee result = sut.deserialize(input, Employee.class);
        assertNotNull(result);
        assertNull(result.getEmployer());
        verify(customDeserializer).deserialize(anyMap(), any(DeserializationContext.class));
    }

    private static class NullDeserializer implements ValueDeserializer<Organization> {
        @Override
        public Organization deserialize(Map<?, ?> jsonNode, DeserializationContext<Organization> ctx) {
            return null;
        }
    }

    @Test
    void deserializeHandlesObjectWithOnlyType() throws Exception {
        final String input = "{ \"@type\": [\"" + Vocabulary.USER + "\"]}";
        final Object jsonObject = JsonUtils.fromString(input);
        final Object expanded = JsonLdProcessor.expand(jsonObject);
        final User result = sut.deserialize(expanded, User.class);
        assertNotNull(result);
    }

    @Test
    void deserializeSupportsMappingIndividualToEnumConstant() throws Exception {
        final Object input = readAndExpand("objectWithReferenceMappedToEnum.json");
        final Attribute result = sut.deserialize(input, Attribute.class);
        assertNotNull(result);
        assertEquals(OwlPropertyType.DATATYPE_PROPERTY, result.getPropertyType());
    }
}
