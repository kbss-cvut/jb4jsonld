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
package cz.cvut.kbss.jsonld.exception;

/**
 * Thrown when no JSON-LD serializable field matching a property IRI is found in a class.
 */
public class UnknownPropertyException extends JsonLdDeserializationException {

    public UnknownPropertyException(String message) {
        super(message);
    }

    public static UnknownPropertyException create(String property, Class<?> cls) {
        return new UnknownPropertyException(
                "No field matching property " + property + " was found in " + cls + " or its ancestors.");
    }
}
