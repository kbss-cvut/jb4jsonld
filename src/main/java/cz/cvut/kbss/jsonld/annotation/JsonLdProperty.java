package cz.cvut.kbss.jsonld.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows to configure serialization and deserialization behavior for an attribute annotated by this annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonLdProperty {

    /**
     * Allows to configure serialization and deserialization access to the property.
     * <p>
     * By default, the property is both serialized and deserialized. This can be restricted by making the property
     * read-only or write-only.
     */
    Access access() default Access.READ_WRITE;

    /**
     * Specifies property access options.
     */
    enum Access {
        /**
         * The property can be written to (deserialization) and read from (serialization).
         */
        READ_WRITE,
        /**
         * The property can be only read from (serialization).
         */
        READ_ONLY,
        /**
         * The property can be only written to (deserialization).
         */
        WRITE_ONLY
    }
}
