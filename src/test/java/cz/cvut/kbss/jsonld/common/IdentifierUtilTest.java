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
package cz.cvut.kbss.jsonld.common;

import cz.cvut.kbss.jsonld.environment.Generator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentifierUtilTest {

    @Test
    void isCompactIriReturnsFalseForExpandedAbsoluteIri() {
        assertFalse(IdentifierUtil.isCompactIri(Generator.generateUri().toString()));
    }

    @Test
    void isCompactIriReturnsFalseForBlankNodeIdentifier() {
        assertFalse(IdentifierUtil.isCompactIri(IdentifierUtil.generateBlankNodeId()));
    }

    @Test
    void isCompactIriReturnsTrueForCompactIri() {
        final String compact = "dc:description";
        assertTrue(IdentifierUtil.isCompactIri(compact));
    }
}
