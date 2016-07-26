package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.model.User;
import org.junit.Before;
import org.junit.Test;

public class CompactedJsonLdSerializerTest {

    private BufferedJsonSerializer jsonWriter;

    private JsonLdSerializer serializer;

    @Before
    public void setUp() {
        this.jsonWriter = new BufferedJsonSerializer();
        this.serializer = new CompactedJsonLdSerializer(jsonWriter);
    }

    @Test
    public void testSerializeObjectWithDataProperties() {
        final User user = Generator.generateUser();
        serializer.serialize(user);
        System.out.println(jsonWriter.getResult());
    }
}
