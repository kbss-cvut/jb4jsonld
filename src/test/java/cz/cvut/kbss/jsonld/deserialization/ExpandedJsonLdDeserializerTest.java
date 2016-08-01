package cz.cvut.kbss.jsonld.deserialization;

import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.User;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ExpandedJsonLdDeserializerTest {

    private static final URI HALSEY_URI = URI
            .create("http://krizik.felk.cvut.cz/ontologies/jaxb-jsonld#Catherine+Halsey");
    private static final URI LASKY_URI = URI
            .create("http://krizik.felk.cvut.cz/ontologies/jaxb-jsonld#Thomas+Lasky");
    private static final URI PALMER_URI = URI
            .create("http://krizik.felk.cvut.cz/ontologies/jaxb-jsonld#Sarah+Palmer");

    private static final Map<URI, User> USERS = initUsers();

    private static final URI ORG_URI = URI.create("http://krizik.felk.cvut.cz/ontologies/jaxb-jsonld#UNSC");
    private static final String ORG_NAME = "UNSC";
    private static final String[] ORG_BRANDS = {"Spartan-II", "Mjolnir IV"};

    private JsonLdDeserializer deserializer;

    private static Map<URI, User> initUsers() {
        final Map<URI, User> map = new HashMap<>();
        map.put(HALSEY_URI, new User(HALSEY_URI, "Catherine", "Halsey", "halsey@unsc.org", true));
        map.put(LASKY_URI, new User(LASKY_URI, "Thomas", "Lasky", "lasky@unsc.org", false));
        map.put(PALMER_URI, new User(PALMER_URI, "Sarah", "Palmer", "palmer@unsc.org", false));
        return map;
    }

    @Before
    public void setUp() {
        this.deserializer = JsonLdDeserializer.createExpandedDeserializer();
    }

    @Test
    public void testDeserializeInstanceWithDataProperties() throws Exception {
        final Object input = readAndExpand("objectWithDataProperties.json");
        final User result = deserializer.deserialize(input, User.class);
        verifyUserAttributes(USERS.get(HALSEY_URI), result);
    }

    private void verifyUserAttributes(User expected, User actual) {
        assertEquals(expected.getUri(), actual.getUri());
        assertEquals(expected.getAdmin(), actual.getAdmin());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getUsername(), actual.getUsername());
    }

    private Object readAndExpand(String fileName) throws Exception {
        final InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        final Object jsonObject = JsonUtils.fromInputStream(is);
        return JsonLdProcessor.expand(jsonObject);
    }

    @Test
    public void testDeserializeInstanceWithSingularObjectProperty() throws Exception {
        final Object input = readAndExpand("objectWithSingularReference.json");
        final Employee result = deserializer.deserialize(input, Employee.class);
        verifyUserAttributes(USERS.get(HALSEY_URI), result);
        assertNotNull(result.getEmployer());
        verifyOrganizationAttributes(result.getEmployer());
    }

    private void verifyOrganizationAttributes(Organization result) {
        assertEquals(ORG_URI, result.getUri());
        assertEquals(ORG_NAME, result.getName());
        assertNotNull(result.getDateCreated());
        for (String brand : ORG_BRANDS) {
            assertTrue(result.getBrands().contains(brand));
        }
    }

    @Test
    public void testDeserializeInstanceWithPluralObjectProperty() throws Exception {
        final Object input = readAndExpand("objectWithPluralReference.json");
        final Organization result = deserializer.deserialize(input, Organization.class);
        verifyOrganizationAttributes(result);
        assertEquals(3, result.getEmployees().size());
        for (Employee e : result.getEmployees()) {
            assertTrue(USERS.containsKey(e.getUri()));
            verifyUserAttributes(USERS.get(e.getUri()), e);
        }
    }

    @Test
    public void testDeserializerInstanceWithPluralObjectPropertyWithBackwardReferencesToOriginalInstance()
            throws Exception {
        final Object input = readAndExpand("objectWithPluralObjectPropertyWithBackwardReferences.json");
        final Organization result = deserializer.deserialize(input, Organization.class);
        verifyOrganizationAttributes(result);
        assertEquals(3, result.getEmployees().size());
        for (Employee e : result.getEmployees()) {
            assertTrue(USERS.containsKey(e.getUri()));
            verifyUserAttributes(USERS.get(e.getUri()), e);
            assertNotNull(e.getEmployer());
            assertSame(result, e.getEmployer());
        }
    }
}
