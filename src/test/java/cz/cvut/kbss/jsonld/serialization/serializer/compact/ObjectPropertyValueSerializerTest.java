package cz.cvut.kbss.jsonld.serialization.serializer.compact;

import cz.cvut.kbss.jopa.model.annotations.Individual;
import cz.cvut.kbss.jopa.vocabulary.OWL;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.OwlPropertyType;
import cz.cvut.kbss.jsonld.exception.InvalidEnumMappingException;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.context.DummyJsonLdContext;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.model.ObjectNode;
import cz.cvut.kbss.jsonld.serialization.traversal.ObjectGraphTraverser;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ObjectPropertyValueSerializerTest {

    @Mock
    private ObjectGraphTraverser graphTraverser;

    @InjectMocks
    private ObjectPropertyValueSerializer sut;

    @Test
    void serializeInvokesGraphTraverserForSpecifiedObjectByDefault() {
        final Employee instance = Generator.generateEmployee();
        final SerializationContext<Employee> ctx = new SerializationContext<>(Vocabulary.HAS_MEMBER, instance,
                                                                              DummyJsonLdContext.INSTANCE);
        final JsonNode result = sut.serialize(instance, ctx);
        assertNull(result);
        verify(graphTraverser).traverse(ctx);
    }

    @Test
    void serializeReturnsObjectNodeWithIdForSpecifiedEnumConstantMappedToIndividual() {
        final OwlPropertyType instance = OwlPropertyType.OBJECT_PROPERTY;
        final SerializationContext<OwlPropertyType> ctx =
                new SerializationContext<>(Vocabulary.HAS_PROPERTY_TYPE, instance, DummyJsonLdContext.INSTANCE);

        final JsonNode result = sut.serialize(instance, ctx);
        assertInstanceOf(ObjectNode.class, result);
        final ObjectNode objectNode = (ObjectNode) result;
        assertEquals(1, objectNode.getItems().size());
        assertThat(objectNode.getItems(), hasItem(JsonNodeFactory.createObjectIdNode(JsonLd.ID, OWL.OBJECT_PROPERTY)));
    }

    @Test
    void serializeThrowsInvalidEnumMappingExceptionForEnumConstantWithoutMatchingIndividualMapping() {
        final InvalidEnum instance = InvalidEnum.OBJECT_PROPERTY;
        final SerializationContext<InvalidEnum> ctx =
                new SerializationContext<>(Vocabulary.HAS_PROPERTY_TYPE, instance, DummyJsonLdContext.INSTANCE);

        assertThrows(InvalidEnumMappingException.class, () -> sut.serialize(instance, ctx));
    }

    private enum InvalidEnum {
        @Individual(iri = OWL.DATATYPE_PROPERTY)
        DATATYPE_PROPERTY,
        // Missing individual mapping here
        OBJECT_PROPERTY
    }
}