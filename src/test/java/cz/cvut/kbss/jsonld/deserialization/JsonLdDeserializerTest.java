package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.deserialization.util.TargetClassResolver;
import cz.cvut.kbss.jsonld.deserialization.util.TypeMap;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Study;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonLdDeserializerTest {

    @Test
    public void constructionScansClasspathAndBuildsTypeMap() throws Exception {
        final JsonLdDeserializer deserializer = JsonLdDeserializer.createExpandedDeserializer();
        assertFalse(typeMap(deserializer).get(Vocabulary.STUDY).isEmpty());
        assertTrue(typeMap(deserializer).get(Vocabulary.STUDY).contains(Study.class));
    }

    private TypeMap typeMap(JsonLdDeserializer deserializer) throws Exception {
        final Field typeMapField = TargetClassResolver.class.getDeclaredField("typeMap");
        typeMapField.setAccessible(true);
        return (TypeMap) typeMapField.get(deserializer.classResolver);
    }

    @Test
    public void constructionScansClasspathWithSpecifiedPackageAndBuildsTypeMap() throws Exception {
        final Configuration config = new Configuration();
        config.set(ConfigParam.SCAN_PACKAGE, "cz.cvut.kbss.jsonld.deserialization");
        final JsonLdDeserializer deserializer = JsonLdDeserializer.createExpandedDeserializer(config);
        assertTrue(typeMap(deserializer).get(Vocabulary.STUDY).isEmpty());
        assertTrue(typeMap(deserializer).get(Vocabulary.AGENT).contains(TestClass.class));
    }

    @OWLClass(iri = Vocabulary.AGENT)
    public static class TestClass {
    }
}