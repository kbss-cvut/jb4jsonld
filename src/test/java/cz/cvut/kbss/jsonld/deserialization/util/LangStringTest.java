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
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LangStringTest {

    @Test
    void getLanguageReturnsEmptyOptionalForNoneLanguage() {
        final LangString langString = new LangString("test", JsonLd.NONE);
        assertEquals(Optional.empty(), langString.getLanguage());
    }

    @Test
    void getLanguageReturnsLanguageByDefault() {
        final LangString langString = new LangString("test", "en");
        assertEquals(Optional.of("en"), langString.getLanguage());
    }
}
