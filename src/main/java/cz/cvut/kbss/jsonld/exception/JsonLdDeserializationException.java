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
package cz.cvut.kbss.jsonld.exception;

import java.lang.reflect.Field;

/**
 * Thrown when an error occurs during deserialization of JSON-LD into POJO(s).
 */
public class JsonLdDeserializationException extends JsonLdException {

    public JsonLdDeserializationException(String message) {
        super(message);
    }

    public JsonLdDeserializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public static JsonLdDeserializationException singularAttributeCardinalityViolated(String property, Field field) {
        return new JsonLdDeserializationException("Encountered multiple values of property " + property +
                ", which is mapped to a singular attribute " + field);
    }
}
