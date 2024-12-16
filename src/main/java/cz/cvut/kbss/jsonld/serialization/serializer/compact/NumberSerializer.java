package cz.cvut.kbss.jsonld.serialization.serializer.compact;

import cz.cvut.kbss.jopa.vocabulary.XSD;
import cz.cvut.kbss.jsonld.serialization.model.JsonNode;
import cz.cvut.kbss.jsonld.serialization.serializer.SerializerUtils;
import cz.cvut.kbss.jsonld.serialization.serializer.ValueSerializer;
import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * Serializes numeric values.
 */
public class NumberSerializer implements ValueSerializer<Number> {

    @Override
    public JsonNode serialize(Number value, SerializationContext<Number> ctx) {
        Objects.requireNonNull(value);
        if (value instanceof Integer) {
            return SerializerUtils.createdTypedValueNode(ctx.getTerm(), value, XSD.INT);
        } else if (value instanceof Long) {
            return SerializerUtils.createdTypedValueNode(ctx.getTerm(), value, XSD.LONG);
        } else if (value instanceof Double) {
            return SerializerUtils.createdTypedValueNode(ctx.getTerm(), value, XSD.DOUBLE);
        } else if (value instanceof Float) {
            return SerializerUtils.createdTypedValueNode(ctx.getTerm(), value, XSD.FLOAT);
        } else if (value instanceof Short) {
            return SerializerUtils.createdTypedValueNode(ctx.getTerm(), value, XSD.SHORT);
        } else if (value instanceof BigInteger) {
            return SerializerUtils.createdTypedValueNode(ctx.getTerm(), value, XSD.INTEGER);
        } else if (value instanceof BigDecimal) {
            return SerializerUtils.createdTypedValueNode(ctx.getTerm(), value, XSD.DECIMAL);
        } else {
            throw new IllegalArgumentException("Unsupported numeric literal type " + value.getClass());
        }
    }

    /**
     * Gets a list of Java types supported by this serializer.
     *
     * @return List of Java classes
     */
    public static List<Class<? extends Number>> getSupportedTypes() {
        return List.of(Integer.class, Long.class, Double.class, Float.class, Short.class, BigInteger.class,
                       BigDecimal.class);
    }
}
