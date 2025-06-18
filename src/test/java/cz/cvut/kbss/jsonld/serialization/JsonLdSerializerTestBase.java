/*
 * JB4JSON-LD
 * Copyright (C) 2025 Czech Technical University in Prague
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

import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.common.IdentifierUtil;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.TestUtil;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Attribute;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.GeneratesRdf;
import cz.cvut.kbss.jsonld.environment.model.GenericMember;
import cz.cvut.kbss.jsonld.environment.model.ObjectWithAnnotationProperties;
import cz.cvut.kbss.jsonld.environment.model.ObjectWithNumericAttributes;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.OwlPropertyType;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.environment.model.PersonWithTypedProperties;
import cz.cvut.kbss.jsonld.environment.model.Role;
import cz.cvut.kbss.jsonld.environment.model.Study;
import cz.cvut.kbss.jsonld.environment.model.User;
import cz.cvut.kbss.jsonld.exception.MissingIdentifierException;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.util.BufferedJsonGenerator;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static cz.cvut.kbss.jsonld.environment.IsIsomorphic.isIsomorphic;
import static cz.cvut.kbss.jsonld.environment.TestUtil.parseAndExpand;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contains tests common to all {@link JsonLdSerializer} implementations.
 */
public abstract class JsonLdSerializerTestBase {

    protected BufferedJsonGenerator jsonWriter = new BufferedJsonGenerator();

    protected JsonLdSerializer sut;

    protected static ValueFactory vf() {
        return TestUtil.VALUE_FACTORY;
    }

    protected Model toRdf(GeneratesRdf instance) {
        final Model model = new LinkedHashModel();
        final Set<URI> visited = new HashSet<>();
        instance.toRdf(model, TestUtil.VALUE_FACTORY, visited);
        return model;
    }

    protected Model readJson(String json) throws IOException {
        final Model model = new LinkedHashModel();
        final RDFParser rdfParser = Rio.createParser(RDFFormat.JSONLD);
        rdfParser.setRDFHandler(new StatementCollector(model));
        rdfParser.parse(new StringReader(json));
        return model;
    }

    protected JsonValue serializeAndRead(Object value) {
        sut.serialize(value);
        return Json.createReader(new ByteArrayInputStream(jsonWriter.getResult().getBytes(StandardCharsets.UTF_8)))
                   .read();
    }

    @Test
    void testSerializeObjectWithSingularReference() throws Exception {
        final Employee employee = Generator.generateEmployee();

        sut.serialize(employee);
        final Model expected = toRdf(employee);
        final Model actual = readJson(jsonWriter.getResult());
        assertThat(actual, isIsomorphic(expected));
    }

    @Test
    void testSerializeObjectWithPluralReference() throws Exception {
        final Organization org = Generator.generateOrganization();
        generateEmployees(org, false);  // No backward references for this test

        sut.serialize(org);
        final Model expected = toRdf(org);
        final Model actual = readJson(jsonWriter.getResult());
        assertThat(actual, isIsomorphic(expected));
    }

