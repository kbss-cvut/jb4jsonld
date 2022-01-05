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
package cz.cvut.kbss.jsonld.serialization;

import cz.cvut.kbss.jsonld.exception.JsonLdSerializationException;
import cz.cvut.kbss.jsonld.serialization.model.LiteralNode;
import cz.cvut.kbss.jsonld.serialization.model.NumericLiteralNode;

import java.util.Date;

class TemporalNodeFactory {

    private TemporalNodeFactory() {
        throw new AssertionError();
    }

    static LiteralNode<Long> createLiteralNode(String name, Object value) {
        assert value != null;
        if (value instanceof Date) {
            final Date date = (Date) value;
            return name != null ? new NumericLiteralNode<>(name, date.getTime()) :
                   new NumericLiteralNode<>(date.getTime());
        } else {
            throw new JsonLdSerializationException(
                    "Unsupported temporal type " + value.getClass() + " of value " + value);
        }
    }
}
