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
 * Thrown when polymorphic deserialization encounters a JSON-LD object which can be deserialized as multiple target Java
 * classes and we are unable to unambiguously decide which to use.
 */
public class AmbiguousTargetTypeException extends TargetTypeException {

    public AmbiguousTargetTypeException(String message) {
        super(message);
    }
}
