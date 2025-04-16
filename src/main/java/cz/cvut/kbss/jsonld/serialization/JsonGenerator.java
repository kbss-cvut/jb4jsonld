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
package cz.cvut.kbss.jsonld.serialization;

import java.io.IOException;

/**
 * Represents interface to the underlying JAXB implementation, which handles the actual value serialization.
 */
public interface JsonGenerator {

    /**
     * Writes a field name (JSON string surrounded by double quotes).
     * <p>
     * Can be used only in an object, when a field name is expected.
     *
     * @param name Field name to write
     * @throws IOException When JSON writing error occurs
     */
    void writeFieldName(String name) throws IOException;

    /**
     * Writes a starting marker of a JSON object value (the '{' character).
     * <p>
     * Can be used anywhere except when a field name is expected.
     *
     * @throws IOException When JSON writing error occurs
     */
    void writeObjectStart() throws IOException;

    /**
     * Writes a closing marker of a JSON object value (the '}' character).
     * <p>
     * Can be used for closing objects either after a complete value or an object opening marker.
     *
     * @throws IOException When JSON writing error occurs
     */
    void writeObjectEnd() throws IOException;

    /**
     * Writes an opening marker of a JSON array value (the '[' character).
     * <p>
     * Can be used anywhere except when a field name is expected.
     *
     * @throws IOException When JSON writing error occurs
     */
    void writeArrayStart() throws IOException;

    /**
     * Writes a closing marker of a JSON array value(the ']' character).
     * <p>
     * Can be used when the innermost structured type is array.
     *
     * @throws IOException When JSON writing error occurs
     */
    void writeArrayEnd() throws IOException;

    /**
     * Outputs the given numeric value as a JSON number.
     *
     * @param number Number to write
     * @throws IOException When JSON writing error occurs
     */
    void writeNumber(Number number) throws IOException;

    /**
     * Outputs the given boolean value as a JSON boolean.
     *
     * @param value Value to write
     * @throws IOException When JSON writing error occurs
     */
    void writeBoolean(boolean value) throws IOException;

    /**
     * Outputs JSON literal {@code null} value.
     * <p>
     * This is usually not used, because {@code null} values are by default omitted by the serialization. But this can
     * be configurable.
     *
     * @throws IOException When JSON writing error occurs
     */
    void writeNull() throws IOException;

    /**
     * Outputs a String value.
     * <p>
     * Escaping will be done by the underlying implementation.
     *
     * @param text Text to write
     * @throws IOException When JSON writing error occurs
     */
    void writeString(String text) throws IOException;
}
