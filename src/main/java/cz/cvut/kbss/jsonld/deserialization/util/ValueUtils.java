package cz.cvut.kbss.jsonld.deserialization.util;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import jakarta.json.JsonNumber;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class ValueUtils {

    private ValueUtils() {
        throw new AssertionError();
    }

    /**
     * Extracts the value of the {@link JsonLd#VALUE} attribute from the specified JSON object.
     *
     * @param jsonNode JSON object from which to extract the value
     * @return Extracted value
     * @throws JsonLdDeserializationException If the specified node is not a JSON object or if it does not contain a
     *                                        {@code @value} attribute
     */
    public static JsonValue getValue(JsonValue jsonNode) {
        if (jsonNode.getValueType() != JsonValue.ValueType.OBJECT || !jsonNode.asJsonObject()
                                                                              .containsKey(JsonLd.VALUE)) {
            throw new JsonLdDeserializationException("Cannot deserialize node " + jsonNode + "as literal. " +
                                                             "It is missing attribute '" + JsonLd.VALUE + "'.");
        }
        return jsonNode.asJsonObject().get(JsonLd.VALUE);
    }

    /**
     * Returns a string representation of the specified JSON value.
     *
     * @param value Value to stringify
     * @return String value
     */
    public static String stringValue(JsonValue value) {
        return value instanceof JsonString ? ((JsonString) value).getString() : value.toString();
    }

    public static Object literalValue(JsonValue value) {
        switch(value.getValueType()) {
            case STRING:
                return ((JsonString) value).getString();
            case NUMBER:
                return ((JsonNumber) value).numberValue();
            case TRUE:
                return true;
            case FALSE:
                return false;
            case NULL:
                return null;
            default:
                throw new IllegalArgumentException("Value " + value + " is not a literal.");
        }
    }
}
