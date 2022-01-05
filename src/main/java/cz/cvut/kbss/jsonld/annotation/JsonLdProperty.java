/**
 * Copyright (C) 2022 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
     *
     * @return Type of access to annotated property
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
