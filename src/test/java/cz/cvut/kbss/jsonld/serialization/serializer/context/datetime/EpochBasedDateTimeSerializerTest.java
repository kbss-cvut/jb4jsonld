package cz.cvut.kbss.jsonld.serialization.serializer.context.datetime;

import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.TemporalEntity;
import cz.cvut.kbss.jsonld.serialization.context.JsonLdContext;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EpochBasedDateTimeSerializerTest {

    private final EpochBasedDateTimeSerializer sut = new EpochBasedDateTimeSerializer();

    @Test
    void serializeRegistersTermIriInJsonLdContext() throws Exception {
        final JsonLdContext ctx = mock(JsonLdContext.class);
        final OffsetDateTime value = OffsetDateTime.now();
        final Field field = TemporalEntity.class.getDeclaredField("offsetDateTime");
        final SerializationContext<TemporalAccessor> serializationContext =
                new SerializationContext<>(Vocabulary.DATE_CREATED, field, value, ctx);
        sut.serialize(value, serializationContext);
        verify(ctx).registerTermMapping(field.getName(), Vocabulary.DATE_CREATED);
    }
}