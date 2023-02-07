package cz.cvut.kbss.jsonld.serialization.context;

import cz.cvut.kbss.jopa.vocabulary.DC;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jsonld.exception.AmbiguousTermMappingException;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddedTermMappingHolderTest {

    private final EmbeddedTermMappingHolder sut = new EmbeddedTermMappingHolder();

    @Test
    void registerTermMappingThrowsAmbiguousTermMappingExceptionWhenRootHolderAlreadyContainsTermMapping() {
        final String term = "name";
        sut.registerTermMapping(term, JsonNodeFactory.createStringLiteralNode(term, RDFS.LABEL));
        assertThrows(AmbiguousTermMappingException.class,
                     () -> sut.registerTermMapping(term,
                                                   JsonNodeFactory.createStringLiteralNode(term, DC.Terms.TITLE)));
    }

}