/**
 * Copyright (C) 2022 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jsonld.deserialization.util;

import cz.cvut.kbss.jopa.datatype.xsd.XsdDatatypeMapper;
import cz.cvut.kbss.ontodriver.model.Literal;

public class XSDTypeCoercer {

    public static Object coerceType(String value, String type) {
        final Literal literal = new Literal(value, type);
        return XsdDatatypeMapper.getInstance().map(literal)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported type for XSD type coercion: " + type));
    }
}
