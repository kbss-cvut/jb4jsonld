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
package cz.cvut.kbss.jsonld.environment;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Objects;

/**
 * Matches two {@link Model}s if they are isomorphic.
 *
 * That is, they contain the same statements (including some blank node magic).
 */
public class IsIsomorphic extends TypeSafeMatcher<Model> {

    private final Model expected;

    public IsIsomorphic(Model expected) {
        this.expected = Objects.requireNonNull(expected);
    }

    @Override
    protected boolean matchesSafely(Model actual) {
        return actual != null && Models.isomorphic(expected, actual);
    }

    @Override
    public void describeTo(Description description) {
        description.appendValueList("[", ", ", "]", expected);
    }

    public static IsIsomorphic isIsomorphic(Model expected) {
        return new IsIsomorphic(expected);
    }
}
