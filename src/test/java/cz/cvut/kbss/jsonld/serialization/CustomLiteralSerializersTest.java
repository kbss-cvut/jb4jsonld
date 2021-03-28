package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Study;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomLiteralSerializersTest {

    private ValueSerializers serializers;

    private JsonLdTreeBuilder treeBuilder;

    @BeforeEach
    void setUp() {
        this.serializers = new CommonValueSerializers();
        this.treeBuilder = new JsonLdTreeBuilder(serializers);
    }

    @Test
    void visitAttributeUsesConfiguredCustomSerializerWhenTypeMatches() throws Exception {
        final ValueSerializer<Integer> intSerializer = mock(ValueSerializer.class);
        serializers.registerSerializer(Integer.class, intSerializer);
        final Study study = new Study();
        study.setUri(Generator.generateUri());
        study.setNoOfPeopleInvolved(10);
        final JsonNode serialized = JsonNodeFactory.createLiteralNode(Vocabulary.NUMBER_OF_PEOPLE_INVOLVED, (long) study.getNoOfPeopleInvolved());
        when(intSerializer.serialize(any(), any())).thenReturn(serialized);
        treeBuilder.openObject(new SerializationContext<>(study));
        final SerializationContext<Integer> ctx = new SerializationContext<>(Vocabulary.NUMBER_OF_PEOPLE_INVOLVED, Study.class.getDeclaredField("noOfPeopleInvolved"), study.getNoOfPeopleInvolved());
        treeBuilder.visitAttribute(ctx);
        verify(intSerializer).serialize(study.getNoOfPeopleInvolved(), ctx);
        final JsonNode node = JsonLdTreeBuilderTest.getNode(treeBuilder.getTreeRoot(), Vocabulary.NUMBER_OF_PEOPLE_INVOLVED);
        assertNotNull(node);
        assertSame(serialized, node);
    }
}
