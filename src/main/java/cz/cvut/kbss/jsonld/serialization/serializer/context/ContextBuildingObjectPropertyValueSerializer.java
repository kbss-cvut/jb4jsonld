package cz.cvut.kbss.jsonld.serialization.serializer.context;

import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.SerializerUtils;
import cz.cvut.kbss.jsonld.serialization.serializer.compact.ObjectPropertyValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.ObjectGraphTraverser;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

public class ContextBuildingObjectPropertyValueSerializer extends ObjectPropertyValueSerializer {

    private boolean serializeUsingExtendedDefinition;

    public ContextBuildingObjectPropertyValueSerializer(ObjectGraphTraverser graphTraverser) {
        super(graphTraverser);
    }

    @Override
    public JsonNode serialize(Object value, SerializationContext ctx) {
        if (ctx.getTerm() != null) {
            registerTermDefinition(ctx);
        }
        return super.serialize(value, ctx);
    }

    private void registerTermDefinition(SerializationContext<?> ctx) {
        if (serializeUsingExtendedDefinition) {
            ctx.registerTermMapping(ctx.getFieldName(),
                                    SerializerUtils.createTypedTermDefinition(ctx.getFieldName(), ctx.getTerm(),
                                                                              JsonLd.ID));
        } else {
            ctx.registerTermMapping(ctx.getFieldName(), ctx.getTerm());
        }
    }

    @Override
    public void configure(Configuration config) {
        this.serializeUsingExtendedDefinition = config.is(ConfigParam.SERIALIZE_INDIVIDUALS_USING_EXPANDED_DEFINITION);
    }
}
