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
package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jopa.model.MultilingualString;
import cz.cvut.kbss.jsonld.deserialization.util.LangString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultilingualStringContextTest {

    private MultilingualStringContext sut;

    @BeforeEach
    void setUp() {
        this.sut = new MultilingualStringContext(new MultilingualString(), Collections.emptyMap());
    }

    @Test
    void addItemAddsLangStringToCurrentInstance() {
        final LangString ls = new LangString("building", "en");
        sut.addItem(ls);
        assertTrue(sut.getInstance().contains("en"));
        assertEquals(ls.getValue(), sut.getInstance().get("en"));
    }

    @Test
    void addItemSetsSimpleLiteralInCurrentInstanceWhenStringIsPassedAsArgument() {
        final String value = "Mjolnir";
        sut.addItem(value);
        assertTrue(sut.getInstance().containsSimple());
        assertEquals(value, sut.getInstance().get());
    }
}
