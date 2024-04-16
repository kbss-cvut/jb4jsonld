/*
 * JB4JSON-LD
 * Copyright (C) 2024 Czech Technical University in Prague
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
package cz.cvut.kbss.jsonld.deserialization;

import cz.cvut.kbss.jsonld.environment.Generator;
import cz.cvut.kbss.jsonld.environment.model.Employee;
import cz.cvut.kbss.jsonld.environment.model.User;
import cz.cvut.kbss.jsonld.exception.JsonLdDeserializationException;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

class CollectionInstanceContextTest {

    @Test
    void addItemAddsObjectToCollectionInTheContext() {
        final InstanceContext<?> ctx = new CollectionInstanceContext<>(new HashSet<>(), Collections.emptyMap());
        final User u = Generator.generateUser();
        ctx.addItem(u);
        final Collection<?> col = (Collection<?>) ctx.getInstance();
        assertEquals(1, col.size());
        assertSame(u, col.iterator().next());
    }

    @Test
    void addItemResolvesReferenceToExistingInstanceAndAddsItIntoCollection() {
        final Employee e = Generator.generateEmployee();
        final InstanceContext<Set> ctx = new CollectionInstanceContext<>(new HashSet<>(), Employee.class,
                Collections.singletonMap(e.getUri().toString(), e));
        ctx.addItem(e.getUri().toString());
        assertTrue(ctx.getInstance().contains(e));
    }

    @Test
    void addItemTransformsStringValuesToUris() {
        final InstanceContext<Set> ctx = new CollectionInstanceContext<>(new HashSet<>(), URI.class,
                Collections.emptyMap());
        final Set<User> users = Generator.generateUsers();
        for (User u : users) {
            ctx.addItem(u.getUri().toString());
        }
        users.forEach(u -> assertTrue(ctx.getInstance().contains(u.getUri())));
    }

    @Test
    void addItemThrowsDeserializationExceptionWhenIncompatibleValueIsAdded() {
        final InstanceContext<Set> ctx = new CollectionInstanceContext<>(new HashSet<>(), Integer.class,
                Collections.emptyMap());
        final JsonLdDeserializationException result = assertThrows(JsonLdDeserializationException.class,
                () -> ctx.addItem("Test"));
        assertThat(result.getMessage(), containsString("Type mismatch"));
    }

    @Test
    void addItemResolvesReferenceToExistingInstanceAndAddsItsIdentifierIntoCollectionIfCollectionIsOfIdentifierType() {
        final Employee e = Generator.generateEmployee();
        final InstanceContext<Set> ctx = new CollectionInstanceContext<>(new HashSet<>(), URI.class,
                Collections.singletonMap(e.getUri().toString(), e));
        ctx.addItem(e.getUri().toString());
        assertTrue(ctx.getInstance().contains(e.getUri()));
    }
}
