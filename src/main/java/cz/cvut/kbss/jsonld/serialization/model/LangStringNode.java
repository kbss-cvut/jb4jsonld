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
package cz.cvut.kbss.jsonld.serialization.model;

import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.serialization.JsonNodeFactory;

/**
 * Represents a single string with a language tag.
 * <p>
 * It is written out as an object with two attributes - {@code @language} and {@code @value} with the corresponding
 * values.
 */
public class LangStringNode extends ObjectNode {

    public LangStringNode(String value, String language) {
        addItems(value, language);
    }

    private void addItems(String value, String language) {
        // @none is a JSON-LD 1.1 feature
        addItem(JsonNodeFactory.createLiteralNode(JsonLd.LANGUAGE, language != null ? language : JsonLd.NONE));
        addItem(JsonNodeFactory.createLiteralNode(JsonLd.VALUE, value));
    }

    public LangStringNode(String name, String value, String language) {
        super(name);
        addItems(value, language);
    }
}
