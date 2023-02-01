package cz.cvut.kbss.jsonld.serialization.serializer.context.datetime;

import cz.cvut.kbss.jopa.datatype.DateTimeUtil;
import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
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
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ContextBuildingTimeSerializerTest {

    private final ContextBuildingTimeSerializer sut = new ContextBuildingTimeSerializer();

    @Test
    void serializeRegistersTermDefinitionWithIdAndTypeInJsonLdContext() throws Exception {
        final JsonLdContext ctx = mock(JsonLdContext.class);
        final OffsetTime value = OffsetTime.now();
        final Field field = TemporalEntity.class.getDeclaredField("offsetTime");
        final String property = Generator.generateUri().toString();
        final SerializationContext<TemporalAccessor> serializationContext =
                new SerializationContext<>(property, field, value, ctx);
        sut.serialize(value, serializationContext);
        final ArgumentCaptor<ObjectNode> captor = ArgumentCaptor.forClass(ObjectNode.class);
        verify(ctx).registerTermMapping(eq(field.getName()), captor.capture());
        assertThat(captor.getValue().getItems(), hasItems(
                JsonNodeFactory.createLiteralNode(JsonLd.ID, property),
                JsonNodeFactory.createLiteralNode(JsonLd.TYPE, XSD.TIME)
        ));
    }

    @Test
    void serializeReturnsLiteralNodeWithStringSerialization() throws Exception {
        final LocalTime value = LocalTime.now();
        final Field field = TemporalEntity.class.getDeclaredField("localTime");
        final String property = Generator.generateUri().toString();
        final SerializationContext<TemporalAccessor> serializationContext =
                new SerializationContext<>(property, field, value, DummyJsonLdContext.INSTANCE);

        final JsonNode result = sut.serialize(value, serializationContext);
        assertEquals(new StringLiteralNode(field.getName(), DateTimeFormatter.ISO_TIME.format(value.atOffset(
                DateTimeUtil.SYSTEM_OFFSET))), result);
    }
}