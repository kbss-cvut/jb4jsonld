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
