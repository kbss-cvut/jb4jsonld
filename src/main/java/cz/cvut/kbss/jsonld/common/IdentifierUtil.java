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

import java.util.Objects;
import java.util.Random;

/**
 * Allows to generate blank nodes for identifier-less instances.
 * <p>
 * Although objects in JSON-LD are not required to have id, some tools may have issues processing such data. In addition,
 * multiple references to the same instance cannot be created when serializing the JSON-LD, as there is no identifier to
 * reference.
 */
public class IdentifierUtil {

    /**
     * Prefix of a blank node identifier.
     */
    public static final String B_NODE_PREFIX = "_:";

    private static final Random RANDOM = new Random();

    /**
     * Generates a (pseudo)random blank node identifier.
     *
     * @return Blank node identifier
     */
    public static String generateBlankNodeId() {
        return B_NODE_PREFIX + RANDOM.nextInt(Integer.MAX_VALUE);
    }

    /**
     * Checks whether the specified value is a <i>compact IRI</i>, as defined by the JSON-LD specification
     * <a href="https://w3c.github.io/json-ld-syntax/#compact-iris">par. 4.1.5</a>.
     *
     * @param value The value to examine
     * @return {@code true} if the specified value is a compact IRI, {@code false} otherwise
     */
    public static boolean isCompactIri(String value) {
        Objects.requireNonNull(value);
        final int colonIndex = value.indexOf(':');
        if (colonIndex >= 0) {
            final String prefixWithColon = value.substring(0, colonIndex + 1);
            final String suffix = value.substring(colonIndex + 1);
            return !B_NODE_PREFIX.equals(prefixWithColon) && !suffix.startsWith("//");
        }
        return false;
    }
}
