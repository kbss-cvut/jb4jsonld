package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.LangStringNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MultilingualStringSerializerTest {

    private static final String ATTRIBUTE_NAME = "label";

    private MultilingualString value;

    private MultilingualStringSerializer sut;

    @BeforeEach
    void setUp() {
        this.value = new MultilingualString();
        this.sut = new MultilingualStringSerializer();
    }

    @Test
    void serializeWithAttributeAndValuesReturnsCollectionNodeWithTranslations() {
        value.set("en", "construction");
        value.set("cs", "stavba");
        final JsonNode result = sut.serialize(ATTRIBUTE_NAME, value);
        assertThat(result, instanceOf(CollectionNode.class));
        final CollectionNode colNode = (CollectionNode) result;
        assertEquals(ATTRIBUTE_NAME, result.getName());
        assertEquals(2, colNode.getItems().size());
        colNode.getItems().forEach(item -> assertThat(item, instanceOf(LangStringNode.class)));
    }

    @Test
    void serializeWithAttributeAndSingleTranslationReturnsLangStringNode() {
        value.set("en", "construction");
        final JsonNode result = sut.serialize(ATTRIBUTE_NAME, value);
        assertThat(result, instanceOf(LangStringNode.class));
        assertEquals(ATTRIBUTE_NAME, result.getName());
    }

    @Test
    void serializeWithAttributeAndValuesHandlesSimpleLiteral() {
        value.set("construction");
        value.set("cs", "stavba");
        final JsonNode result = sut.serialize(ATTRIBUTE_NAME, value);
        assertThat(result, instanceOf(CollectionNode.class));
        final CollectionNode colNode = (CollectionNode) result;
        assertEquals(ATTRIBUTE_NAME, result.getName());
        assertEquals(2, colNode.getItems().size());
        colNode.getItems().forEach(item -> assertThat(item, instanceOf(LangStringNode.class)));
    }

    @Test
    void serializeReturnsCollectionNodeWithTranslations() {
        value.set("en", "construction");
        value.set("cs", "stavba");
        final JsonNode result = sut.serialize(value);
        assertThat(result, instanceOf(CollectionNode.class));
        final CollectionNode colNode = (CollectionNode) result;
        assertEquals(2, colNode.getItems().size());
        colNode.getItems().forEach(item -> assertThat(item, instanceOf(LangStringNode.class)));
    }

    @Test
    void serializeWithSingleTranslationReturnsLangStringNode() {
        value.set("en", "construction");
        final JsonNode result = sut.serialize(value);
        assertThat(result, instanceOf(LangStringNode.class));
    }
}
