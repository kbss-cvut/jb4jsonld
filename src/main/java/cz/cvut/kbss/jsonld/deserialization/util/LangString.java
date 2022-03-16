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

import cz.cvut.kbss.jsonld.JsonLd;

import java.util.Optional;

/**
 * Represents a string with a language tag.
 */
public class LangString extends cz.cvut.kbss.ontodriver.model.LangString {

    public LangString(String value, String language) {
        super(value, language);
    }

    /**
     * Gets the language set on this tagged string.
     * <p>
     * Note that if the language is {@link JsonLd#NONE} (JSON-LD 1.1 keyword), an empty {@link Optional} is returned to ensure
     * consistency with {@link cz.cvut.kbss.jopa.model.MultilingualString} behavior.
     *
     * @return Language tag, possibly empty
     */
    @Override
    public Optional<String> getLanguage() {
        final Optional<String> superResult = super.getLanguage();
        return JsonLd.NONE.equals(superResult.get()) ? Optional.empty() : superResult;
    }

    @Override
    public String toString() {
        // This implementation returns only value to allow its usage in DataTypeTransformer
        return getValue();
    }
}
