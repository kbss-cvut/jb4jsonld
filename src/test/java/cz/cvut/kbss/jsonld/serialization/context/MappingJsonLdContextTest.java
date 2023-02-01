package cz.cvut.kbss.jsonld.serialization.context;

import cz.cvut.kbss.jopa.vocabulary.DC;
import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.serialization.serializer.SerializerUtils;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MappingJsonLdContextTest {

    private final MappingJsonLdContext sut = new MappingJsonLdContext();

    @Test
    void getMappedTermReturnsMappedTermWhenSimpleIriBasedMappingIsRegistered() {
        final String term = "types";
        sut.registerTermMapping(term, JsonLd.TYPE);
        final Optional<String> result = sut.getMappedTerm(JsonLd.TYPE);
        assertTrue(result.isPresent());
        assertEquals(term, result.get());
    }

    @Test
    void getMappedTermReturnsMappedTermWhenObjectNodeIsRegisteredForIt() {
        final String term = "created";
        sut.registerTermMapping("id", JsonLd.ID);
        sut.registerTermMapping(term, SerializerUtils.createTypedTermDefinition(term, DC.Terms.CREATED, XSD.DATE));
        final Optional<String> result = sut.getMappedTerm(DC.Terms.CREATED);
        assertTrue(result.isPresent());
        assertEquals(term, result.get());
    }

    @Test
    void getMappedTermReturnsEmptyOptionalWhenNoMatchingMappingIsRegistered() {
        sut.registerTermMapping("id", JsonLd.ID);
        sut.registerTermMapping("created",
                                SerializerUtils.createTypedTermDefinition("created", DC.Terms.CREATED, XSD.DATE));
        final Optional<String> result = sut.getMappedTerm(Generator.generateUri().toString());
        assertNotNull(result);
        assertFalse(result.isPresent());
    }
}