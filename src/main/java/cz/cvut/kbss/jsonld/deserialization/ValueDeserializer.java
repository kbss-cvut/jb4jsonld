package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.Configuration;
import cz.cvut.kbss.jsonld.common.Configurable;
import jakarta.json.JsonValue;

/**
 * Deserializes JSON-LD nodes to Java objects.
 *
 * @param <T> Target type
 */
public interface ValueDeserializer<T> extends Configurable {

    /**
     * Deserializes the specified JSON-LD node.
     *
     * @param jsonNode JSON-LD node to deserialize
     * @param ctx      Deserialization context
     * @return Deserialized object
     */
    T deserialize(JsonValue jsonNode, DeserializationContext<T> ctx);

    /**
     * Applies the specified configuration to this deserializer.
     * <p>
     * Should be called at the beginning of deserialization of JSON-LD content so that potential runtime changes in configuration
     * can be reflected by the deserialization process.
     * <p>
     * Implementations are free to apply configuration on initialization and rely on the default implementation of this
     * method which does nothing.
     *
     * @param config Configuration to apply
     */
    @Override
    default void configure(Configuration config) {
    }
}
