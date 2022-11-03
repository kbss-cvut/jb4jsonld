package cz.cvut.kbss.jsonld.serialization.context;

import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Person;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
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
    void createWithFieldAndValueMapsFieldNameToPropertyInJsonLdContextAndReturnsSerializationContextWithFieldName() throws Exception {
        final SerializationContext<String> ctx = sut.createWithAttributeId(Person.getFirstNameField(), "Test");
        assertEquals(Person.getFirstNameField().getName(), ctx.getAttributeId());
        verify(jsonLdContext).registerTermMapping(Person.getFirstNameField().getName(), Vocabulary.FIRST_NAME);
    }
}