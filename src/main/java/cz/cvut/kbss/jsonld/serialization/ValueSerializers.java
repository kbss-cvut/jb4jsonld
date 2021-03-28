package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.serialization.traversal.SerializationContext;

import java.util.Optional;

/**
 * Provides serializers for JSON-LD tree building.
 */
public interface ValueSerializers {

    /**
     * Gets a custom serializer for the specified serialization context.
     *
     * @param ctx Context representing the value to serialize
     * @param <T> Type of the value
     * @return Optional containing the custom serializer registered for the specified value or an empty optional if there is no custom
     * serializer registered
     * @see #getOrDefault(SerializationContext)
     */
    <T> Optional<ValueSerializer<T>> getSerializer(SerializationContext<T> ctx);

    /**
     * Gets a custom serializer registered for the specified serialization context or the default serializer ({@link
     * DefaultValueSerializer}) if there is no custom one.
     *
     * @param ctx Context representing the value to serialize
     * @param <T> Type of the value
     * @return Value serializer for the specified context
     * @see #getSerializer(SerializationContext)
     */
    <T> ValueSerializer<T> getOrDefault(SerializationContext<T> ctx);

    /**
     * Registers the specified serializer for the specified type.
     *
     * @param forType    Type to be serialized using the specified serializer
     * @param serializer Serializer to register
     * @param <T>        Value type
     */
    <T> void registerSerializer(Class<T> forType, ValueSerializer<T> serializer);
}
