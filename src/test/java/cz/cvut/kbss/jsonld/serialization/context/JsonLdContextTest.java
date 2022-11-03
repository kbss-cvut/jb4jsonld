package cz.cvut.kbss.jsonld.serialization.context;

import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.exception.AmbiguousTermMappingException;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.CompositeNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class JsonLdContextTest {

    private final JsonLdContext sut = new JsonLdContext();

    @Test
    void registerTermMappingWithIriAddsTermMappingToLiteralJsonNode() throws Exception {
        final String term = Person.getFirstNameField().getName();
        sut.registerTermMapping(term, Vocabulary.FIRST_NAME);
        assertThat(sut.getMapping().keySet(), hasItem(term));
        assertEquals(sut.getMapping().get(term), JsonNodeFactory.createLiteralNode(term, Vocabulary.FIRST_NAME));
    }

    @Test
    void registerTermMappingWithIriThrowsAmbiguousTermMappingExceptionWhenTermIsAlreadyRegisteredToDifferentIri() throws Exception {
        final String term = Person.getFirstNameField().getName();
        sut.registerTermMapping(term, Vocabulary.FIRST_NAME);
        assertThrows(AmbiguousTermMappingException.class, () -> sut.registerTermMapping(term, Vocabulary.LAST_NAME));
    }

    @Test
    void registerTermMappingWithIriTwiceDoesNotThrowException() throws Exception {
        final String term = Person.getFirstNameField().getName();
        sut.registerTermMapping(term, Vocabulary.FIRST_NAME);
        assertDoesNotThrow(() -> sut.registerTermMapping(term, Vocabulary.FIRST_NAME));
    }

    @Test
    void getContextNodeReturnsCompositeNodeWithRegisteredMappings() throws Exception {
        final String firstName = Person.getFirstNameField().getName();
        sut.registerTermMapping(firstName, Vocabulary.FIRST_NAME);
        final String lastName = Person.getLastNameField().getName();
        sut.registerTermMapping(lastName, Vocabulary.LAST_NAME);

        final JsonNode result = sut.getContextNode();
        assertThat(result, instanceOf(CompositeNode.class));
        final CompositeNode compositeResult = (CompositeNode) result;
        assertThat(compositeResult.getItems(),
                   hasItems(JsonNodeFactory.createLiteralNode(firstName, Vocabulary.FIRST_NAME),
                            JsonNodeFactory.createLiteralNode(lastName, Vocabulary.LAST_NAME)));
    }
}