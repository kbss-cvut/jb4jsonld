package cz.cvut.kbss.jsonld.common;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.Types;
import cz.cvut.kbss.jsonld.annotation.JsonLdProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.URI;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonLdPropertyAccessResolverTest {

    private JsonLdPropertyAccessResolver sut = new JsonLdPropertyAccessResolver();

    @ParameterizedTest
    @CsvSource({
            "withoutAccessConfig, true",
            "withReadWriteAccess, true",
            "withReadOnlyAccess,  true",
            "withWriteOnlyAccess, false,",
            "types,               true",
            "id,                  true"})
    void isReadable(String fieldName, boolean result) throws Exception {
        assertEquals(result, sut.isReadable(TestClass.class.getDeclaredField(fieldName)));
    }

    @ParameterizedTest
    @CsvSource({
            "withoutAccessConfig, true",
            "withReadWriteAccess, true",
            "withReadOnlyAccess,  false",
            "withWriteOnlyAccess, true"})
    void isWriteable(String fieldName, boolean result) throws Exception {
        assertEquals(result, sut.isWriteable(TestClass.class.getDeclaredField(fieldName)));
    }

    @SuppressWarnings("unused")
    private static class TestClass {

        // This is ignored for serialization
        @JsonLdProperty(access = JsonLdProperty.Access.WRITE_ONLY)
        @Id
        private URI id;

        @OWLDataProperty(iri = "http://withoutAccessConfig")
        private String withoutAccessConfig;

        @JsonLdProperty
        @OWLDataProperty(iri = "http://withReadWriteAccess")
        private String withReadWriteAccess;

        @JsonLdProperty(access = JsonLdProperty.Access.READ_ONLY)
        @OWLDataProperty(iri = "http://withReadOnlyAccess")
        private String withReadOnlyAccess;

        @JsonLdProperty(access = JsonLdProperty.Access.WRITE_ONLY)
        @OWLDataProperty(iri = "http://withWriteOnlyAccess")
        private String withWriteOnlyAccess;

        // This is ignored for serialization
        @JsonLdProperty(access = JsonLdProperty.Access.WRITE_ONLY)
        @Types
        private Set<String> types;
    }
}
