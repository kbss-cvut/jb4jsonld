/*
 * JB4JSON-LD
 * Copyright (C) 2023 Czech Technical University in Prague
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
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
     *
     * @return Array of Java attribute names in order in which they should be processed
     */
    String[] value() default {};
}
