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
package cz.cvut.kbss.jsonld.deserialization.util;

import cz.cvut.kbss.jsonld.JsonLd;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a string with a language tag.
 */
public class LangString implements Serializable {

    private final String value;
    private final String language;

    public LangString(String value, String language) {
        this.value = Objects.requireNonNull(value);
        this.language = language;
    }

    public String getValue() {
        return value;
    }

    /**
     * Gets the language set on this tagged string.
     * <p>
     * Note that if the language is {@link JsonLd#NONE} (JSON-LD 1.1 keyword), a {@code null} is returned to ensure
     * consistency with {@link cz.cvut.kbss.jopa.model.MultilingualString} behavior.
     *
     * @return Language tag, possibly {@code null}
     */
    public String getLanguage() {
        return JsonLd.NONE.equals(language) ? null : language;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LangString)) {
            return false;
        }
        LangString that = (LangString) o;
        return value.equals(that.value) && Objects.equals(language, that.language);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, language);
    }

    @Override
    public String toString() {
        // This implementation returns only value to allow its usage in DataTypeTransformer
        return value;
    }
}
