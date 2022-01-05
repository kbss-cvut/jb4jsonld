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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LangStringTest {

    @Test
    void getLanguageReturnsNullForNoneLanguage() {
        final LangString langString = new LangString("test", JsonLd.NONE);
        assertNull(langString.getLanguage());
    }

    @Test
    void getLanguageReturnsLanguageByDefault() {
        final LangString langString = new LangString("test", "en");
        assertEquals("en", langString.getLanguage());
    }
}
