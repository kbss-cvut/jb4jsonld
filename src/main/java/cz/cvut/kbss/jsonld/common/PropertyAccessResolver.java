package cz.cvut.kbss.jsonld.common;

import java.lang.reflect.Field;

/**
 * Resolves property access configuration for instance fields.
 */
public interface PropertyAccessResolver {

    /**
     * Resolves whether value of the specified field should be serialized.
     *
     * @param field Field to check
     * @return Whether the field should be serialized
     */
    boolean shouldSerialize(Field field);

    /**
     * Resolves whether value of the specified field should be deserialized.
     *
     * @param field Field to check
     * @return Whether the field should be deserialized
     */
    boolean shouldDeserialize(Field field);
}
