package cz.cvut.kbss.jsonld.serialization.traversal;

import cz.cvut.kbss.jopa.CommonVocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jsonld.Constants;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.environment.model.User;
import cz.cvut.kbss.jsonld.exception.JsonLdSerializationException;
import cz.cvut.kbss.jsonld.serialization.BeanAnnotationProcessor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class BeanAnnotationProcessorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void getOwlClassesExtractsClassIriFromObject() {
        final Person p = new Person();
        final Set<String> result = BeanAnnotationProcessor.getOwlClasses(p);
        assertEquals(1, result.size());
        assertTrue(result.contains(Vocabulary.PERSON));
    }

    @Test
    public void getOwlClassesReturnsNullForNonOwlClass() {
        final URI uri = URI.create("http://test");
        assertTrue(BeanAnnotationProcessor.getOwlClasses(uri).isEmpty());
    }

    @Test
    public void getOwlClassesExtractsClassesFromAncestors() {
        final User u = new User();
        final Set<String> result = BeanAnnotationProcessor.getOwlClasses(u);
        assertTrue(result.contains(Vocabulary.USER));
        assertTrue(result.contains(Vocabulary.PERSON));
    }

    @Test
    public void getSerializableFieldsExtractsAllFieldsOfAnInstanceIncludingSuperclassOnes() throws Exception {
        final User u = new User();
        final List<Field> fields = BeanAnnotationProcessor.getSerializableFields(u);
        assertTrue(fields.contains(User.class.getDeclaredField("username")));
        assertTrue(fields.containsAll(
                Arrays.asList(Person.class.getDeclaredField("uri"), Person.class.getDeclaredField("firstName"),
                        Person.class.getDeclaredField("lastName"))));
    }

    @Test
    public void getSerializableFieldsSkipsStaticFields() throws Exception {
        final Organization org = new Organization();
        final List<Field> fields = BeanAnnotationProcessor.getSerializableFields(org);
        assertFalse(fields.contains(Organization.class.getDeclaredField("DEFAULT_COUNTRY")));
    }

    @Test
    public void getSerializableFieldsSkipsNonAnnotatedFields() throws Exception {
        final Organization org = new Organization();
        final List<Field> fields = BeanAnnotationProcessor.getSerializableFields(org);
        assertFalse(fields.contains(Organization.class.getDeclaredField("age")));
    }

    @Test
    public void getAttributeIdentifierReturnsIdForIdAttribute() throws Exception {
        final String id = BeanAnnotationProcessor.getAttributeIdentifier(Person.class.getDeclaredField("uri"));
        assertEquals(Constants.JSON_LD_ID, id);
    }

    @Test
    public void getAttributeIdentifierReturnsIriOfOWLDataProperty() throws Exception {
        final String id = BeanAnnotationProcessor.getAttributeIdentifier(Person.class.getDeclaredField("firstName"));
        assertEquals(Vocabulary.FIRST_NAME, id);
    }

    @Test
    public void getAttributeIdentifierReturnsIriOfOWLObjectProperty() throws Exception {
        final String id = BeanAnnotationProcessor.getAttributeIdentifier(Employee.class.getDeclaredField("employer"));
        assertEquals(Vocabulary.IS_MEMBER_OF, id);
    }

    @Test
    public void getAttributeIdentifierReturnsIriOfOWLAnnotationProperty() throws Exception {
        final String id = BeanAnnotationProcessor.getAttributeIdentifier(Organization.class.getDeclaredField("name"));
        assertEquals(CommonVocabulary.RDFS_LABEL, id);
    }

    @Test
    public void getAttributeIdentifierThrowsIllegalArgumentWhenFieldIsNotJsonLdSerializable() throws Exception {
        thrown.expect(JsonLdSerializationException.class);
        thrown.expectMessage(
                "Field " + Organization.class.getDeclaredField("DEFAULT_COUNTRY") + " is not JSON-LD serializable.");
        BeanAnnotationProcessor.getAttributeIdentifier(Organization.class.getDeclaredField("DEFAULT_COUNTRY"));
    }

    @Test
    public void getInstanceIdentifierExtractsIdFieldValue() throws Exception {
        final Organization org = Generator.generateOrganization();
        assertEquals(org.getUri(), BeanAnnotationProcessor.getInstanceIdentifier(org));
    }

    @Test
    public void getInstanceIdentifierExtractsIdFieldFromAncestorClass() throws Exception {
        final Employee e = Generator.generateEmployee();
        assertEquals(e.getUri(), BeanAnnotationProcessor.getInstanceIdentifier(e));
    }

    @Test
    public void getInstanceIdentifierThrowsExceptionWhenIdentifierFieldIsNotFound() {
        final Object instance = new ClassWithoutIdentifier();
        thrown.expect(JsonLdSerializationException.class);
        thrown.expectMessage("Instance " + instance + " contains no valid identifier field.");
        BeanAnnotationProcessor.getInstanceIdentifier(instance);
    }

    @OWLClass(iri = "http://krizik.felk.cvut.cz/ontologies/jaxb-jsonld/ClassWithoutIdentifier")
    private static class ClassWithoutIdentifier {
        @OWLAnnotationProperty(iri = CommonVocabulary.RDFS_LABEL)
        private String label;
    }
}
