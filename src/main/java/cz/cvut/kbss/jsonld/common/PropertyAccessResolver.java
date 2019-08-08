package cz.cvut.kbss.jsonld.common;

import java.lang.reflect.Field;

/**
 * Resolves property access configuration for instance fields.
 */
public interface PropertyAccessResolver {

    /**
     * Resolves whether value of the specified field is readable for serialization.
     *
     * @param field Field to check
     * @return Whether the field is readable
     */
    boolean isReadable(Field field);

    /**
     * Resolves whether the specified field is writeable by deserialization.
     *
     * @param field Field to check
     * @return Whether the field is writeable
     */
    boolean isWriteable(Field field);
}
