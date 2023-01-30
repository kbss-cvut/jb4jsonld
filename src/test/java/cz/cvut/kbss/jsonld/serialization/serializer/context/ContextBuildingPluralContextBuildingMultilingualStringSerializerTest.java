package cz.cvut.kbss.jsonld.serialization.serializer.context;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jopa.vocabulary.SKOS;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.model.ObjectWithPluralMultilingualString;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.context.JsonLdContext;
import cz.cvut.kbss.jsonld.serialization.context.MappingJsonLdContext;
import cz.cvut.kbss.jsonld.serialization.model.CollectionNode;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.LiteralNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ContextBuildingPluralContextBuildingMultilingualStringSerializerTest {

    private final ContextBuildingPluralMultilingualStringSerializer
            sut = new ContextBuildingPluralMultilingualStringSerializer();

    @Test
    void serializeRegistersTermMappingWithLanguageTypeInJsonLdContext() throws Exception {
        final Set<MultilingualString> value = new HashSet<>(Collections.singletonList(
                MultilingualString.create("test", "en")
        ));
        final JsonLdContext jsonLdCtx = mock(JsonLdContext.class);
        sut.serialize(value,
                      new SerializationContext<>(SKOS.ALT_LABEL, ObjectWithPluralMultilingualString.getAltLabelField(),
                                                 value, jsonLdCtx));
        final ArgumentCaptor<JsonNode> captor = ArgumentCaptor.forClass(JsonNode.class);
        verify(jsonLdCtx).registerTermMapping(eq(ObjectWithPluralMultilingualString.getAltLabelField().getName()),
                                              captor.capture());
        assertInstanceOf(ObjectNode.class, captor.getValue());
        assertThat(((ObjectNode) captor.getValue()).getItems(), hasItems(
                JsonNodeFactory.createLiteralNode(JsonLd.ID, SKOS.ALT_LABEL),
                JsonNodeFactory.createLiteralNode(JsonLd.CONTAINER, JsonLd.LANGUAGE)
        ));
    }

    @Test
    void serializeConsolidatesCollectionOfMultilingualStringsIntoMapWithLanguageAsKeysAndArrayAsValues() throws Exception {
        final MultilingualString mlsOne = MultilingualString.create("English one", "en");
        mlsOne.set("cs", "Česky");
        final MultilingualString mlsTwo = MultilingualString.create("English two", "en");
        final Set<MultilingualString> value = new HashSet<>(Arrays.asList(mlsOne, mlsTwo));
        final ObjectNode result = sut.serialize(value, new SerializationContext<>(SKOS.ALT_LABEL,
                                                                                  ObjectWithPluralMultilingualString.getAltLabelField(),
                                                                                  value, new MappingJsonLdContext()));
        assertThat(result.getItems(), hasItem(JsonNodeFactory.createLiteralNode("cs", mlsOne.get("cs"))));
        final Optional<JsonNode> enItems = result.getItems().stream().filter(n -> n.getName().equals("en")).findAny();
        assertTrue(enItems.isPresent());
        assertInstanceOf(CollectionNode.class, enItems.get());
        assertThat(((CollectionNode<?>) enItems.get()).getItems(),
                   hasItems(JsonNodeFactory.createLiteralNode(mlsOne.get("en")),
                            JsonNodeFactory.createLiteralNode(mlsTwo.get("en"))));
    }

    @Test
    void serializeSerializesLanguageLessStringWithJsonLdNone() throws Exception {
        final MultilingualString mlsOne = MultilingualString.create("English one", "en");
        mlsOne.set("Language-less");
        final Set<MultilingualString> value = new HashSet<>(Collections.singletonList(mlsOne));
        final ObjectNode result = sut.serialize(value, new SerializationContext<>(SKOS.ALT_LABEL,
                                                                                  ObjectWithPluralMultilingualString.getAltLabelField(),
                                                                                  value, new MappingJsonLdContext()));
        final Optional<JsonNode> noLangNode =
                result.getItems().stream().filter(n -> n.getName().equals(JsonLd.NONE)).findAny();
        assertTrue(noLangNode.isPresent());
        assertEquals(mlsOne.get(), ((LiteralNode<?>) noLangNode.get()).getValue());
    }
}