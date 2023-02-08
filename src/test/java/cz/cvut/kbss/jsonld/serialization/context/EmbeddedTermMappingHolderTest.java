package cz.cvut.kbss.jsonld.serialization.context;

import cz.cvut.kbss.jopa.vocabulary.DC;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jsonld.exception.AmbiguousTermMappingException;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
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

    @Test
    void registerTermMappingDoesNothingWhenParentContextAlreadyHasEquivalentMappingForSpecifiedTerm() {
        final EmbeddedTermMappingHolder child = new EmbeddedTermMappingHolder(sut);
        final String term = "name";
        sut.registerTermMapping(term, JsonNodeFactory.createStringLiteralNode(term, RDFS.LABEL));
        child.registerTermMapping(term, JsonNodeFactory.createStringLiteralNode(term, RDFS.LABEL));
        assertThat(child.getMapping(), not(hasKey(term)));
        assertTrue(child.hasTermMapping(term));
    }
}