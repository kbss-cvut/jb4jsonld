package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.JsonLd;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

class LangStringNodeTest extends AbstractNodeTest {

    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    void writeValueWritesValueAndLanguageAsStrings() throws Exception {
        final String value = "test";
        final String language = "en";
        final LangStringNode sut = new LangStringNode(value, language);
        sut.write(serializerMock);
        final InOrder inOrder = Mockito.inOrder(serializerMock);
        inOrder.verify(serializerMock).writeObjectStart();
        inOrder.verify(serializerMock).writeFieldName(JsonLd.LANGUAGE);
        inOrder.verify(serializerMock).writeString(language);
        inOrder.verify(serializerMock).writeFieldName(JsonLd.VALUE);
        inOrder.verify(serializerMock).writeString(value);
        inOrder.verify(serializerMock).writeObjectEnd();
    }

    @Test
    void writeValueWritesValueAndNullWhenLanguageTagIsNotSpecified() throws Exception {
        final String value = "test";
        final LangStringNode sut = new LangStringNode(value, null);
        sut.write(serializerMock);
        final InOrder inOrder = Mockito.inOrder(serializerMock);
        inOrder.verify(serializerMock).writeObjectStart();
        inOrder.verify(serializerMock).writeFieldName(JsonLd.LANGUAGE);
        inOrder.verify(serializerMock).writeNull();
        inOrder.verify(serializerMock).writeFieldName(JsonLd.VALUE);
        inOrder.verify(serializerMock).writeString(value);
        inOrder.verify(serializerMock).writeObjectEnd();
    }
}
