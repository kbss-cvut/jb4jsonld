package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.serialization.*;
import cz.cvut.kbss.jsonld.serialization.traversal.ObjectGraphTraverser;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ObjectGraphValueSerializersTest {

    @Mock
    private ObjectGraphTraverser traverser;

    private ObjectGraphValueSerializers sut;

    @BeforeEach
    void setUp() {
        this.sut = new ObjectGraphValueSerializers(new CommonValueSerializers(), traverser);
    }

    @Test
    void getSerializerReturnsObjectPropertySerializerWhenProvidedFieldIsObjectProperty() throws Exception {
        final SerializationContext<Organization> ctx = new SerializationContext<>(Vocabulary.IS_MEMBER_OF, Employee.getEmployerField(), Generator.generateOrganization());
        final Optional<ValueSerializer<Organization>> result = sut.getSerializer(ctx);
        assertTrue(result.isPresent());
        assertThat(result.get(), instanceOf(ObjectPropertyValueSerializer.class));
    }

    @Test
    void getSerializerReturnsEmptyOptionalWhenFieldIsNotObjectPropertyAndNoCustomSerializerIsRegisteredForIt() throws Exception {
        final SerializationContext<Date> ctx = new SerializationContext<>(Vocabulary.DATE_CREATED, Organization.class.getDeclaredField("dateCreated"), new Date());
        final Optional<ValueSerializer<Date>> result = sut.getSerializer(ctx);
        assertFalse(result.isPresent());
    }

    @Test
    void getSerializerReturnsCustomSerializerWhenItIsRegisteredInCommon() throws Exception {
        final ValueSerializer<Organization> serializer = ((value, ctx) -> JsonNodeFactory.createObjectIdNode(Generator.generateUri()));
        sut.registerSerializer(Organization.class, serializer);
        final SerializationContext<Organization> ctx = new SerializationContext<>(Vocabulary.IS_MEMBER_OF, Employee.getEmployerField(), Generator.generateOrganization());
        final Optional<ValueSerializer<Organization>> result = sut.getSerializer(ctx);
        assertTrue(result.isPresent());
        assertEquals(serializer, result.get());
    }

    @Test
    void getOrDefaultReturnsObjectPropertySerializerWhenProvidedFieldIsObjectProperty() throws Exception {
        final SerializationContext<Organization> ctx = new SerializationContext<>(Vocabulary.IS_MEMBER_OF, Employee.getEmployerField(), Generator.generateOrganization());
        final ValueSerializer<Organization> result = sut.getOrDefault(ctx);
        assertThat(result, instanceOf(ObjectPropertyValueSerializer.class));
    }

    @Test
    void getOrDefaultReturnsCustomSerializerWhenItIsRegistered() throws Exception {
        final ValueSerializer<Organization> serializer = ((value, ctx) -> JsonNodeFactory.createObjectIdNode(Generator.generateUri()));
        sut.registerSerializer(Organization.class, serializer);
        final SerializationContext<Organization> ctx = new SerializationContext<>(Vocabulary.IS_MEMBER_OF, Employee.getEmployerField(), Generator.generateOrganization());
        final ValueSerializer<Organization> result = sut.getOrDefault(ctx);
        assertEquals(serializer, result);
    }

    @Test
    void getOrDefaultReturnsDefaultSerializerWhenFieldIsNotObjectPropertyAndNoCustomSerializerIsRegisteredForIt() throws Exception {
        final SerializationContext<Date> ctx = new SerializationContext<>(Vocabulary.DATE_CREATED, Organization.class.getDeclaredField("dateCreated"), new Date());
        final ValueSerializer<Date> result = sut.getOrDefault(ctx);
        assertThat(result, instanceOf(DefaultValueSerializer.class));
    }
}