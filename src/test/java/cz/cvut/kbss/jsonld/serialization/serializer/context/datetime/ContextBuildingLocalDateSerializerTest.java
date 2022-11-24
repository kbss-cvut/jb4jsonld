package cz.cvut.kbss.jsonld.serialization.serializer.context.datetime;

import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.TemporalEntity;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.context.DummyJsonLdContext;
import cz.cvut.kbss.jsonld.serialization.context.JsonLdContext;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.model.StringLiteralNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ContextBuildingLocalDateSerializerTest {

    private final ContextBuildingLocalDateSerializer sut = new ContextBuildingLocalDateSerializer();

    @Test
    void serializeRegistersTermDefinitionWithIdAndTypeInJsonLdContext() throws Exception {
        final JsonLdContext ctx = mock(JsonLdContext.class);
        final LocalDate value = LocalDate.now();
        final Field field = TemporalEntity.class.getDeclaredField("localDate");
        final SerializationContext<TemporalAccessor> serializationContext =
                new SerializationContext<>(Vocabulary.DATE_CREATED, field, value, ctx);
        sut.serialize(value, serializationContext);
        final ArgumentCaptor<JsonNode> captor = ArgumentCaptor.forClass(JsonNode.class);
        verify(ctx).registerTermMapping(eq(field.getName()), captor.capture());
        assertInstanceOf(ObjectNode.class, captor.getValue());
        assertThat(((ObjectNode) captor.getValue()).getItems(), hasItems(
                JsonNodeFactory.createLiteralNode(JsonLd.ID, Vocabulary.DATE_CREATED),
                JsonNodeFactory.createLiteralNode(JsonLd.TYPE, XSD.DATE)
        ));
    }

    @Test
    void serializeReturnsLiteralNodeWithStringSerialization() throws Exception {
        final LocalDate value = LocalDate.now();
        final Field field = TemporalEntity.class.getDeclaredField("localDate");
        final SerializationContext<TemporalAccessor> serializationContext =
                new SerializationContext<>(Vocabulary.DATE_CREATED, field, value, DummyJsonLdContext.INSTANCE);

        final JsonNode result = sut.serialize(value, serializationContext);
        assertEquals(new StringLiteralNode(field.getName(), DateTimeFormatter.ISO_DATE.format(value)), result);
    }
}