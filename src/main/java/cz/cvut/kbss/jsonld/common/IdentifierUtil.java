/**
 * Copyright (C) 2020 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.common;

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
}
