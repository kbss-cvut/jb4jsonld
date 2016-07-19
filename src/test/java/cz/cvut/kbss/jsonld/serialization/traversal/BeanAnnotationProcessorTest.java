package cz.cvut.kbss.jsonld.serialization.traversal;

import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.environment.model.User;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class BeanAnnotationProcessorTest {

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
}
