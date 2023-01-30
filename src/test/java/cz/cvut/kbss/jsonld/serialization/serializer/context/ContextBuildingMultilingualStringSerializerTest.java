package cz.cvut.kbss.jsonld.serialization.serializer.context;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.model.ObjectWithMultilingualString;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.context.DummyJsonLdContext;
import cz.cvut.kbss.jsonld.serialization.context.JsonLdContext;
import cz.cvut.kbss.jsonld.serialization.context.MappingJsonLdContext;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ContextBuildingMultilingualStringSerializerTest {

    private final ContextBuildingMultilingualStringSerializer sut = new ContextBuildingMultilingualStringSerializer();

    @Test
    void serializeRegistersTermMappingWithLanguageTypeInJsonLdContext() throws Exception {
        final MultilingualString value = MultilingualString.create("test", "en");
        final JsonLdContext jsonLdCtx = mock(JsonLdContext.class);
        sut.serialize(value, new SerializationContext<>(RDFS.LABEL, ObjectWithMultilingualString.getLabelField(), value,
                                                        jsonLdCtx));
        final ArgumentCaptor<JsonNode> captor = ArgumentCaptor.forClass(JsonNode.class);
        verify(jsonLdCtx).registerTermMapping(eq(ObjectWithMultilingualString.getLabelField().getName()),
                                              captor.capture());
        assertInstanceOf(ObjectNode.class, captor.getValue());
        assertThat(((ObjectNode) captor.getValue()).getItems(), hasItems(
                JsonNodeFactory.createLiteralNode(JsonLd.ID, RDFS.LABEL),
                JsonNodeFactory.createLiteralNode(JsonLd.CONTAINER, JsonLd.LANGUAGE)
        ));
    }

    @Test
    void serializeSerializesMultilingualStringAsMapWithLanguageAsKey() throws Exception {
        final MultilingualString value = MultilingualString.create("English", "en");
        value.set("cs", "Česky");
        final ObjectNode result = sut.serialize(value, new SerializationContext<>(RDFS.LABEL,
                                                                                  ObjectWithMultilingualString.getLabelField(),
                                                                                  value,
                                                                                  new MappingJsonLdContext()));
        assertEquals(ObjectWithMultilingualString.getLabelField().getName(), result.getName());
        value.getValue().forEach(
                (lang, str) -> assertThat(result.getItems(), hasItem(JsonNodeFactory.createLiteralNode(lang, str))));
    }

    @Test
    void serializeSerializesLanguageLessStringWithJsonLdNone() {
        final MultilingualString value = MultilingualString.create("English", "en");
        value.set("Language-less");
        final ObjectNode result = sut.serialize(value, new SerializationContext<>(value,DummyJsonLdContext.INSTANCE));
        assertThat(result.getItems(), hasItem(JsonNodeFactory.createLiteralNode(JsonLd.NONE, value.get())));
    }
}