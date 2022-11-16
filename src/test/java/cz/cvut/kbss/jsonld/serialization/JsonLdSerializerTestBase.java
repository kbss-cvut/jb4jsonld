package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.TestUtil;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.*;
import cz.cvut.kbss.jsonld.exception.MissingIdentifierException;
import cz.cvut.kbss.jsonld.serialization.util.BufferedJsonGenerator;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static cz.cvut.kbss.jsonld.environment.IsIsomorphic.isIsomorphic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Contains tests common to all {@link JsonLdSerializer} implementations.
 */
public class JsonLdSerializerTestBase {

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
}
