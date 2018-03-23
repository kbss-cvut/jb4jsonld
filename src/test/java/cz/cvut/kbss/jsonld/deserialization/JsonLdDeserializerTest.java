package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Study;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonLdDeserializerTest {

    @Test
    public void constructionScansClasspathAndBuildsTypeMap() {
        final JsonLdDeserializer deserializer = JsonLdDeserializer.createExpandedDeserializer();
        assertFalse(deserializer.typeMap.get(Vocabulary.STUDY).isEmpty());
        assertTrue(deserializer.typeMap.get(Vocabulary.STUDY).contains(Study.class));
    }

    @Test
    public void constructionScansClasspathWithSpecifiedPackageAndBuildsTypeMap() {
        final Configuration config = new Configuration();
        config.set(ConfigParam.SCAN_PACKAGE, "cz.cvut.kbss.jsonld.deserialization");
        final JsonLdDeserializer deserializer = JsonLdDeserializer.createExpandedDeserializer(config);
        assertTrue(deserializer.typeMap.get(Vocabulary.STUDY).isEmpty());
        assertTrue(deserializer.typeMap.get(Vocabulary.AGENT).contains(TestClass.class));
    }

    @OWLClass(iri = Vocabulary.AGENT)
    public static class TestClass {
    }
}