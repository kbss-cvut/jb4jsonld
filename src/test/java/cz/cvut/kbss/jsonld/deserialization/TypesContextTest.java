/**
 * Copyright (C) 2020 Czech Technical University in Prague
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

import cz.cvut.kbss.jsonld.environment.Vocabulary;
import cz.cvut.kbss.jsonld.environment.model.User;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypesContextTest {

    @Test
    void addItemSkipsTypeDeclaredOnClass() {
        final TypesContext<Set<String>, String> context = new TypesContext<>(new HashSet<>(), Collections.emptyMap(),
                String.class, User.class);
        context.addItem(Vocabulary.USER);
        context.addItem(Vocabulary.AGENT);
        context.addItem(Vocabulary.EMPLOYEE);
        assertTrue(context.getInstance().contains(Vocabulary.AGENT));
        assertTrue(context.getInstance().contains(Vocabulary.EMPLOYEE));
        assertFalse(context.getInstance().contains(Vocabulary.USER));
    }

    @Test
    void addItemTransformsValueToElementTypeUri() {
        final TypesContext<Set<URI>, URI> context = new TypesContext<>(new HashSet<>(), Collections.emptyMap(),
                URI.class, User.class);
        context.addItem(Vocabulary.USER);
        context.addItem(Vocabulary.AGENT);
        context.addItem(Vocabulary.EMPLOYEE);
        assertTrue(context.getInstance().contains(URI.create(Vocabulary.AGENT)));
        assertTrue(context.getInstance().contains(URI.create(Vocabulary.EMPLOYEE)));
        assertFalse(context.getInstance().contains(URI.create(Vocabulary.USER)));
    }
}