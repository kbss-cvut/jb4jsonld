package cz.cvut.kbss.jsonld.serialization.context;

import cz.cvut.kbss.jopa.vocabulary.DC;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
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

    @Test
    void getTermMappingRetrievesMappingFromParentContextWhenAvailable() {
        sut.registerTermMapping("id", JsonLd.ID);
        final MappingJsonLdContext childSut = new MappingJsonLdContext(sut);
        final Optional<JsonNode> result = childSut.getTermMapping("id");
        assertTrue(result.isPresent());
        assertEquals(JsonNodeFactory.createStringLiteralNode("id", JsonLd.ID), result.get());
    }

    @Test
    void getTermMappingReturnsMappingFromCurrentContextThatOverridesParentMapping() {
        final String term = "name";
        sut.registerTermMapping(term, RDFS.LABEL);
        final MappingJsonLdContext childSut = new MappingJsonLdContext(sut);
        childSut.registerTermMapping(term, DC.Terms.TITLE);
        final Optional<JsonNode> result = childSut.getTermMapping(term);
        assertTrue(result.isPresent());
        assertEquals(JsonNodeFactory.createStringLiteralNode(term, DC.Terms.TITLE), result.get());
    }

    @Test
    void hasTermMappingReturnsTrueWhenTermIsMappedInParentContext() {
        sut.registerTermMapping("id", JsonLd.ID);
        final MappingJsonLdContext childSut = new MappingJsonLdContext(sut);
        assertTrue(childSut.hasTermMapping("id"));
    }

    @Test
    void registerTermMappingRegistersTermsIntoParentContextAsLongAsNoMappingConflictsAppear() {
        final MappingJsonLdContext childSut = new MappingJsonLdContext(sut);
        final String term = "name";
        childSut.registerTermMapping(term, RDFS.LABEL);
        childSut.registerTermMapping("uri", JsonLd.ID);
        assertTrue(sut.hasTermMapping(term));
        assertTrue(sut.hasTermMapping("uri"));
        assertTrue(childSut.isCurrentEmpty());
        childSut.registerTermMapping(term, DC.Terms.TITLE);
        assertTrue(childSut.hasTermMapping(term));
        final Optional<JsonNode> parentResult = sut.getTermMapping(term);
        assertTrue(parentResult.isPresent());
        assertEquals(JsonNodeFactory.createStringLiteralNode(term, RDFS.LABEL), parentResult.get());
        final Optional<JsonNode> childResult = childSut.getTermMapping(term);
        assertTrue(childResult.isPresent());
        assertEquals(JsonNodeFactory.createStringLiteralNode(term, DC.Terms.TITLE), childResult.get());
    }

    @Test
    void registerTermMappingRegistersTermsIntoParentContextAfterMappingConflictWhenRegisteredTermIsNotInConflict() {
        final MappingJsonLdContext childSut = new MappingJsonLdContext(sut);
        final String term = "name";
        childSut.registerTermMapping(term, RDFS.LABEL);
        childSut.registerTermMapping(term, DC.Terms.TITLE);
        childSut.registerTermMapping("created", SerializerUtils.createTypedTermDefinition("created", DC.Terms.CREATED,
                                                                                          XSD.DATETIME));
        assertTrue(sut.hasTermMapping("created"));
    }
}