    protected void generateEmployees(Organization org, boolean withBackwardReference) {
        for (int i = 0; i < Generator.randomInt(5, 10); i++) {
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
        final Model expected = toRdf(org);
        final Model actual = readJson(jsonWriter.getResult());
        assertThat(actual, isIsomorphic(expected));
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
        final Model expected = toRdf(org);
        final Model actual = readJson(jsonWriter.getResult());
        assertThat(actual, isIsomorphic(expected));
    }

    @Test
    void testSerializationOfObjectWithStringBasedUnmappedProperties() throws Exception {
        final Person person = Generator.generatePerson();

        sut.serialize(person);
        final Model expected = toRdf(person);
        final Model actual = readJson(jsonWriter.getResult());
        assertThat(actual, isIsomorphic(expected));
    }

    @Test
    void serializationPutsOwlClassAndTypesContentIntoOneTypeProperty() throws Exception {
        final User user = Generator.generateUser();
        final String type = Generator.URI_BASE + "TypeOne";
        user.setTypes(Collections.singleton(type));
        sut.serialize(user);
        final Model expected = toRdf(user);
        final Model actual = readJson(jsonWriter.getResult());
        final Model expectedTypes = expected.filter(vf().createIRI(user.getUri().toString()), RDF.TYPE, null);
        final Model actualTypes = actual.filter(vf().createIRI(user.getUri().toString()), RDF.TYPE, null);
        assertEquals(expectedTypes, actualTypes);
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
    void testSerializeObjectWithDataProperties() throws Exception {
        final User user = Generator.generateUser();

        sut.serialize(user);
        final Model expected = toRdf(user);
        final Model actual = readJson(jsonWriter.getResult());
        assertThat(actual, isIsomorphic(expected));
    }

    @Test
    void serializationSerializesPlainIdentifierObjectPropertyValue() throws Exception {
        final Organization company = Generator.generateOrganization();
        company.setCountry(URI.create("http://dbpedia.org/resource/Czech_Republic"));
        sut.serialize(company);
        final Model result = readJson(jsonWriter.getResult());
        assertThat(result, hasItem(vf().createStatement(vf().createIRI(company.getUri().toString()),
                                                        vf().createIRI(Vocabulary.ORIGIN),
                                                        vf().createIRI(company.getCountry().toString()))));
    }

    @Test
    void serializationGeneratesBlankNodeIdentifierForInstanceOfClassWithoutIdentifierField() {
        final PersonWithoutIdentifier person = new PersonWithoutIdentifier();
        person.firstName = "Thomas";
        person.lastName = "Lasky";
        final JsonValue json = serializeAndRead(person);
        assertEquals(JsonValue.ValueType.OBJECT, json.getValueType());
        final String id = json.asJsonObject().getString(JsonLd.ID);
        assertNotNull(id);
        assertThat(id, startsWith(IdentifierUtil.B_NODE_PREFIX));
    }

    @OWLClass(iri = Vocabulary.PERSON)
    protected static class PersonWithoutIdentifier {


        @OWLDataProperty(iri = Vocabulary.FIRST_NAME)
        protected String firstName;

        @OWLDataProperty(iri = Vocabulary.LAST_NAME)
        protected String lastName;
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
    void serializationUsesGeneratedBlankNodeForObjectReference() throws Exception {
        final Organization company = Generator.generateOrganization();
        company.setUri(null);
        final Employee employee = Generator.generateEmployee();
        employee.setEmployer(company);
        company.addEmployee(employee);
        sut.serialize(company);
        final Model result = readJson(jsonWriter.getResult());
        final Iterator<Statement> statements = result.getStatements(vf().createIRI(employee.getUri().toString()),
                                                                    vf().createIRI(Vocabulary.IS_MEMBER_OF), null)
                                                     .iterator();
        assertTrue(statements.hasNext());
        while (statements.hasNext()) {
            final Statement s = statements.next();
            assertTrue(s.getObject().isBNode());
            assertThat(result, hasItem(vf().createStatement((BNode) s.getObject(), RDF.TYPE,
                                                            vf().createIRI(Vocabulary.ORGANIZATION))));
        }
    }

    @Test
    void serializationSkipsPropertiesWithWriteOnlyAccess() throws Exception {
        final User user = Generator.generateUser();
        user.setPassword("test-117");
        sut.serialize(user);
        final Model result = readJson(jsonWriter.getResult());
        assertFalse(result.contains(vf().createIRI(user.getUri().toString()),
                                    vf().createIRI(Vocabulary.PASSWORD), null));
    }

    @Test
    void serializationSerializesPropertyWithReadOnlyAccess() throws Exception {
        final Study study = new Study();
        study.setUri(Generator.generateUri());
        study.setName("Test study");
        study.setParticipants(Collections.singleton(Generator.generateEmployee()));
        study.setMembers(Collections.singleton(Generator.generateEmployee()));

        sut.serialize(study);
        final Model expected = toRdf(study);
        final Model actual = readJson(jsonWriter.getResult());
        assertThat(actual, isIsomorphic(expected));
    }

    @Test
    void serializationSerializesAnnotationPropertyStringValueAsString() throws Exception {
        final ObjectWithAnnotationProperties toSerialize = new ObjectWithAnnotationProperties(Generator.generateUri());
        toSerialize.setChangedValue(Generator.generateUri().toString());

        sut.serialize(toSerialize);
        final Model expected = toRdf(toSerialize);
        final Model actual = readJson(jsonWriter.getResult());
        assertThat(actual, isIsomorphic(expected));
    }

    @Test
    void serializationSerializesUrisOfAnnotationPropertyAttributeAsObjectsWithId() throws Exception {
        final ObjectWithAnnotationProperties toSerialize = new ObjectWithAnnotationProperties(Generator.generateUri());
        toSerialize
                .setOrigins(IntStream.range(0, 5).mapToObj(i -> Generator.generateUri()).collect(Collectors.toSet()));

        sut.serialize(toSerialize);
        final Model expected = toRdf(toSerialize);
        final Model actual = readJson(jsonWriter.getResult());
        assertThat(actual, isIsomorphic(expected));
    }

    @Test
    void serializationSerializesEnumDataPropertyAsStringValueOfEnumConstant() throws Exception {
        final User user = Generator.generateUser();
        user.setRole(Role.ADMIN);

        sut.serialize(user);
        final Model result = readJson(jsonWriter.getResult());
        assertTrue(result.contains(vf().createIRI(user.getUri().toString()), vf().createIRI(Vocabulary.ROLE),
                                   vf().createLiteral(Role.ADMIN.toString())));
    }

    @Test
    void serializationSerializesConcreteValueOfFieldOfTypeObject() throws Exception {
        final GenericMember instance = new GenericMember();
        instance.setUri(Generator.generateUri());
        instance.setMemberOf(Generator.generateOrganization());

        sut.serialize(instance);
        final Model expected = toRdf(instance);
        final Model actual = readJson(jsonWriter.getResult());
        assertThat(actual, isIsomorphic(expected));
    }

    @Test
    void serializationSupportsRegistrationAndUsageOfCustomSerializers() throws Exception {
        sut.registerSerializer(LocalDate.class, ((value, ctx) -> JsonNodeFactory.createLiteralNode(ctx.getTerm(),
                                                                                                   value.toString())));
        final CompactedJsonLdSerializerTest.OrganizationWithLocalDate
                organization = new CompactedJsonLdSerializerTest.OrganizationWithLocalDate();
        organization.uri = Generator.generateUri();
        organization.created = LocalDate.now();

        sut.serialize(organization);
        final Model result = readJson(jsonWriter.getResult());
        assertThat(result, hasItem(vf().createStatement(vf().createIRI(organization.uri.toString()),
                                                        vf().createIRI(Vocabulary.DATE_CREATED),
                                                        vf().createLiteral(organization.created.toString()))));
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
        final ValueSerializer<Organization> serializer = (value, ctx) -> {
            final ObjectNode node = JsonNodeFactory.createObjectNode(ctx.getTerm());
            node.addItem(JsonNodeFactory.createObjectIdNode(JsonLd.ID, value.getUri()));
            return node;
        };
        sut.registerSerializer(Organization.class, serializer);
        final Employee employee = Generator.generateEmployee();

        sut.serialize(employee);
        final Model result = readJson(jsonWriter.getResult());
        assertThat(result, hasItem(vf().createStatement(vf().createIRI(employee.getUri().toString()),
                                                        vf().createIRI(Vocabulary.IS_MEMBER_OF),
                                                        vf().createIRI(employee.getEmployer().getUri().toString()))));
    }

    @Test
    void serializationSupportsUsageOfCustomObjectPropertyValueSerializersOnPluralAttributes() throws Exception {
        final ValueSerializer<Employee> serializer = (value, ctx) -> {
            final ObjectNode node =
                    ctx.getTerm() != null ? JsonNodeFactory.createObjectNode(ctx.getTerm()) :
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

        sut.serialize(organization);
        final Model result = readJson(jsonWriter.getResult());
        organization.getEmployees().forEach(e -> {
            assertThat(result, hasItem(vf().createStatement(vf().createIRI(organization.getUri().toString()),
                                                            vf().createIRI(Vocabulary.HAS_MEMBER),
                                                            vf().createIRI(e.getUri().toString()))));
            final Model empResult = result.filter(vf().createIRI(e.getUri().toString()), null, null);
            assertEquals(1, empResult.size());
            assertThat(empResult, hasItems(vf().createStatement(vf().createIRI(e.getUri().toString()),
                                                                vf().createIRI(Vocabulary.USERNAME),
                                                                vf().createLiteral(e.getUsername()))));
        });
    }

    @Test
    void serializationSerializesEnumConstantMappedToIndividualAsIndividual() throws Exception {
        final Attribute instance = new Attribute();
        instance.setUri(Generator.generateUri());
        instance.setPropertyType(OwlPropertyType.values()[Generator.randomInt(0, OwlPropertyType.values().length)]);
        sut.serialize(instance);
        final Model expected = toRdf(instance);
        final Model actual = readJson(jsonWriter.getResult());
        assertThat(actual, isIsomorphic(expected));
    }

    @Test
    void serializationSerializesAttributeWithCollectionOfEnumConstantsMappedToIndividualsAsArrayOfIndividuals() throws Exception {
        final Attribute instance = new Attribute();
        instance.setUri(Generator.generateUri());
        instance.setPluralPropertyType(new HashSet<>(Arrays.asList(OwlPropertyType.values())));
        sut.serialize(instance);
        final Model expected = toRdf(instance);
        final Model actual = readJson(jsonWriter.getResult());
        assertThat(actual, isIsomorphic(expected));
    }

    @Test
    void serializationIncludesDatatypeOfNumericLiterals() throws Exception {
        final ObjectWithNumericAttributes instance = new ObjectWithNumericAttributes(Generator.generateUri());
        instance.setDoubleValue(155.15);
        instance.setFloatValue(155.15f);
        instance.setLongValue(155L);
        instance.setShortValue((short) 155);
        instance.setIntValue(155);
        instance.setBigIntegerValue(BigInteger.valueOf(155L));
        instance.setBigDecimalValue(BigDecimal.valueOf(155.15));
        sut.serialize(instance);
        final JsonArray result = parseAndExpand(jsonWriter.getResult());
        final JsonObject obj = result.getJsonObject(0);
        checkValueDatatype(obj, Vocabulary.DEFAULT_PREFIX + "doubleValue", XSD.DOUBLE, instance.getDoubleValue());
        checkValueDatatype(obj, Vocabulary.DEFAULT_PREFIX + "floatValue", XSD.FLOAT, instance.getFloatValue());
        checkValueDatatype(obj, Vocabulary.DEFAULT_PREFIX + "longValue", XSD.LONG, instance.getLongValue());
        checkValueDatatype(obj, Vocabulary.DEFAULT_PREFIX + "shortValue", XSD.SHORT, instance.getShortValue());
        checkValueDatatype(obj, Vocabulary.DEFAULT_PREFIX + "intValue", XSD.INT, instance.getIntValue());
        checkValueDatatype(obj, Vocabulary.DEFAULT_PREFIX + "bigIntegerValue", XSD.INTEGER, instance.getBigIntegerValue());
        checkValueDatatype(obj, Vocabulary.DEFAULT_PREFIX + "bigDecimalValue", XSD.DECIMAL, instance.getBigDecimalValue());
    }

    private static void checkValueDatatype(JsonObject result, String attId, String datatype, Number value) {
        final JsonArray att = result.getJsonArray(attId);
        assertEquals(1, att.size());
        assertEquals(datatype, att.getJsonObject(0).getString("@type"));
        assertEquals(JsonValue.ValueType.STRING, att.getJsonObject(0).get("@value").getValueType());
        assertEquals(value.toString(), att.getJsonObject(0).getJsonString("@value").getString());
    }

    @Test
    void serializeEmptyCollectionReturnsEmptyArray() {
        sut.serialize(List.of());
        assertEquals("[]", jsonWriter.getResult());
    }
}
