package cz.cvut.kbss.jsonld.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be used to define ordering (possibly partial) to use when serializing object attributes.
 * <p>
 * Attributes included in annotation declaration will be serialized first (in defined order), followed by any attributes
 * not included in the definition.
 * <p>
 * Note that this annotation expects the values to be names of the Java attributes, NOT their JSON-LD counterparts (IRIs).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonLdAttributeOrder {

    /**
     * Order in which properties of annotated object are to be handled.
     */
    String[] value() default {};
}
