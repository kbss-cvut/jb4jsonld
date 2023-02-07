package cz.cvut.kbss.jsonld.serialization.context;

import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.CompositeNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.*;

class JsonLdContextTest {

    private final MappingJsonLdContext sut = new MappingJsonLdContext();

    @Test
    void registerTermMappingWithIriAddsTermMappingToLiteralJsonNode() throws Exception {
        final String term = Person.getFirstNameField().getName();
        sut.registerTermMapping(term, Vocabulary.FIRST_NAME);
        assertTrue(sut.hasTermMapping(term));
        final Optional<JsonNode> result = sut.getTermMapping(term);
        assertTrue(result.isPresent());
        assertEquals(result.get(), JsonNodeFactory.createLiteralNode(term, Vocabulary.FIRST_NAME));
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
        final CompositeNode<?> compositeResult = (CompositeNode<?>) result;
        assertThat(compositeResult.getItems(),
                   hasItems(JsonNodeFactory.createLiteralNode(firstName, Vocabulary.FIRST_NAME),
                            JsonNodeFactory.createLiteralNode(lastName, Vocabulary.LAST_NAME)));
    }
}