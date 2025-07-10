package cz.cvut.kbss.jsonld.serialization.serializer.compact;

import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.SerializerUtils;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.util.Objects;

public class BooleanSerializer implements ValueSerializer<Boolean> {

    @Override
    public JsonNode serialize(Boolean value, SerializationContext<Boolean> ctx) {
        Objects.requireNonNull(value);
        return SerializerUtils.createdTypedValueNode(ctx.getTerm(), value, XSD.BOOLEAN);
    }
}
