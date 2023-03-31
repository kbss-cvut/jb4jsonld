package cz.cvut.kbss.jsonld.serialization.serializer.context;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.common.BeanClassProcessor;
import cz.cvut.kbss.jsonld.common.EnumUtil;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.IndividualSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

public class ContextBuildingIndividualSerializer extends IndividualSerializer {

    private boolean serializeUsingExtendedDefinition;

    @Override
    public JsonNode serialize(Object value, SerializationContext ctx) {
        if (serializeUsingExtendedDefinition) {
            if (BeanClassProcessor.isIdentifierType(value.getClass())) {
                return JsonNodeFactory.createStringLiteralNode(ctx.getTerm(), value.toString());
            } else {
                return JsonNodeFactory.createStringLiteralNode(ctx.getTerm(),
                                                               EnumUtil.resolveMappedIndividual((Enum<?>) value));
            }
        }
        return super.serialize(value, ctx);
    }

    @Override
    public void configure(Configuration config) {
        this.serializeUsingExtendedDefinition = config.is(ConfigParam.SERIALIZE_INDIVIDUALS_USING_EXPANDED_DEFINITION);
    }
}
