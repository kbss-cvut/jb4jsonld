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
package cz.cvut.kbss.jsonld.exception;

/**
 * Indicates that the serializer encountered an instance without an identifier and it was configured to require identifier presence.
 *
 * @see cz.cvut.kbss.jsonld.ConfigParam#REQUIRE_ID
 */
public class MissingIdentifierException extends JsonLdSerializationException {

    public MissingIdentifierException(String message) {
        super(message);
    }

    public static MissingIdentifierException create(Object instance) {
        return new MissingIdentifierException(
                "Instance " + instance + " is missing an identifier. Either it has no @Id field or its value is null.");
    }
}
