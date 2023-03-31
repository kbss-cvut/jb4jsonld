package cz.cvut.kbss.jsonld.serialization.serializer.compact;

import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.serialization.context.DummyJsonLdContext;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.traversal.ObjectGraphTraverser;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ObjectPropertyValueSerializerTest {

    @Mock
    private ObjectGraphTraverser graphTraverser;

    @InjectMocks
    private ObjectPropertyValueSerializer sut;

    @Test
    void serializeInvokesGraphTraverserForSpecifiedObject() {
        final Employee instance = Generator.generateEmployee();
        final SerializationContext<Employee> ctx = new SerializationContext<>(Vocabulary.HAS_MEMBER, instance,
                                                                              DummyJsonLdContext.INSTANCE);
        final JsonNode result = sut.serialize(instance, ctx);
        assertNull(result);
        verify(graphTraverser).traverse(ctx);
    }
}