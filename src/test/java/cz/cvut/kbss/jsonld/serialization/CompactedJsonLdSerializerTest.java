package cz.cvut.kbss.jsonld.serialization;

import com.github.jsonldjava.utils.JsonUtils;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.environment.model.User;
import cz.cvut.kbss.jsonld.serialization.util.BufferedJsonSerializer;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertNotNull;

public class CompactedJsonLdSerializerTest {

    private BufferedJsonSerializer jsonWriter;

    private JsonLdSerializer serializer;

    @Before
    public void setUp() {
        this.jsonWriter = new BufferedJsonSerializer();
        this.serializer = new CompactedJsonLdSerializer(jsonWriter);
    }

    // The following tests only verify validity of the output JSON-LD, no structure checks are performed

    @Test
    public void testSerializeObjectWithDataProperties() throws Exception {
        final User user = Generator.generateUser();
        serializer.serialize(user);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertNotNull(jsonObject);
    }

    @Test
    public void testSerializeCollectionOfObjects() throws Exception {
        final Set<User> users = Generator.generateUsers();
        serializer.serialize(users);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertNotNull(jsonObject);
    }

    @Test
    public void testSerializeObjectWithSingularReference() throws Exception {
        final Employee employee = Generator.generateEmployee();
        serializer.serialize(employee);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertNotNull(jsonObject);
    }

    @Test
    public void testSerializeObjectWithPluralReference() throws Exception {
        final Organization org = Generator.generateOrganization();
        generateEmployees(org, false);  // No backward references for this test
        serializer.serialize(org);
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertNotNull(jsonObject);
    }

    private void generateEmployees(Organization org, boolean withBackwardReference) {
        for (int i = 0; i < Generator.randomCount(10); i++) {
            final Employee emp = Generator.generateEmployee();
            emp.setEmployer(withBackwardReference ? org : null);
            org.addEmployee(emp);
        }
    }

    @Test
    public void testSerializeObjectWithBackwardReferences() throws Exception {
        final Organization org = Generator.generateOrganization();
        generateEmployees(org, true);
        serializer.serialize(org);
        System.out.println(jsonWriter.getResult());
        Object jsonObject = JsonUtils.fromString(jsonWriter.getResult());
        assertNotNull(jsonObject);
    }
}
