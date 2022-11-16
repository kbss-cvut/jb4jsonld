package cz.cvut.kbss.jsonld.serialization.context;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.ObjectWithMultilingualString;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ContextMappingSerializationContextFactoryTest {

    @Mock
    private JsonLdContext jsonLdContext;

    @InjectMocks
    private ContextMappingSerializationContextFactory sut;

    @Test
    void createForAttributeMapsFieldNameToPropertyInJsonLdContextAndReturnsSerializationContextWithFieldName() throws Exception {
        final SerializationContext<String> ctx = sut.createForAttribute(Person.getFirstNameField(), "Test");
        assertEquals(Person.getFirstNameField().getName(), ctx.getAttributeId());
        verify(jsonLdContext).registerTermMapping(Person.getFirstNameField().getName(), Vocabulary.FIRST_NAME);
    }

    // TODO This would require also changing the way multilingual strings are serialized into nodes in MultilingualStringSerializer
    // I.e., instead of objects with @value and @language, a single object with language-keyed map should be used
    @Disabled
    @Test
    void createForAttributeRegistersLanguageContainerInJsonLdContextForMultilingualStringField() throws Exception {
        final SerializationContext<MultilingualString> ctx = sut.createForAttribute(ObjectWithMultilingualString.getLabelField(), MultilingualString.create("Test", "en"));
        assertEquals(ObjectWithMultilingualString.getLabelField().getName(), ctx.getAttributeId());
        final ObjectNode langContainer = JsonNodeFactory.createObjectNode();
        langContainer.addItem(JsonNodeFactory.createLiteralNode(JsonLd.ID, RDFS.LABEL));
        langContainer.addItem(JsonNodeFactory.createLiteralNode(JsonLd.CONTAINER, JsonLd.LANGUAGE));
        verify(jsonLdContext).registerTermMapping(ObjectWithMultilingualString.getLabelField().getName(), langContainer);
    }
}