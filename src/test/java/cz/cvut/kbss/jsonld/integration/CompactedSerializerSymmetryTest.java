package cz.cvut.kbss.jsonld.integration;

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.serialization.JsonLdSerializer;
import cz.cvut.kbss.jsonld.serialization.util.BufferedJsonGenerator;

public class CompactedSerializerSymmetryTest extends DeSerializationSymmetryTest {

    @Override
    JsonLdSerializer getSerializer(BufferedJsonGenerator jsonWriter, Configuration configuration) {
        return JsonLdSerializer.createCompactedJsonLdSerializer(jsonWriter, configuration);
    }
}
