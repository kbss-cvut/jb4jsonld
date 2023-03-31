package cz.cvut.kbss.jsonld.serialization.serializer.context;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.Organization;
import cz.cvut.kbss.jsonld.serialization.context.JsonLdContext;
import cz.cvut.kbss.jsonld.serialization.serializer.SerializerUtils;
import cz.cvut.kbss.jsonld.serialization.traversal.ObjectGraphTraverser;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ContextBuildingObjectPropertyValueSerializerTest {

    @Mock
    private ObjectGraphTraverser objectGraphTraverser;

    @InjectMocks
    private ContextBuildingObjectPropertyValueSerializer sut;

    @Test
    void serializeRegistersTermIriMappingInJsonLdContext() throws Exception {
        final JsonLdContext ctx = mock(JsonLdContext.class);
        final Organization value = Generator.generateOrganization();
        final SerializationContext<Organization> serializationCtx =
                new SerializationContext<>(Vocabulary.IS_MEMBER_OF, Employee.getEmployerField(), value, ctx);

        sut.serialize(value, serializationCtx);
        verify(ctx).registerTermMapping(Employee.getEmployerField().getName(), Vocabulary.IS_MEMBER_OF);
    }

    @Test
    void serializeRegistersExtendedTermDefinitionWithIdAndTypeInJsonLdContextWhenConfiguredToUseExtendedDefinition() throws Exception {
        final Configuration config = new Configuration();
        config.set(ConfigParam.SERIALIZE_INDIVIDUALS_USING_EXPANDED_DEFINITION, Boolean.TRUE.toString());
        sut.configure(config);
        final JsonLdContext ctx = mock(JsonLdContext.class);
        final Organization value = Generator.generateOrganization();
        final SerializationContext<Organization> serializationCtx =
                new SerializationContext<>(Vocabulary.IS_MEMBER_OF, Employee.getEmployerField(), value, ctx);

        sut.serialize(value, serializationCtx);
        verify(ctx).registerTermMapping(Employee.getEmployerField().getName(),
                                        SerializerUtils.createTypedTermDefinition(Employee.getEmployerField().getName(),
                                                                                  Vocabulary.IS_MEMBER_OF,
                                                                                  JsonLd.ID));
    }
}