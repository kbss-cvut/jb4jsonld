/*
 * JB4JSON-LD
 * Copyright (C) 2025 Czech Technical University in Prague
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
     * @param objectClass Type of the object being serialized
     * @return Whether the field is readable
     */
    boolean isReadable(Field field, Class<?> objectClass);

    /**
     * Resolves whether the specified field is writeable by deserialization.
     *
     * @param field Field to check
     * @return Whether the field is writeable
     */
    boolean isWriteable(Field field);
}
